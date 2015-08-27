package org.iatoki.judgels.jophiel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_user_info")
public final class UserInfoModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String userJid;

    @Column(columnDefinition = "TEXT")
    public String gender;

    public long birthDate;

    @Column(columnDefinition = "TEXT")
    public String streetAddress;

    public int postalCode;

    @Column(columnDefinition = "TEXT")
    public String institution;

    public String city;

    public String provinceOrState;

    public String country;

    public String shirtSize;
}
