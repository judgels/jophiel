package org.iatoki.judgels.jophiel.oauth2;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(IdTokenModel.class)
public abstract class IdTokenModel_ extends org.iatoki.judgels.play.models.entities.AbstractModel_ {

        public static volatile SingularAttribute<IdTokenModel, Long> id;
        public static volatile SingularAttribute<IdTokenModel, String> code;
        public static volatile SingularAttribute<IdTokenModel, String> userJid;
        public static volatile SingularAttribute<IdTokenModel, String> clientJid;
        public static volatile SingularAttribute<IdTokenModel, String> token;
        public static volatile SingularAttribute<IdTokenModel, Boolean> redeemed;
}
