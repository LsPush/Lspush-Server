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

import app.config.ErrorCode;
import app.data.BaseResponse;
import app.data.Response;
import app.data.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
public class FollowController {
    private final UserService userService;
    private final FollowService followService;

    @Autowired
    public FollowController(UserService userService, FollowService followService) {
        this.userService = userService;
        this.followService = followService;
    }

    @PostMapping("/follow/{userId:.*}")
    public BaseResponse follow(@RequestHeader(value = "token") String token, @PathVariable("userId") long userId) {
        long currentUserId = userService.checkExpireTokenWithTime(token);
        if (currentUserId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (currentUserId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        if (userId < 0) {
            return ErrorCode.error(ErrorCode.USER_NOT_EXISTED);
        }

        return followService.follow(currentUserId, userId);
    }

    @PostMapping("/unfollow/{userId:.*}")
    public BaseResponse unfollow(@RequestHeader(value = "token") String token, @PathVariable("userId") long userId) {
        long currentUserId = userService.checkExpireTokenWithTime(token);
        if (currentUserId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (currentUserId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        if (userId < 0) {
            return ErrorCode.error(ErrorCode.USER_NOT_EXISTED);
        }

        return followService.unfollow(currentUserId, userId);
    }

    @GetMapping("/following/{userId:.*}")
    public Response<List<User>> getUserFollowing(@RequestHeader(value = "token") String token,
        @PathVariable("userId") long userId,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size) {
        long currentUserId = userService.checkExpireTokenWithTime(token);
        if (currentUserId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (currentUserId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        if (userId < 0) {
            return ErrorCode.error(ErrorCode.USER_NOT_EXISTED);
        }

        return followService.getUserFollowing(currentUserId, userId, page, size);
    }

    @GetMapping("/follower/{userId:.*}")
    public Response<List<User>> getUserFollowers(@RequestHeader(value = "token") String token,
        @PathVariable("userId") long userId,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size) {
        long currentUserId = userService.checkExpireTokenWithTime(token);
        if (currentUserId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (currentUserId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        if (userId < 0) {
            return ErrorCode.error(ErrorCode.USER_NOT_EXISTED);
        }

        return followService.getUserFollowers(currentUserId, userId, page, size);
    }
}
