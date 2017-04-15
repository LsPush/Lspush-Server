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
import app.dao.FavorDao;
import app.data.BaseResponse;
import app.data.Collection;
import app.data.Favor;
import app.data.internal.HotCollection;
import app.utils.DateTimeUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FavorService {
    private static final Logger log = LoggerFactory.getLogger(FavorService.class);
    private final FavorDao favorDao;
    private final CollectionDao colDao;
    private final JdbcTemplate jdbcTemplate;

    private static final String SQL_HOT_COLLECTION = "SELECT DISTINCT col_id, count(*) AS count\n"
        + "FROM favor, collection\n"
        + "WHERE favor.col_id = collection.id\n"
        + "GROUP BY col_id\n"
        + "ORDER BY count DESC, collection.update_date DESC, col_id DESC\n"
        + "LIMIT %d, %d";

    private static final String SQL_RECENT_HOT_COLLECTION = "SELECT DISTINCT col_id, count(*) AS count\n"
        + "FROM favor, collection\n"
        + "WHERE favor.col_id = collection.id && collection.update_date >= \"%s\"\n"
        + "GROUP BY col_id\n"
        + "ORDER BY count DESC, collection.update_date DESC, col_id DESC\n"
        + "LIMIT 5";

    private static final String SQL_USER_HOT_COLLECTION = "SELECT DISTINCT col_id, count(*) AS count\n"
        + "FROM favor, collection\n"
        + "WHERE favor.col_id = collection.id && collection.user_id = %d\n"
        + "GROUP BY col_id\n"
        + "ORDER BY count DESC, collection.update_date DESC, col_id DESC\n"
        + "LIMIT 5";

    public FavorService(FavorDao favorDao, CollectionDao colDao, JdbcTemplate jdbcTemplate) {
        this.favorDao = favorDao;
        this.colDao = colDao;
        this.jdbcTemplate = jdbcTemplate;
    }

    public BaseResponse addFavor(long userId, long colId) {
        Collection collection = colDao.findOne(colId);
        if (collection == null) {
            return ErrorCode.error(ErrorCode.COLLECTION_NOT_EXISTED);
        }

        Favor favor = new Favor();
        favor.setUserId(userId);
        favor.setColId(colId);
        Favor result = favorDao.findOneByColAndUser(colId, userId);
        if (result != null) {
            favor.setId(result.getId());
        }
        favor.setUpdateDate(DateTime.now().toDate());
        favorDao.save(favor);
        return BaseResponse.SUCCESS;
    }

    public BaseResponse removeFavor(long userId, long colId) {
        Collection collection = colDao.findOne(colId);
        if (collection == null) {
            return ErrorCode.error(ErrorCode.COLLECTION_NOT_EXISTED);
        }

        Favor result = favorDao.findOneByColAndUser(colId, userId);
        if (result != null) {
            favorDao.delete(result.getId());
        }
        return BaseResponse.SUCCESS;
    }

    public List<HotCollection> queryHotCollects(int page, int size) {
        final String hotCollects = String.format(SQL_HOT_COLLECTION, page, size);
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(hotCollects);
        return mappingHotCollect(mapList);
    }

    public List<HotCollection> queryRecentTopHotCollects(int days) {
        String recent = DateTimeUtils.toDateTime(DateTime.now().minusDays(days));
        log.info("queryRecentTopHotCollects before: {}", recent);
        final String hotCollects = String.format(SQL_RECENT_HOT_COLLECTION, recent);
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(hotCollects);
        return mappingHotCollect(mapList);
    }

    public List<HotCollection> queryUserHotCollect(long userId) {
        final String userHotCollect = String.format(SQL_USER_HOT_COLLECTION, userId);
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(userHotCollect);
        return mappingHotCollect(mapList);
    }

    public List<HotCollection> mappingHotCollect(@Nonnull List<Map<String, Object>> mapList) {
        List<HotCollection> hotCollections = new ArrayList<>(mapList.size());
        for (Map<String, Object> item : mapList) {
            HotCollection hotCol = new HotCollection();
            hotCol.setColId((Long) item.get("col_id"));
            hotCol.setCount((Long) item.get("count"));
            hotCollections.add(hotCol);
        }
        return hotCollections;
    }
}
