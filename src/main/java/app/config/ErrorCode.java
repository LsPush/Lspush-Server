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
package app.config;

import app.data.BaseResponse;
import app.data.Response;

import java.util.Map;
import java.util.TreeMap;

public class ErrorCode {
    public static final int TOKEN_TIMEOUT = BaseResponse.TOKEN_TIMEOUT;
    public static final int INVALID_TOKEN = 101;
    public static final int INVALID_CAPTCHA = 102;
    public static final int INVALID_TICKET = 103;
    public static final int INVALID_ACCOUNT = 104;
    public static final int FAILED_CREATE_USER_TOKEN = 105;
    public static final int INVALID_USER_TOKEN = 106;
    public static final int ACCOUNT_EXISTED = 107;
    public static final int ACCOUNT_NOT_EXISTED = 108;
    public static final int PASSWORD_ERROR = 109;
    public static final int INVALID_URL = 110;
    public static final int COLLECTION_NOT_EXISTED = 111;
    public static final int URL_EMPTY = 112;
    public static final int INVALID_COLLECTION = 113;
    public static final int EMPTY_COMMENT = 114;
    public static final int USER_NOT_EXISTED = 115;
    public static final int UPDATE_USER_INFO_FAILED = 116;

    private static final Map<Integer, String> ERROR_CODE = new TreeMap<>();

    static {
        ERROR_CODE.put(INVALID_TOKEN, "Invalid Token");
        ERROR_CODE.put(INVALID_CAPTCHA, "Invalid Captcha");
        ERROR_CODE.put(INVALID_TICKET, "Invalid Ticket");
        ERROR_CODE.put(INVALID_ACCOUNT, "账号或密码格式错误");
        ERROR_CODE.put(FAILED_CREATE_USER_TOKEN, "Failed to Create User Token");
        ERROR_CODE.put(INVALID_USER_TOKEN, "Invalid User Token");
        ERROR_CODE.put(ACCOUNT_EXISTED, "账号已存在");
        ERROR_CODE.put(ACCOUNT_NOT_EXISTED, "账号不存在");
        ERROR_CODE.put(PASSWORD_ERROR, "密码错误");
        ERROR_CODE.put(INVALID_URL, "Invalid Url");
        ERROR_CODE.put(COLLECTION_NOT_EXISTED, "Collection Not Existed");
        ERROR_CODE.put(URL_EMPTY, "Url is Empty");
        ERROR_CODE.put(TOKEN_TIMEOUT, "Token Timeout");
        ERROR_CODE.put(INVALID_COLLECTION, "Invalid Collection");
        ERROR_CODE.put(EMPTY_COMMENT, "Comment Not Empty");
        ERROR_CODE.put(USER_NOT_EXISTED, "用户不存在");
        ERROR_CODE.put(UPDATE_USER_INFO_FAILED, "更新用户信息失败");
    }

    public static <T> Response<T> error(int errorCode) {
        return new Response<>(errorCode, ERROR_CODE.get(errorCode));
    }

    public static <T> Response<T> get(int resultCode, String message, T data) {
        if (resultCode == BaseResponse.COMMON_SUCCESS) {
            return new Response<>(data);
        }

        return new Response<>(resultCode, ERROR_CODE.getOrDefault(resultCode, message));
    }
}
