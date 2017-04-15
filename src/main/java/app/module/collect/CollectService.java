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
import app.dao.*;
import app.data.*;
import app.data.internal.HotCollection;
import app.module.user.FollowService;
import app.utils.TextUtils;
import app.utils.URLUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CollectService {
    private static final Logger log = LoggerFactory.getLogger(CollectService.class);
    private final CollectionDao colDao;
    private final UserDao userDao;
    private final FavorDao favorDao;
    private final CommentDao commentDao;
    private final FavorService favorService;
    private final FollowDao followDao;
    private final FollowService followService;
    private final JdbcTemplate jdbcTemplate;
    public static final Sort UPDATE_SORT = new Sort(Sort.Direction.DESC, "updateDate", "id");
    public static final String SQL_SEARCH_FAVOR_COLLECT = "SELECT DISTINCT collection.id as colId\n"
        + "FROM collection, favor\n"
        + "WHERE collection.id = favor.col_id && favor.user_id = %d && collection.%s like \"%%%s%%\"\n"
        + "GROUP BY colId\n"
        + "ORDER BY collection.update_date DESC, col_id DESC\n"
        + "LIMIT %d, %d";

    @Autowired
    public CollectService(CollectionDao colDao, UserDao userDao, FavorDao favorDao, CommentDao commentDao,
        FavorService favorService, FollowDao followDao, FollowService followService, JdbcTemplate jdbcTemplate) {
        this.colDao = colDao;
        this.userDao = userDao;
        this.favorDao = favorDao;
        this.commentDao = commentDao;
        this.favorService = favorService;
        this.followDao = followDao;
        this.followService = followService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public BaseResponse postCollect(long userId, @Nonnull Collect collect) {
        String url = collect.getUrl();
        if (!URLUtils.isNetworkUrlStrict(url)) {
            return ErrorCode.error(ErrorCode.INVALID_URL);
        }

        Collection result = null;
        if (collect.getId() > 0) {
            result = colDao.findOne(collect.getId());
        }

        Date now = DateTime.now().toDate();
        Collection collection = new Collection();
        if (result == null) {
            collection.setUrl(url);
            collection.setUserId(userId);
            collection.setCreateDate(now);
        } else {
            collection.setId(result.getId());
            collection.setUrl(result.getUrl());
            collection.setUserId(result.getUserId());
            collection.setCreateDate(result.getCreateDate());
        }

        collection.setTitle(collect.getTitle());
        collection.setDescription(collect.getDescription());
        collection.setImage(collect.getImage());
        collection.setTags(TextUtils.join("|", collect.getTags()));
        collection.setUpdateDate(now);

        colDao.save(collection);
        return BaseResponse.SUCCESS;
    }

    public Response<List<Collect>> findNewestCollections(long currentUserId, int page, int size) {
        Page<Collection> collections = colDao.findAll(new PageRequest(page, size, UPDATE_SORT));
        return Response.create(parseCollections(currentUserId, collections));
    }

    public Response<List<Collect>> findUserCollections(long currentUserId, long targetUserId, int page, int size) {
        Page<Collection> collections = colDao.findByUser(targetUserId, new PageRequest(page, size, UPDATE_SORT));

        User user = userDao.findOne(targetUserId).profile();
        List<Collect> collects = new ArrayList<>(collections.getContent().size());
        for (Collection col : collections) {
            Collect collect = new Collect(col);
            collect.setUser(user);
            collects.add(fillCollect(collect, col.getId(), currentUserId));
        }

        return Response.create(collects);
    }

    public Response<List<Collect>> findUserFavorCollections(long currentUserId, long targetUserId, int page, int size) {
        Page<Favor> favors = favorDao.findByUser(targetUserId, new PageRequest(page, size, UPDATE_SORT));

        List<Collect> collects = new ArrayList<>(favors.getContent().size());
        for (Favor favor : favors) {
            Collection col = colDao.findOne(favor.getColId());
            Collect collect = new Collect(col);

            User user = userDao.findOne(col.getUserId()).profile();
            collect.setUser(user);

            collects.add(fillCollect(collect, favor.getColId(), currentUserId));
        }

        return Response.create(collects);
    }

    public Response<List<Collect>> findCollect(long currentUserId, long targetUserId, String option, String group,
        String keyword, int page, int size) {

        User targetUser = userDao.findOne(targetUserId);
        if (targetUser == null) targetUserId = currentUserId;

        List<Collect> collects;
        switch (group) {
            case "user":
                collects = searchCollectOfUser(currentUserId, targetUserId, option, keyword, page, size);
                break;
            case "favor":
                collects = searchCollectOfUserFavor(currentUserId, targetUserId, option, keyword, page, size);
                break;
            case "all":
            default:
                collects = searchCollect(currentUserId, option, keyword, page, size);
                break;
        }
        return Response.create(collects);
    }

    private List<Collect> searchCollectOfUser(long currentUserId, long targetUserId, String option, String keyword,
        int page, int size) {
        PageRequest pageRequest = new PageRequest(page, size, UPDATE_SORT);
        Page<Collection> collections;
        switch (option) {
            case "tag":
                collections = colDao.findByTagAndUser(keyword, targetUserId, pageRequest);
                break;
            case "url":
                collections = colDao.findByUrlAndUser(keyword, targetUserId, pageRequest);
                break;
            case "title":
            default:
                collections = colDao.findByTitleAndUser(keyword, targetUserId, pageRequest);
                break;
        }
        return parseCollections(currentUserId, collections);
    }

    private List<Collect> searchCollect(long currentUserId, String option, String keyword,
        int page, int size) {
        PageRequest pageRequest = new PageRequest(page, size, UPDATE_SORT);
        Page<Collection> collections;
        switch (option) {
            case "tag":
                collections = colDao.findByTag(keyword, pageRequest);
                break;
            case "url":
                collections = colDao.findByUrl(keyword, pageRequest);
                break;
            case "title":
            default:
                collections = colDao.findByTitle(keyword, pageRequest);
                break;
        }
        return parseCollections(currentUserId, collections);
    }

    private List<Collect> searchCollectOfUserFavor(long currentUserId, long targetUserId, String option, String keyword,
        int page, int size) {
        String sql;
        switch (option) {
            case "tag":
                sql = String.format(SQL_SEARCH_FAVOR_COLLECT, targetUserId, "tags", keyword, page, size);
                break;
            case "url":
                sql = String.format(SQL_SEARCH_FAVOR_COLLECT, targetUserId, "url", keyword, page, size);
                break;
            case "title":
            default:
                sql = String.format(SQL_SEARCH_FAVOR_COLLECT, targetUserId, "title", keyword, page, size);
                break;
        }
        List<Long> ids = jdbcTemplate.queryForList(sql, Long.class);
        List<Collection> collections = new ArrayList<>(ids.size());
        for (Long id : ids) {
            collections.add(colDao.findOne(id));
        }
        return parseCollections(currentUserId, collections, collections.size());
    }

    public Response<List<Collect>> findHotCollections(long currentUserId, int page, int size) {
        List<HotCollection> hotCols = favorService.queryHotCollects(page, size);
        List<Collect> collects = parseHotCollections(currentUserId, hotCols);
        return Response.create(collects);
    }

    public Response<List<Collect>> findRecentHotCollections(long currentUserId, int days) {
        if (days < 5) days = 5;

        List<HotCollection> hotCols = favorService.queryRecentTopHotCollects(days);
        List<Collect> collects = parseHotCollections(currentUserId, hotCols);
        return Response.create(collects);
    }

    private List<Collect> parseHotCollections(long currentUserId, List<HotCollection> hotCols) {
        List<Collect> collects = new ArrayList<>(hotCols.size());
        for (HotCollection hotCol : hotCols) {
            Collection col = colDao.findOne(hotCol.getColId());
            Collect collect = new Collect(col);

            User user = userDao.findOne(col.getUserId()).profile();
            collect.setUser(user);

            collects.add(fillCollect(collect, hotCol.getColId(), currentUserId, hotCol.getCount()));
        }
        return collects;
    }

    public Response<UserProfile> getUserProfile(long currentUserId, long targetUserId) {
        UserProfile profile = new UserProfile();
        profile.setUser(userDao.findOne(targetUserId).profile());
        profile.setFavorCount(favorDao.countByUser(targetUserId));
        profile.setShareCount(colDao.countByUser(targetUserId));
        profile.setFollowingCount(followDao.countByOwer(targetUserId));
        profile.setFollowersCount(followDao.countByFollowing(targetUserId));
        profile.setHasFollow(followService.hasFollow(currentUserId, targetUserId));

        List<HotCollection> hotCols = favorService.queryUserHotCollect(targetUserId);
        List<Collect> collects = new ArrayList<>(hotCols.size());
        for (HotCollection hotCol : hotCols) {
            Collection col = colDao.findOne(hotCol.getColId());
            Collect collect = new Collect(col);
            collect.setUser(profile.getUser());
            collects.add(fillCollect(collect, hotCol.getColId(), currentUserId, hotCol.getCount()));
        }
        profile.setHotShareCollect(collects);
        return Response.create(profile);
    }

    private List<Collect> parseCollections(long currentUserId, Page<Collection> collections) {
        return parseCollections(currentUserId, collections, collections.getContent().size());
    }

    private List<Collect> parseCollections(long currentUserId, Iterable<Collection> collections, int size) {
        List<Collect> collects = new ArrayList<>(size);
        for (Collection col : collections) {
            Collect collect = new Collect(col);

            User user = userDao.findOne(col.getUserId()).profile();
            collect.setUser(user);
            collects.add(fillCollect(collect, col.getId(), currentUserId));
        }
        return collects;
    }

    private Collect fillCollect(Collect collect, long colId, long currentUserId) {
        long favorCount = favorDao.countByCollection(colId);
        return fillCollect(collect, colId, currentUserId, favorCount);
    }

    private Collect fillCollect(Collect collect, long colId, long currentUserId, long favorCount) {
        collect.setFavorCount(favorCount);
        Favor favor = favorDao.findOneByColAndUser(colId, currentUserId);
        collect.setHasFavor(favor != null);
        long commentCount = commentDao.countByCollection(colId);
        collect.setCommentCount(commentCount);
        return collect;
    }
}
