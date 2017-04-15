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
import app.dao.FollowDao;
import app.dao.UserDao;
import app.data.BaseResponse;
import app.data.Follow;
import app.data.Response;
import app.data.User;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FollowService {
    private static final Logger log = LoggerFactory.getLogger(FollowService.class);
    private final FollowDao followDao;
    private final UserDao userDao;

    public static final Sort UPDATE_SORT = new Sort(Sort.Direction.DESC, "updateDate", "id");

    @Autowired
    public FollowService(FollowDao followDao, UserDao userDao) {
        this.followDao = followDao;
        this.userDao = userDao;
    }

    public BaseResponse follow(long currentUserId, long targetUserId) {
        if (currentUserId == targetUserId) return BaseResponse.SUCCESS;

        User user = userDao.findOne(targetUserId);
        if (user == null) {
            return ErrorCode.error(ErrorCode.USER_NOT_EXISTED);
        }

        Follow follow = new Follow(currentUserId, targetUserId);
        Follow result = followDao.findOneByOwnerAndFollowing(currentUserId, targetUserId);
        if (result != null) {
            follow.setId(result.getId());
        }
        follow.setUpdateDate(DateTime.now().toDate());
        followDao.save(follow);
        return BaseResponse.SUCCESS;
    }

    public BaseResponse unfollow(long currentUserId, long targetUserId) {
        if (currentUserId == targetUserId) return BaseResponse.SUCCESS;

        User user = userDao.findOne(targetUserId);
        if (user == null) {
            return ErrorCode.error(ErrorCode.USER_NOT_EXISTED);
        }

        Follow result = followDao.findOneByOwnerAndFollowing(currentUserId, targetUserId);
        if (result != null) {
            followDao.delete(result.getId());
        }
        return BaseResponse.SUCCESS;
    }

    public Response<List<User>> getUserFollowing(long currentUserId, long targetUserId, int page, int size) {
        User user = userDao.findOne(targetUserId);
        if (user == null) {
            return ErrorCode.error(ErrorCode.USER_NOT_EXISTED);
        }

        Page<Follow> follows = followDao.findByOwner(targetUserId, new PageRequest(page, size, UPDATE_SORT));
        List<User> users = new ArrayList<>(follows.getContent().size());
        for (Follow follow : follows) {
            long userId = follow.getFollowingId();
            User one = userDao.findOne(userId);
            // 关心的是我有没有 follow 这个用户
            one.setHasFollow(hasFollow(currentUserId, userId));
            users.add(one);
        }
        return Response.create(users);
    }

    public Response<List<User>> getUserFollowers(long currentUserId, long targetUserId, int page, int size) {
        User user = userDao.findOne(targetUserId);
        if (user == null) {
            return ErrorCode.error(ErrorCode.USER_NOT_EXISTED);
        }

        Page<Follow> follows = followDao.findByFollowing(targetUserId, new PageRequest(page, size, UPDATE_SORT));
        List<User> users = new ArrayList<>(follows.getContent().size());
        for (Follow follow : follows) {
            long userId = follow.getOwnerId();
            User one = userDao.findOne(userId);
            // 关心的是我有没有 follow 这个用户
            one.setHasFollow(hasFollow(currentUserId, userId));
            users.add(one);
        }
        return Response.create(users);
    }

    public boolean hasFollow(long currentUserId, long targetUserId) {
        if (currentUserId != targetUserId) {
            Follow follow = followDao.findOneByOwnerAndFollowing(currentUserId, targetUserId);
            return follow != null;
        } else {
            return true; // all user follow them self, but not existed in table
        }
    }
}
