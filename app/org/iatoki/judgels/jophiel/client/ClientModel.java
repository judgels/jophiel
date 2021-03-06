package org.iatoki.judgels.jophiel.client;

import org.iatoki.judgels.play.jid.JidPrefix;
import org.iatoki.judgels.play.model.AbstractJudgelsModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_client")
@JidPrefix("JOCL")
public final class ClientModel extends AbstractJudgelsModel {

    public String name;

    public String secret;

    public String applicationType;

    public String scopes;

    public ClientModel() {

    }

    public ClientModel(long id, String jid, String name, String applicationType, String scopes) {
        this.id = id;
        this.jid = jid;
        this.name = name;
        this.applicationType = applicationType;
        this.scopes = scopes;
    }
}
