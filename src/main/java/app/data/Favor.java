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
public class Favor implements Serializable {
    private static final long serialVersionUID = -5369293472227615409L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private long id;

    @Column(name = "user_id") private long userId;
    @Column(name = "col_id") private long colId;

    private Date updateDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getColId() {
        return colId;
    }

    public void setColId(long colId) {
        this.colId = colId;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        return "Favor{" +
            "id=" + id +
            ", userId=" + userId +
            ", colId=" + colId +
            ", updateDate=" + updateDate +
            '}';
    }
}
