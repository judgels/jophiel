package org.iatoki.judgels.jophiel.user;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public final class User {

    private long id;
    private String jid;
    private String username;
    private String name;
    private String emailJid;
    private Optional<String> phoneJid;
    private boolean showName;
    private URL profilePictureUrl;
    private List<String> roles;

    public User(long id, String jid, String username, String name, String emailJid, String phoneJid, boolean showName, URL profilePictureUrl, List<String> roles) {
        this.id = id;
        this.jid = jid;
        this.username = username;
        this.name = name;
        this.emailJid = emailJid;
        this.phoneJid = Optional.ofNullable(phoneJid);
        this.showName = showName;
        this.profilePictureUrl = profilePictureUrl;
        this.roles = roles;
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

    public String getName() {
        return name;
    }

    public String getEmailJid() {
        return emailJid;
    }

    public Optional<String> getPhoneJid() {
        return phoneJid;
    }

    public boolean isShowName() {
        return showName;
    }

    public URL getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public List<String> getRoles() {
        return roles;
    }
}
