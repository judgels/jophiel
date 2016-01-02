package org.iatoki.judgels.jophiel.user.profile.email;

public final class UnverifiedUserEmail {

    private final long id;
    private final String jid;
    private final String username;
    private final String email;
    private final boolean emailVerified;

    public UnverifiedUserEmail(long id, String jid, String username, String email, boolean emailVerified) {
        this.id = id;
        this.jid = jid;
        this.username = username;
        this.email = email;
        this.emailVerified = emailVerified;
    }

    public long getId() {
        return id;
    }

    public String getJid() {
        return jid;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}
