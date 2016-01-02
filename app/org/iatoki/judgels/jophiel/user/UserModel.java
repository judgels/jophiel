package org.iatoki.judgels.jophiel.user;

import org.iatoki.judgels.play.jid.JidPrefix;
import org.iatoki.judgels.play.model.AbstractJudgelsModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_user")
@JidPrefix("USER")
public final class UserModel extends AbstractJudgelsModel {

    @Column(unique = true)
    public String username;

    public String name;

    public String password;

    public String profilePictureImageName;

    public String emailJid;

    public String phoneJid;

    public boolean showName;

    public String roles;

    public UserModel() {

    }

    public UserModel(String username, String name) {
        this.username = username;
        this.name = name;
    }

    public UserModel(long id, String username, String name, String profilePictureImageName, String roles) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.profilePictureImageName = profilePictureImageName;
        this.roles = roles;
    }
}
