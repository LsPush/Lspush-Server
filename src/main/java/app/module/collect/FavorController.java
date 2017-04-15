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
package app.module.collect;

import app.config.ErrorCode;
import app.data.BaseResponse;
import app.module.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favor")
public class FavorController {
    private final UserService userService;
    private final FavorService favorService;

    @Autowired
    public FavorController(UserService userService, FavorService favorService) {
        this.userService = userService;
        this.favorService = favorService;
    }

    @PostMapping("/add/{colId:.*}")
    public BaseResponse addFavor(@RequestHeader(value = "token") String token, @PathVariable("colId") long colId) {
        long userId = userService.checkExpireTokenWithTime(token);
        if (userId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (userId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        if (colId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_COLLECTION);
        }

        return favorService.addFavor(userId, colId);
    }

    @PostMapping("/remove/{colId:.*}")
    public BaseResponse removeFavor(@RequestHeader(value = "token") String token, @PathVariable("colId") long colId) {
        long userId = userService.checkExpireTokenWithTime(token);
        if (userId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (userId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        if (colId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_COLLECTION);
        }

        return favorService.removeFavor(userId, colId);
    }
}
