package org.iatoki.judgels.jophiel.user;

import org.iatoki.judgels.play.model.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserTokenModel.class)
public class UserTokenModel_ extends AbstractModel_ {

    public static volatile SingularAttribute<UserTokenModel, String> userJid;
    public static volatile SingularAttribute<UserTokenModel, String> token;
}
