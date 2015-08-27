package org.iatoki.judgels.jophiel.models.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserModel.class)
public abstract class UserModel_ extends org.iatoki.judgels.play.models.entities.AbstractJudgelsModel_ {

        public static volatile SingularAttribute<UserModel, String> name;
        public static volatile SingularAttribute<UserModel, String> username;
        public static volatile SingularAttribute<UserModel, String> password;
        public static volatile SingularAttribute<UserModel, String> emailJid;
        public static volatile SingularAttribute<UserModel, String> phoneJid;
        public static volatile SingularAttribute<UserModel, Boolean> showName;
        public static volatile SingularAttribute<UserModel, String> roles;
        public static volatile SingularAttribute<UserModel, String> profilePictureImageName;
}
