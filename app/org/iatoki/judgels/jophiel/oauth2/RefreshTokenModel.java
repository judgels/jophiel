package org.iatoki.judgels.jophiel.oauth2;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_refresh_token")
public final class RefreshTokenModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String code;

    public String userJid;

    public String clientJid;

    public String token;

    public boolean redeemed;

    public String scopes;
}
