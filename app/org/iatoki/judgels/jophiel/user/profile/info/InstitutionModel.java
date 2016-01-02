package org.iatoki.judgels.jophiel.user.profile.info;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_institution")
public final class InstitutionModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    @Column(columnDefinition = "TEXT", unique = true)
    public String institution;

    public int referenceCount;
}
