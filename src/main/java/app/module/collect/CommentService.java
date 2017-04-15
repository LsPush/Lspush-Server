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
import app.dao.CollectionDao;
import app.dao.CommentDao;
import app.dao.UserDao;
import app.data.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {
    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    private final CommentDao commentDao;
    private final CollectionDao colDao;
    private final UserDao userDao;
    public static final Sort UPDATE_SORT = new Sort(Sort.Direction.DESC, "updateDate", "id");

    @Autowired
    public CommentService(CommentDao commentDao, CollectionDao colDao, UserDao userDao) {
        this.commentDao = commentDao;
        this.colDao = colDao;
        this.userDao = userDao;
    }

    public Response<Comment> addComment(long userId, long colId, @Nonnull String comment) {
        Collection collection = colDao.findOne(colId);
        if (collection == null) {
            return ErrorCode.error(ErrorCode.COLLECTION_NOT_EXISTED);
        }

        Comment result = new Comment();
        result.setColId(colId);
        result.setUserId(userId);
        result.setUpdateDate(DateTime.now().toDate());
        result.setComment(comment);

        Comment data = commentDao.save(result);
        User user = userDao.findOne(userId);
        data.setUser(user.profile());
        return Response.create(data);
    }

    public Response<List<Comment>> findCommentByCollection(long colId, int page, int size) {
        Page<Comment> comments = commentDao.findByCollection(colId, new PageRequest(page, size, UPDATE_SORT));
        List<Comment> commentList = new ArrayList<>(comments.getContent().size());
        for (Comment comment : comments) {
            Comment item = new Comment();
            item.setId(comment.getId());
            item.setComment(comment.getComment());
            item.setUpdateDate(comment.getUpdateDate());

            User user = userDao.findOne(comment.getUserId());
            item.setUser(user.profile());
            commentList.add(item);
        }
        return Response.create(commentList);
    }
}
