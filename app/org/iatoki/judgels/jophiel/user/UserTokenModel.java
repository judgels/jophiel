package org.iatoki.judgels.jophiel.user;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.*;

@Entity
@Table(name = "jophiel_user_token")
public class UserTokenModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    @Column(unique = true)
    public String userJid;

    public String token;
}
