package org.iatoki.judgels.jophiel;

public final class AccessToken {

    private final long id;

    private final String code;

    private final String userJid;

    private final String clientJid;

    private final String token;

    private final long expireTime;

    private final boolean redeemed;

    private final String scopes;

    public AccessToken(long id, String code, String userJid, String clientJid, String token, long expireTime, boolean redeemed, String scopes) {
        this.id = id;
        this.code = code;
        this.userJid = userJid;
        this.clientJid = clientJid;
        this.token = token;
        this.expireTime = expireTime;
        this.redeemed = redeemed;
        this.scopes = scopes;
    }

    public long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getUserJid() {
        return userJid;
    }

    public String getClientJid() {
        return clientJid;
    }

    public String getToken() {
        return token;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public boolean isRedeemed() {
        return redeemed;
    }

    public String getScopes() {
        return scopes;
    }
}
