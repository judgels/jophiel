package org.iatoki.judgels.jophiel.oauth2;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_authorization_code")
public final class AuthorizationCodeModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String userJid;

    public String clientJid;

    public String code;

    public String redirectURI;

    public long expireTime;

    public String scopes;
}
