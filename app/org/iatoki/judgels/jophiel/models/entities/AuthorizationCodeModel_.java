package org.iatoki.judgels.jophiel.models.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AuthorizationCodeModel.class)
public abstract class AuthorizationCodeModel_ extends org.iatoki.judgels.play.models.entities.AbstractModel_ {

        public static volatile SingularAttribute<AuthorizationCodeModel, Long> id;
        public static volatile SingularAttribute<AuthorizationCodeModel, String> userJid;
        public static volatile SingularAttribute<AuthorizationCodeModel, String> clientJid;
        public static volatile SingularAttribute<AuthorizationCodeModel, String> code;
        public static volatile SingularAttribute<AuthorizationCodeModel, String> redirectURI;
        public static volatile SingularAttribute<AuthorizationCodeModel, Long> expireTime;
        public static volatile SingularAttribute<AuthorizationCodeModel, String> scopes;

}