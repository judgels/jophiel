package org.iatoki.judgels.jophiel;

public final class IdToken {

    private final long id;

    private final String code;

    private final String userJid;

    private final String clientJid;

    private final String token;

    private final boolean redeemed;

    public IdToken(long id, String code, String userJid, String clientJid, String token, boolean redeemed) {
        this.id = id;
        this.code = code;
        this.userJid = userJid;
        this.clientJid = clientJid;
        this.token = token;
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

    public boolean isRedeemed() {
        return redeemed;
    }
}
