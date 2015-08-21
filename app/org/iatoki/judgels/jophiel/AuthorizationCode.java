package org.iatoki.judgels.jophiel;

public final class AuthorizationCode {

    private final long id;

    private final String userJid;

    private final String clientJid;

    private final String code;

    private final String redirectURI;

    private final long expireTime;

    private final String scopes;

    public AuthorizationCode(long id, String userJid, String clientJid, String code, String redirectURI, long expireTime, String scopes) {
        this.id = id;
        this.userJid = userJid;
        this.clientJid = clientJid;
        this.code = code;
        this.redirectURI = redirectURI;
        this.expireTime = expireTime;
        this.scopes = scopes;
    }

    public long getId() {
        return id;
    }

    public String getUserJid() {
        return userJid;
    }

    public String getClientJid() {
        return clientJid;
    }

    public String getCode() {
        return code;
    }

    public String getRedirectURI() {
        return redirectURI;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public String getScopes() {
        return scopes;
    }

    public boolean isExpired() {
        return (expireTime < System.currentTimeMillis());
    }
}
