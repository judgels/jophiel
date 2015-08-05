package org.iatoki.judgels.jophiel.models.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserForgotPasswordModel.class)
public abstract class UserForgotPasswordModel_ extends org.iatoki.judgels.play.models.entities.AbstractModel_ {

        public static volatile SingularAttribute<UserForgotPasswordModel, Long> id;
        public static volatile SingularAttribute<UserForgotPasswordModel, String> userJid;
        public static volatile SingularAttribute<UserForgotPasswordModel, String> code;
    public static volatile SingularAttribute<UserForgotPasswordModel, Boolean> used;

}
