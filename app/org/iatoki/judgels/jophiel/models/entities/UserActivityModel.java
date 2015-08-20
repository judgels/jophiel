package org.iatoki.judgels.jophiel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_user_activity")
public final class UserActivityModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String clientJid;

    public long time;

    @Column(columnDefinition = "text")
    public String log;
}
