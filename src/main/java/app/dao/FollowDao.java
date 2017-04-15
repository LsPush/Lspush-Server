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
import app.data.Follow;
import app.data.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface FollowDao extends CrudRepository<Follow, Long> {
    @Override
    <S extends Follow> S save(S entities);

    @Query("SELECT COUNT(*) FROM Follow follow WHERE follow.ownerId like ?1")
    long countByOwer(Long ownerId);

    @Query("SELECT COUNT(*) FROM Follow follow WHERE follow.followingId like ?1")
    long countByFollowing(Long followingId);

    @Query("SELECT follow FROM Follow follow WHERE follow.ownerId like ?1 and follow.followingId like ?2")
    Follow findOneByOwnerAndFollowing(Long ownerId, Long followingId);

    @Query("SELECT follow FROM Follow follow WHERE follow.ownerId like ?1")
    Page<Follow> findByOwner(Long uid, Pageable pageable);

    @Query("SELECT follow FROM Follow follow WHERE follow.followingId like ?1")
    Page<Follow> findByFollowing(Long uid, Pageable pageable);
}
