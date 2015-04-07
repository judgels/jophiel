package org.iatoki.judgels.jophiel.commons;

public final class UserActivity {

    private long id;

    private long time;

    private String userJid;

    private String clientJid;

    private String log;

    private String ipAddress;

    public UserActivity(long time, String userJid, String log, String ipAddress) {
        this.time = time;
        this.userJid = userJid;
        this.log = log;
        this.ipAddress = ipAddress;
    }

    public UserActivity(long id, long time, String userJid, String clientJid, String log, String ipAddress) {
        this.id = id;
        this.time = time;
        this.userJid = userJid;
        this.clientJid = clientJid;
        this.log = log;
        this.ipAddress = ipAddress;
    }

    public long getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public String getUserJid() {
        return userJid;
    }

    public String getClientJid() {
        return clientJid;
    }

    public String getLog() {
        return log;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
