package org.iatoki.judgels.jophiel;

public class UserToken {

    private String userJid;
    private String token;

    public UserToken(String userJid, String token) {
        this.userJid = userJid;
        this.token = token;
    }

    public String getUserJid() {
        return userJid;
    }

    public String getToken() {
        return token;
    }
}
