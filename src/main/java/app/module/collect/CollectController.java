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
import app.data.Collect;
import app.data.Response;
import app.data.UserProfile;
import app.module.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collect")
public class CollectController {
    private final UserService userService;
    private final CollectService colService;

    @Autowired
    public CollectController(UserService userService, CollectService colService) {
        this.userService = userService;
        this.colService = colService;
    }

    @PostMapping("/post")
    public BaseResponse postCollection(@RequestHeader(value = "token") String token, @RequestBody Collect collect) {
        long userId = userService.checkExpireTokenWithTime(token);
        if (userId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (userId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        if (collect == null) {
            return ErrorCode.error(ErrorCode.INVALID_COLLECTION);
        }
        return colService.postCollect(userId, collect);
    }

    @GetMapping("/newest")
    public Response<List<Collect>> findNewestCollections(@RequestHeader(value = "token") String token,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size) {
        long userId = userService.checkExpireTokenWithTime(token);
        if (userId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (userId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        return colService.findNewestCollections(userId, page, size);
    }

    @GetMapping("/own/{userId:.*}")
    public Response<List<Collect>> findUserCollections(@RequestHeader(value = "token") String token,
        @PathVariable("userId") long targetUserId,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size) {
        long currentUserId = userService.checkExpireTokenWithTime(token);
        if (currentUserId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (currentUserId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        return colService.findUserCollections(currentUserId, targetUserId, page, size);
    }

    @GetMapping("/profile/{userId:.*}")
    public Response<UserProfile> getUserProfile(@RequestHeader(value = "token") String token,
        @PathVariable("userId") long targetUserId) {
        long currentUserId = userService.checkExpireTokenWithTime(token);
        if (currentUserId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (currentUserId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        return colService.getUserProfile(currentUserId, targetUserId);
    }

    @GetMapping("/favor/{userId:.*}")
    public Response<List<Collect>> findUserFavorCollections(@RequestHeader(value = "token") String token,
        @PathVariable("userId") long targetUserId,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size) {
        long currentUserId = userService.checkExpireTokenWithTime(token);
        if (currentUserId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (currentUserId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        return colService.findUserFavorCollections(currentUserId, targetUserId, page, size);
    }

    /**
     * @param option one of {title, tag, url}
     * @param group  one of {all, user, favor}
     */
    @GetMapping("/search")
    public Response<List<Collect>> findCollect(@RequestHeader(value = "token") String token,
        @RequestParam(value = "keyword") String keyword,
        @RequestParam(value = "option", defaultValue = "title", required = false) String option,
        @RequestParam(value = "group", defaultValue = "all", required = false) String group,
        @RequestParam(value = "targetUserId", defaultValue = "-1", required = false) long targetUserId,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size) {
        long currentUserId = userService.checkExpireTokenWithTime(token);
        if (currentUserId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (currentUserId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        if (targetUserId == -1) targetUserId = currentUserId;

        return colService.findCollect(currentUserId, targetUserId, option, group, keyword, page, size);
    }

    @GetMapping("/hot")
    public Response<List<Collect>> findHotCollections(@RequestHeader(value = "token") String token,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size) {
        long userId = userService.checkExpireTokenWithTime(token);
        if (userId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (userId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        return colService.findHotCollections(userId, page, size);
    }

    @GetMapping("/recent")
    public Response<List<Collect>> findRecentHotCollections(@RequestHeader(value = "token") String token,
        @RequestParam(value = "days", defaultValue = "5", required = false) int days) {
        long userId = userService.checkExpireTokenWithTime(token);
        if (userId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (userId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        return colService.findRecentHotCollections(userId, days);
    }
}
