package app.data;

import app.config.CryptoToken;

import java.util.Date;

public class AccessBundle {
    // 提供 user 供后续使用
    private User user;

    private Date expireTime;
    private CryptoToken expireToken;

    public AccessBundle() {}

    public AccessBundle(User user, Date expireTime, CryptoToken expireToken) {
        this.user = user;
        this.expireTime = expireTime;
        this.expireToken = expireToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public CryptoToken getExpireToken() {
        return expireToken;
    }

    public void updateExpireToken(Date expireTime, CryptoToken expireToken) {
        this.expireTime = expireTime;
        this.expireToken = expireToken;
    }
}