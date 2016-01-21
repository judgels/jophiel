package org.iatoki.judgels.jophiel.user.profile.info;

import org.iatoki.judgels.play.model.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(CityModel.class)
public abstract class CityModel_ extends AbstractModel_ {
    public static volatile SingularAttribute<CityModel, Long> id;
    public static volatile SingularAttribute<CityModel, String> city;
    public static volatile SingularAttribute<CityModel, String> referenceCount;
}
