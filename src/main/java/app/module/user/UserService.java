/*
 * Copyright 2017 TomeOkin
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.module.user;

import app.config.Crypto;
import app.config.CryptoToken;
import app.config.ErrorCode;
import app.dao.UserDao;
import app.data.*;
import app.module.auth.CaptchaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CharMatcher;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final int EXPIRE_TIME = 24; // hours

    private final CaptchaService captchaService;
    private final UserDao userDao;
    private final ObjectMapper jsonUtils;
    private final Funnel<AccountSession> expireFunnel;

    @Autowired
    public UserService(CaptchaService captchaService, UserDao userDao, ObjectMapper jsonUtils) {
        this.captchaService = captchaService;
        this.userDao = userDao;
        this.jsonUtils = jsonUtils;

        expireFunnel =
            (Funnel<AccountSession>) (from, into) -> into.putLong(from.getUserId())
                .putLong(from.getExpireTime());
    }

    public Response<User> updateUserInfo(long currentUserId, User user) {
        User oldUser = userDao.findOne(currentUserId);
        if (oldUser == null) {
            return ErrorCode.error(ErrorCode.USER_NOT_EXISTED);
        }

        oldUser.setAvatar(user.getAvatar());
        oldUser.setDescription(user.getDescription());
        oldUser.setUsername(user.getUsername());
        User result = userDao.save(oldUser).profile();
        return Response.create(result);
    }

    public Response<AccessBundle> register(@Nonnull RegisterData registerData) {
        String ticket = registerData.getTicket();
        if (ticket == null) {
            return ErrorCode.error(ErrorCode.INVALID_TICKET);
        }

        PhoneData phoneData = captchaService.queryTicket(ticket);
        if (phoneData == null) {
            return ErrorCode.error(ErrorCode.INVALID_TICKET);
        }

        boolean isPasswordValid = checkPasswordStrengthBaseline(registerData.getPassword());
        String username = registerData.getUsername();
        boolean isUsernameValid = username.length() >= 3 && username.length() <= 12;
        if (!isPasswordValid || !isUsernameValid) {
            return ErrorCode.error(ErrorCode.INVALID_ACCOUNT);
        }

        User existed = userDao.findFirstByPhone(phoneData.getPhone());
        if (existed != null) {
            return ErrorCode.error(ErrorCode.ACCOUNT_EXISTED);
        }

        User user = new User(username, phoneData.getPhone(), phoneData.getCountry(), registerData.getPassword(),
            registerData.getAvatar());
        User result = userDao.save(user);

        AccessBundle accessBundle = newAccessBundle(result);
        if (accessBundle == null) {
            return ErrorCode.error(ErrorCode.FAILED_CREATE_USER_TOKEN);
        }
        captchaService.removeTicket(ticket);
        return Response.create(accessBundle);
    }

    public Response<AccessBundle> login(@Nonnull LoginData loginData) {
        String phone = loginData.getPhone();
        if (phone == null) {
            return ErrorCode.error(ErrorCode.INVALID_ACCOUNT);
        }
        log.info("登录信息：{}", loginData.toString());

        User user = userDao.findFirstByPhone(phone);
        if (user == null) {
            return ErrorCode.error(ErrorCode.ACCOUNT_NOT_EXISTED);
        }

        if (!user.getPassword().equals(loginData.getPassword())) {
            return ErrorCode.error(ErrorCode.PASSWORD_ERROR);
        }

        AccessBundle accessBundle = newAccessBundle(user);
        if (accessBundle == null) {
            return ErrorCode.error(ErrorCode.FAILED_CREATE_USER_TOKEN);
        }
        return Response.create(accessBundle);
    }

    private AccessBundle newAccessBundle(User user) {
        DateTime expireTime = DateTime.now().plusHours(EXPIRE_TIME);
        AccountSession session = new AccountSession();
        session.setUserId(user.getId());
        session.setExpireTime(expireTime.getMillis());
        CryptoToken expireToken = newSessionToken(session);
        if (expireToken == null) {
            return null;
        }

        return new AccessBundle(user, expireTime.toDate(), expireToken);
    }

    // public Response<AccessBundle> refreshExpireToken(@Nonnull CryptoToken cryptoToken) {
    //     long result = checkExpireToken(cryptoToken);
    //     if (result < 0) {
    //         return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
    //     }
    //
    //     User user = userDao.findOne(result);
    //     AccessBundle accessBundle = newAccessBundle(user);
    //     if (accessBundle == null) {
    //         return ErrorCode.error(ErrorCode.FAILED_CREATE_USER_TOKEN);
    //     }
    //     return Response.create(accessBundle);
    // }

    // private long checkExpireToken(CryptoToken cryptoToken) {
    //     AccountSession session = parseSession(cryptoToken);
    //     if (session == null) {
    //         return -1;
    //     }
    //
    //     byte[] sessionData = Hashing.sipHash24().hashObject(session, expireFunnel).asBytes();
    //     String sessionString = new String(sessionData, StandardCharsets.UTF_8);
    //     if (!sessionString.equals(session.getSession())) {
    //         log.warn("checkExpireToken - session not equal");
    //         return -1;
    //     }
    //
    //     return session.getUserId();
    // }

    private AccountSession parseSession(CryptoToken cryptoToken) {
        try {
            byte[] json = Crypto.decrypt(cryptoToken);
            return jsonUtils.readValue(json, AccountSession.class);
        } catch (Exception e) {
            log.warn("checkExpireToken - decrypt token failure", e);
            return null;
        }
    }

    public long checkExpireTokenWithTime(String token) {
        try {
            CryptoToken cryptoToken = jsonUtils.readValue(token, CryptoToken.class);
            return checkExpireTokenWithTime(cryptoToken);
        } catch (Exception e) {
            log.warn("check auth bind failure", e);
            return -1;
        }
    }

    public long checkExpireTokenWithTime(CryptoToken cryptoToken) {
        AccountSession expireSession = parseSession(cryptoToken);
        if (expireSession == null) {
            return -1;
        }

        byte[] sessionData = Hashing.sipHash24().hashObject(expireSession, expireFunnel).asBytes();
        String sessionString = new String(sessionData, StandardCharsets.UTF_8);
        if (!sessionString.equals(expireSession.getSession())) {
            log.warn("checkExpireToken - session not equal");
            return -1;
        }

        DateTime now = DateTime.now();
        DateTime deadline = new DateTime(expireSession.getExpireTime());
        if (now.isBefore(deadline) || now.isEqual(deadline)) {
            return expireSession.getUserId();
        }
        return 0;
    }

    private int checkPasswordStrength(String password) {
        // 检查字符串格式 ([a-zA-Z0-9\\.,;]){6,16}
        int strength = (password == null || password.length() < 6 || password.length() > 16) ? 0 :
            CharMatcher.anyOf(".,;").or(CharMatcher.javaLetterOrDigit()).negate().indexIn(password);
        if (strength >= 0) {
            return -1;
        }

        strength = CharMatcher.digit().indexIn(password) >= 0 ? 1 : 0;
        strength += CharMatcher.anyOf(".,;").indexIn(password) >= 0 ? 1 : 0;
        strength += CharMatcher.javaLowerCase().indexIn(password) >= 0 ? 1 : 0;
        strength += CharMatcher.javaUpperCase().indexIn(password) >= 0 ? 1 : 0;
        return strength;
    }

    private boolean checkPasswordStrengthBaseline(String password) {
        return checkPasswordStrength(password) >= 2;
    }

    private CryptoToken newSessionToken(AccountSession session) {
        byte[] data = Hashing.sipHash24().hashObject(session, expireFunnel).asBytes();
        String sessionString = new String(data, StandardCharsets.UTF_8);
        session.setSession(sessionString);
        CryptoToken token;
        try {
            byte[] sessionData = jsonUtils.writeValueAsBytes(session);
            token = Crypto.encrypt(sessionData);
        } catch (Exception e) {
            log.warn("encrypt session failure", e);
            return null;
        }
        return token;
    }
}
