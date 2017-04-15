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
import app.data.*;
import app.utils.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final ObjectMapper jsonUtils;
    private final String DIRECTORY;
    private final String DOWNLOAD_PATH;

    @Autowired
    public UserController(UserService userService, ObjectMapper jsonUtils, Environment env) {
        this.userService = userService;
        this.jsonUtils = jsonUtils;
        String serverUrl = env.getProperty("lspush.serverUrl");
        DOWNLOAD_PATH = serverUrl + "/api/resource/download/";
        DIRECTORY = env.getProperty("app.path.upload");
    }

    @PostMapping("/register")
    public Response<AccessBundle> register(@RequestBody CryptoToken cryptoToken) {
        RegisterData registerData;
        try {
            byte[] json = Crypto.decrypt(cryptoToken);
            registerData = jsonUtils.readValue(json, RegisterData.class);
        } catch (Exception e) {
            log.warn("decrypt register crypt-token failure", e);
            return ErrorCode.error(ErrorCode.INVALID_TOKEN);
        }

        if (registerData == null) {
            return ErrorCode.error(ErrorCode.INVALID_ACCOUNT);
        }
        return userService.register(registerData);
    }

    @PostMapping("/login")
    public Response<AccessBundle> login(@RequestBody CryptoToken cryptoToken) {
        LoginData loginData;
        try {
            byte[] json = Crypto.decrypt(cryptoToken);
            loginData = jsonUtils.readValue(json, LoginData.class);
        } catch (Exception e) {
            log.warn("decrypt login crypt-token failure", e);
            return ErrorCode.error(ErrorCode.INVALID_TOKEN);
        }

        if (loginData == null) {
            return ErrorCode.error(ErrorCode.INVALID_ACCOUNT);
        }
        return userService.login(loginData);
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/updateUserInfo")
    public Response<User> updateUserInfo(@RequestHeader(value = "token") String token,
        @RequestParam(value = "file", required = false) MultipartFile file,
        @RequestParam(value = "user") String userInfo) throws IOException {

        long currentUserId = userService.checkExpireTokenWithTime(token);
        if (currentUserId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (currentUserId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        String path = null;
        try {
            if (file != null) {
                String filename = file.getOriginalFilename();
                FileUtils.ensurePath(DIRECTORY);
                String newFileName = FileUtils.generateImageFilename(filename);
                String filepath = Paths.get(DIRECTORY, newFileName).toString();

                OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(filepath)));
                FileCopyUtils.copy(file.getInputStream(), out);
                path = DOWNLOAD_PATH + newFileName;
            }
        } catch (Exception e) {
            log.error("updateUserInfo", e);
            return ErrorCode.error(ErrorCode.UPDATE_USER_INFO_FAILED);
        }

        User user = jsonUtils.readValue(userInfo, User.class);

        if (path != null) {
            user.setAvatar(path);
        }

        return userService.updateUserInfo(currentUserId, user);
    }

    // @PostMapping("/updateUserToken")
    // public Response<AccessBundle> updateUserToken(@RequestBody CryptoToken cryptoToken) {
    //     if (cryptoToken == null) {
    //         return ErrorCode.error(ErrorCode.INVALID_TOKEN);
    //     }
    //
    //     return userService.refreshExpireToken(cryptoToken);
    // }
}
