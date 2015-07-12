package org.iatoki.judgels.jophiel.models.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserActivityModel.class)
public abstract class UserActivityModel_ extends org.iatoki.judgels.play.models.domains.AbstractModel_ {

        public static volatile SingularAttribute<UserActivityModel, Long> id;
        public static volatile SingularAttribute<UserActivityModel, String> clientJid;
        public static volatile SingularAttribute<UserActivityModel, String> time;
        public static volatile SingularAttribute<UserActivityModel, String> log;

}
