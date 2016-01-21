package org.iatoki.judgels.jophiel.oauth2;

import org.iatoki.judgels.play.model.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(RedirectURIModel.class)
public abstract class RedirectURIModel_ extends AbstractModel_ {

    public static volatile SingularAttribute<RedirectURIModel, Long> id;
    public static volatile SingularAttribute<RedirectURIModel, String> clientJid;
    public static volatile SingularAttribute<RedirectURIModel, String> redirectURI;
}
