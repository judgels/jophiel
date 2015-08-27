package org.iatoki.judgels.jophiel.models.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserInfoModel.class)
public abstract class UserInfoModel_ extends org.iatoki.judgels.play.models.entities.AbstractModel_ {

        public static volatile SingularAttribute<UserInfoModel, Long> id;
        public static volatile SingularAttribute<UserInfoModel, String> userJid;
        public static volatile SingularAttribute<UserInfoModel, String> gender;
        public static volatile SingularAttribute<UserInfoModel, String> birthPlace;
        public static volatile SingularAttribute<UserInfoModel, Long> birthDate;
        public static volatile SingularAttribute<UserInfoModel, String> streetAddress;
        public static volatile SingularAttribute<UserInfoModel, Integer> postalCode;
        public static volatile SingularAttribute<UserInfoModel, String> institution;
        public static volatile SingularAttribute<UserInfoModel, String> city;
        public static volatile SingularAttribute<UserInfoModel, String> provinceOrState;
        public static volatile SingularAttribute<UserInfoModel, String> country;
        public static volatile SingularAttribute<UserInfoModel, String> shirtSize;
}
