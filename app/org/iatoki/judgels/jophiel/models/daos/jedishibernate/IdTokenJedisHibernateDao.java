package org.iatoki.judgels.jophiel.models.daos.jedishibernate;

import org.iatoki.judgels.jophiel.models.daos.IdTokenDao;
import org.iatoki.judgels.jophiel.models.entities.IdTokenModel;
import org.iatoki.judgels.jophiel.models.entities.IdTokenModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("idTokenDao")
public final class IdTokenJedisHibernateDao extends AbstractJedisHibernateDao<Long, IdTokenModel> implements IdTokenDao {

    @Inject
    public IdTokenJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, IdTokenModel.class);
    }

    @Override
    public IdTokenModel findByCode(String code) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<IdTokenModel> query = cb.createQuery(IdTokenModel.class);

        Root<IdTokenModel> root =  query.from(IdTokenModel.class);

        query.where(cb.equal(root.get(IdTokenModel_.code), code));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
