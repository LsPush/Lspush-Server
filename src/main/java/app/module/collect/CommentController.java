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
import app.data.Comment;
import app.data.Response;
import app.module.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    private final UserService userService;
    private final CommentService commentService;

    @Autowired
    public CommentController(UserService userService, CommentService commentService) {
        this.userService = userService;
        this.commentService = commentService;
    }

    @PostMapping("/add/{colId:.*}")
    public Response<Comment> addComment(@RequestHeader(value = "token") String token, @PathVariable("colId") long colId,
        @RequestBody String comment) {
        long userId = userService.checkExpireTokenWithTime(token);
        if (userId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (userId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        if (colId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_COLLECTION);
        }

        if (comment == null) {
            return ErrorCode.error(ErrorCode.EMPTY_COMMENT);
        }

        return commentService.addComment(userId, colId, comment);
    }

    @GetMapping("/find/{colId:.*}")
    public Response<List<Comment>> findCommentByCollection(@RequestHeader(value = "token") String token,
        @PathVariable("colId") long colId,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size) {
        long userId = userService.checkExpireTokenWithTime(token);
        if (userId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_USER_TOKEN);
        } else if (userId == 0) {
            return ErrorCode.error(ErrorCode.TOKEN_TIMEOUT);
        }

        if (colId < 0) {
            return ErrorCode.error(ErrorCode.INVALID_COLLECTION);
        }

        return commentService.findCommentByCollection(colId, page, size);
    }
}
