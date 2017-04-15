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

import app.utils.TextUtils;

import java.util.Arrays;
import java.util.Date;

public class Collect {
    private long id;
    private User user;
    private String url;
    private String title;
    private String description;
    private String image;
    private Date createDate;
    private Date updateDate;
    private String[] tags;
    private long favorCount;
    private boolean hasFavor;
    private long commentCount;

    public Collect() {}

    public Collect(Collection col) {
        id = col.getId();
        url = col.getUrl();
        title = col.getTitle();
        description = col.getDescription();
        image = col.getImage();
        tags = TextUtils.split("\\|", col.getTags());
        createDate = col.getCreateDate();
        updateDate = col.getUpdateDate();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public long getFavorCount() {
        return favorCount;
    }

    public void setFavorCount(long favorCount) {
        this.favorCount = favorCount;
    }

    public boolean isHasFavor() {
        return hasFavor;
    }

    public void setHasFavor(boolean hasFavor) {
        this.hasFavor = hasFavor;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    @Override
    public String toString() {
        return "Collect{" +
            "id=" + id +
            ", user=" + user +
            ", url='" + url + '\'' +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", image='" + image + '\'' +
            ", createDate=" + createDate +
            ", updateDate=" + updateDate +
            ", tags=" + Arrays.toString(tags) +
            ", favorCount=" + favorCount +
            ", hasFavor=" + hasFavor +
            ", commentCount=" + commentCount +
            '}';
    }
}
