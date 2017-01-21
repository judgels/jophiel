package org.iatoki.judgels.jophiel.user;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_user_token")
public class UserTokenModel extends AbstractModel {

    public String userJid;

    public String token;
}
