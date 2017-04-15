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
package app.data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class Follow implements Serializable {
    private static final long serialVersionUID = 8513535436502397117L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private long id;

    @Column(name = "owner_id") private long ownerId;
    @Column(name = "following_id") private long followingId;

    private Date updateDate;

    public Follow() {}

    public Follow(long ownerId, long followingId) {
        this.ownerId = ownerId;
        this.followingId = followingId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public long getFollowingId() {
        return followingId;
    }

    public void setFollowingId(long followingId) {
        this.followingId = followingId;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        return "Follow{" +
            "id=" + id +
            ", ownerId=" + ownerId +
            ", followingId=" + followingId +
            ", updateDate=" + updateDate +
            '}';
    }
}
