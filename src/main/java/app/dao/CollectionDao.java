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
package app.dao;

import app.data.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CollectionDao extends CrudRepository<Collection, Long> {
    @Override
    <S extends Collection> S save(S entity);

    @Override
    Collection findOne(Long id);

    @Query("SELECT col FROM Collection col WHERE col.userId like ?1")
    Page<Collection> findByUser(Long uid, Pageable pageable);

    @Query("SELECT col FROM Collection col WHERE col.tags like %?1%")
    Page<Collection> findByTag(String tag, Pageable pageable);

    @Query("SELECT col FROM Collection col WHERE col.tags like %?1% AND col.userId like ?2")
    Page<Collection> findByTagAndUser(String keyword, Long userId, Pageable pageable);

    @Query("SELECT col FROM Collection col WHERE col.title like %?1%")
    Page<Collection> findByTitle(String title, Pageable pageable);

    @Query("SELECT col FROM Collection col WHERE col.title like %?1% AND col.userId like ?2")
    Page<Collection> findByTitleAndUser(String keyword, Long userId, Pageable pageable);

    @Query("SELECT col FROM Collection col WHERE col.url like %?1%")
    Page<Collection> findByUrl(String url, Pageable pageable);

    @Query("SELECT col FROM Collection col WHERE col.url like %?1% AND col.userId like ?2")
    Page<Collection> findByUrlAndUser(String keyword, Long userId, Pageable pageable);

    Page<Collection> findAll(Pageable pageable);

    @Query("SELECT COUNT(*) FROM Collection col WHERE col.userId like ?1")
    long countByUser(Long userId);
}
