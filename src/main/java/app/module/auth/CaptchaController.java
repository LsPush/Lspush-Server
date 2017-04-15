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
package app.module.auth;

import app.config.Crypto;
import app.config.CryptoToken;
import app.config.ErrorCode;
import app.data.PhoneData;
import app.data.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {
    private static final Logger log = LoggerFactory.getLogger(CaptchaController.class);
    private final CaptchaService captchaService;
    private final ObjectMapper jsonUtils;

    @Autowired
    public CaptchaController(CaptchaService captchaService, ObjectMapper jsonUtils) {
        this.captchaService = captchaService;
        this.jsonUtils = jsonUtils;
    }

    @PostMapping("/checkCaptcha")
    public Response<String> checkCaptcha(@RequestBody CryptoToken cryptoToken) {
        PhoneData phoneData;
        try {
            byte[] json = Crypto.decrypt(cryptoToken);
            phoneData = jsonUtils.readValue(json, PhoneData.class);
        } catch (Exception e) {
            log.warn("decrypt captcha crypt-token failure", e);
            return ErrorCode.error(ErrorCode.INVALID_TOKEN);
        }

        String ticket = null;
        if (phoneData != null) {
            ticket = captchaService.checkCaptcha(phoneData);
        }
        if (ticket != null) {
            return Response.create(ticket);
        } else {
            return ErrorCode.error(ErrorCode.INVALID_CAPTCHA);
        }
    }
}
