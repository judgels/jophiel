package org.iatoki.judgels.jophiel.user.profile.email;

import org.iatoki.judgels.play.model.AbstractJudgelsModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserEmailModel.class)
public abstract class UserEmailModel_ extends AbstractJudgelsModel_ {

        public static volatile SingularAttribute<UserEmailModel, String> userJid;
        public static volatile SingularAttribute<UserEmailModel, String> email;
        public static volatile SingularAttribute<UserEmailModel, Boolean> emailVerified;
        public static volatile SingularAttribute<UserEmailModel, String> emailCode;
}
