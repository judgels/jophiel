package org.iatoki.judgels.jophiel.oauth2;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_redirect_uri")
public final class RedirectURIModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String clientJid;

    public String redirectURI;
}
