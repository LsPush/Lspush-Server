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

import app.data.Favor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface FavorDao extends CrudRepository<Favor, Long> {
    @Override
    <S extends Favor> S save(S entities);

    @Query("SELECT COUNT(*) FROM Favor favor WHERE favor.colId like ?1")
    long countByCollection(Long colId);

    @Query("SELECT COUNT(*) FROM Favor favor WHERE favor.userId like ?1")
    long countByUser(Long userId);

    @Query("SELECT favor FROM Favor favor WHERE favor.colId like ?1 and favor.userId like ?2")
    Favor findOneByColAndUser(Long colId, Long uid);

    @Query("SELECT favor FROM Favor favor WHERE favor.userId like ?1")
    Page<Favor> findByUser(Long uid, Pageable pageable);
}
