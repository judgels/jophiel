package org.iatoki.judgels.jophiel.user.profile.email;

public final class UserEmail {

    private final long id;
    private final String jid;
    private final String userJid;
    private final String email;
    private final boolean emailVerified;

    public UserEmail(long id, String jid, String userJid, String email, boolean emailVerified) {
        this.id = id;
        this.jid = jid;
        this.userJid = userJid;
        this.email = email;
        this.emailVerified = emailVerified;
    }

    public long getId() {
        return id;
    }

    public String getJid() {
        return jid;
    }

    public String getUserJid() {
        return userJid;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}
