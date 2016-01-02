package org.iatoki.judgels.jophiel.user.account;

import org.iatoki.judgels.play.model.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserForgotPasswordModel.class)
public abstract class UserForgotPasswordModel_ extends AbstractModel_ {

        public static volatile SingularAttribute<UserForgotPasswordModel, Long> id;
        public static volatile SingularAttribute<UserForgotPasswordModel, String> userJid;
        public static volatile SingularAttribute<UserForgotPasswordModel, String> code;
        public static volatile SingularAttribute<UserForgotPasswordModel, Boolean> used;
}
