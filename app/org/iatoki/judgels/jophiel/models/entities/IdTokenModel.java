package org.iatoki.judgels.jophiel.models.entities;

import org.iatoki.judgels.play.models.domains.AbstractModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_id_token")
public final class IdTokenModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String code;

    public String userJid;

    public String clientJid;

    @Column(columnDefinition = "text")
    public String token;

    public boolean redeemed;

    public IdTokenModel() {

    }

}
