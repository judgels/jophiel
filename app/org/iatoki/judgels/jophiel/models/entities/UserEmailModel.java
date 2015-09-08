package org.iatoki.judgels.jophiel.models.entities;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.entities.AbstractJudgelsModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_user_email")
@JidPrefix("USEE")
public final class UserEmailModel extends AbstractJudgelsModel {

    public String userJid;

    @Column(unique = true)
    public String email;

    public boolean emailVerified;

    public String emailCode;

    @Override
    public String toString() {
        return email;
    }
}
