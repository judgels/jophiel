package org.iatoki.judgels.jophiel.oauth2;

import org.iatoki.judgels.play.model.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
public final class AccessTokenJedisHibernateDao extends AbstractJedisHibernateDao<Long, AccessTokenModel> implements AccessTokenDao {

    @Inject
    public AccessTokenJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, AccessTokenModel.class);
    }

    @Override
    public boolean existsValidByTokenAndTime(String token, long time) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<AccessTokenModel> root = query.from(AccessTokenModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(AccessTokenModel_.token), token), cb.gt(root.get(AccessTokenModel_.expireTime), time)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public AccessTokenModel findByCode(String code) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<AccessTokenModel> query = cb.createQuery(AccessTokenModel.class);

        Root<AccessTokenModel> root =  query.from(AccessTokenModel.class);

        query.where(cb.equal(root.get(AccessTokenModel_.code), code));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public AccessTokenModel findByToken(String token) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<AccessTokenModel> query = cb.createQuery(AccessTokenModel.class);

        Root<AccessTokenModel> root =  query.from(AccessTokenModel.class);

        query.where(cb.equal(root.get(AccessTokenModel_.token), token));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
