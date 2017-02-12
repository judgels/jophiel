package org.iatoki.judgels.jophiel.user.profile.phone;

import org.iatoki.judgels.play.model.AbstractJudgelsModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserPhoneModel.class)
public abstract class UserPhoneModel_ extends AbstractJudgelsModel_ {

    public static volatile SingularAttribute<UserPhoneModel, Long> id;
    public static volatile SingularAttribute<UserPhoneModel, String> userJid;
    public static volatile SingularAttribute<UserPhoneModel, String> phoneNumber;
    public static volatile SingularAttribute<UserPhoneModel, Boolean> phoneNumberVerified;
}
