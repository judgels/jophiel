package org.iatoki.judgels.jophiel.user.profile.phone;

import org.iatoki.judgels.play.jid.JidPrefix;
import org.iatoki.judgels.play.model.AbstractJudgelsModel;

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
