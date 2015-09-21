package org.iatoki.judgels.jophiel.models.entities;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.entities.AbstractJudgelsModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_user_phone")
@JidPrefix("USEP")
public final class UserPhoneModel extends AbstractJudgelsModel {

    public String userJid;

    public String phoneNumber;

    public boolean phoneNumberVerified;
}
