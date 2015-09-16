package org.iatoki.judgels.jophiel.models.daos.jedishibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jophiel.models.daos.CityDao;
import org.iatoki.judgels.jophiel.models.entities.CityModel;
import org.iatoki.judgels.jophiel.models.entities.CityModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("cityDao")
public final class CityJedisHibernateDao extends AbstractJedisHibernateDao<Long, CityModel> implements CityDao {

    @Inject
    public CityJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, CityModel.class);
    }

    @Override
    public boolean existsByName(String name) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<CityModel> root = query.from(CityModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(CityModel_.city), name));

        return JPA.em().createQuery(query).getSingleResult() != 0;
    }

    @Override
    public CityModel findByName(String name) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<CityModel> query = cb.createQuery(CityModel.class);
        Root<CityModel> root = query.from(CityModel.class);

        query.where(cb.equal(root.get(CityModel_.city), name));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    protected List<SingularAttribute<CityModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(CityModel_.city);
    }
}
