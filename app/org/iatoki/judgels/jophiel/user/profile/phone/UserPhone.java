package org.iatoki.judgels.jophiel.user.profile.phone;

public final class UserPhone {

    private final long id;
    private final String jid;
    private final String userJid;
    private final String phone;
    private final boolean phoneVerified;

    public UserPhone(long id, String jid, String userJid, String phone, boolean phoneVerified) {
        this.id = id;
        this.jid = jid;
        this.userJid = userJid;
        this.phone = phone;
        this.phoneVerified = phoneVerified;
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

    public String getPhone() {
        return phone;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }
}
