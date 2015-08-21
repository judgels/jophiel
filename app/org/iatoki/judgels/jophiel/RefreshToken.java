package org.iatoki.judgels.jophiel;

public final class RefreshToken {

    private final long id;

    private final String code;

    private final String userJid;

    private final String clientJid;

    private final String token;

    private final String scopes;

    private final boolean redeemed;

    public RefreshToken(long id, String code, String userJid, String clientJid, String token, String scopes, boolean redeemed) {
        this.id = id;
        this.code = code;
        this.userJid = userJid;
        this.clientJid = clientJid;
        this.token = token;
        this.scopes = scopes;
        this.redeemed = redeemed;
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

    public String getScopes() {
        return scopes;
    }

    public boolean isRedeemed() {
        return redeemed;
    }
}
