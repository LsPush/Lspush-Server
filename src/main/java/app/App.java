/*
 * Copyright 2016 TomeOkin
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
package app;

import app.config.Crypto;
import app.config.LsPushProperties;
import app.dao.FollowDao;
import app.dao.UserDao;
import app.data.*;
import app.data.internal.HotCollection;
import app.module.collect.CollectService;
import app.module.collect.CommentService;
import app.module.collect.FavorService;
import app.module.user.FollowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Date;
import java.util.List;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(LsPushProperties.class)
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private final LsPushProperties lsPushProperties;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(App.class, args);
    }

    public App(LsPushProperties lsPushProperties) {
        this.lsPushProperties = lsPushProperties;
        initCrypt();
    }

    public void initCrypt() {
        try {
            log.info(lsPushProperties.toString());
            log.info("init crypt");
            Crypto.init(lsPushProperties.getPublicKey(), lsPushProperties.getPrivateKey());
        } catch (Exception e) {
            log.error("create Crypto instance failure", e);
        }
    }

    @Bean
    public ThreadPoolTaskExecutor executor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadGroupName("LsPush-ThreadGroup");
        executor.setThreadNamePrefix("LsPush-Thread");
        executor.setCorePoolSize(20);
        return executor;
    }

    //@Autowired
    public void testDate(ObjectMapper mapper) {
        try {
            String dateJson = "\"2017-02-07T22:44:48+0800\"";
            Date date = mapper.readValue(dateJson, Date.class);
            Response<Date> dateResponse = Response.create(date);
            String json = mapper.writeValueAsString(dateResponse);
            log.info("json date: {}", json);
        } catch (Throwable cause) {
            log.error("convert json to Date", cause);
        }
    }

    //@Autowired
    public void testUser(UserDao userDao) {
        User user = new User("123", "123456548690", "CN", "1234567.", "12");
        userDao.save(user);

        List<User> users = userDao.findAll();
        for (User one : users) {
            log.info(one.toString());
        }

        User a = userDao.findFirstByPhone("123456548690");
        log.info("User a is existed ? {}", a.toString());

        userDao.delete(a.getId());

        User one = userDao.findFirstByPhone("13108901231");
        log.info("User one is existed ? {}", one != null);
    }

    //@Autowired
    public void testComment(CommentService commentService) {
        log.info(">> testComment");
        Response<List<Comment>> response = commentService.findCommentByCollection(6, 0, 10);
        if (response.isSuccess()) {
            List<Comment> commentList = response.getData();
            for (Comment comment : commentList) {
                log.info(comment.toString());
            }
        }
    }

    //@Autowired
    public void testCollection(CollectService collectService) {
        Collect collect = new Collect();
        collect.setUrl("http");
        collect.setTitle("title");
        collect.setDescription("desc");
        collect.setImage("");
        collect.setCreateDate(new Date());
        collect.setUpdateDate(new Date());
        collect.setTags(new String[]{"123", "234", "456"});
        collectService.postCollect(2, collect);

        Collect collect1 = new Collect();
        collect1.setUrl("http");
        collect1.setTitle("title");
        collect1.setDescription("desc");
        collect1.setImage("");
        collect1.setCreateDate(new Date());
        collect1.setUpdateDate(new Date());
        collect1.setTags(new String[]{"2345"});
        collectService.postCollect(1, collect1);

        Response<List<Collect>> collectionsRes = collectService.findUserCollections(1L, 1L, 0, 10);
        List<Collect> collects = collectionsRes.getData();
        for (Collect col : collects) {
            log.info(col.toString());
        }

        List<Collect> colByDates = collectService.findNewestCollections(1, 0, 10).getData();
        for (Collect col : colByDates) {
            log.info(col.toString());
        }
    }

    //@Autowired
    public void testFavor(FavorService favorService) {
        favorService.addFavor(1, 1);
        favorService.addFavor(2, 1);
    }

    //@Autowired
    public void testHotCollection(FavorService favorService) {
        List<HotCollection> hotCollections = favorService.queryHotCollects(0, 10);
        log.info("> testHotCollection");
        for (HotCollection hc : hotCollections) {
            log.info(hc.toString());
        }
    }

    //@Autowired
    public void testFollow(FollowDao followDao) {
        log.info("user 1 关注的人数：{}", followDao.countByOwer(1L));
        log.info("关注 user 1 的人数：{}", followDao.countByFollowing(1L));
    }

    //@Autowired
    public void testUserProfile(CollectService colService) {
        Response<UserProfile> response = colService.getUserProfile(2L, 1L);
        UserProfile profile = response.getData();
        log.info("User 1 profile of User 2: {}", profile.toString());
    }

    //@Autowired
    public void testUserFavorCollections(CollectService colService) {
        Response<List<Collect>> response = colService.findUserFavorCollections(2, 1, 0, 10);
        List<Collect> collects = response.getData();
        log.info("> testUserFavorCollections");
        for (Collect col : collects) {
            log.info(col.toString());
        }
    }

    //@Autowired
    public void testFollow(FollowService followService) {
        BaseResponse response = followService.follow(2, 3);
        log.info("User 2 follow User 1 success ? {}", response.isSuccess());
    }

    //@Autowired
    public void testRecentTopCollects(CollectService colService) {
        Response<List<Collect>> response = colService.findRecentHotCollections(2, 5);
        List<Collect> collects = response.getData();
        log.info("> testRecentTopCollects");
        for (Collect col : collects) {
            log.info(col.toString());
        }
    }

    // @Autowired
    // public void testSearchOption(CollectionDao colDao) {
    //     log.info("testSearchOption");
    //     Pageable pageable = new PageRequest(0, 10, CollectService.UPDATE_SORT);
    //     Page<Collection> collections = colDao.f("tags", "Android", pageable);
    //     for (Collection col : collections) {
    //         log.info(col.toString());
    //     }
    // }

    //@Autowired
    public void testSearchCollect(CollectService colService) {
        log.info("> testSearchCollect #tag ^user");
        Response<List<Collect>> response = colService.findCollect(1, 1, "tag", "user", "Android", 0, 10);
        List<Collect> collects = response.getData();
        for (Collect col : collects) {
            log.info(col.toString());
        }

        log.info("> testSearchCollect #title ^favor");
        response = colService.findCollect(1, 2, "title", "favor", "Android", 0, 10);
        collects = response.getData();
        for (Collect col : collects) {
            log.info(col.toString());
        }

        log.info("> testSearchCollect #url ^all");
        response = colService.findCollect(2, 2, "url", "all", "https://github.com", 0, 10);
        collects = response.getData();
        for (Collect col : collects) {
            log.info(col.toString());
        }
    }
}
