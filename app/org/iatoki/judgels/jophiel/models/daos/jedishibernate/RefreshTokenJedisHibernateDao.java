package org.iatoki.judgels.jophiel.models.daos.jedishibernate;

import org.iatoki.judgels.jophiel.models.daos.RefreshTokenDao;
import org.iatoki.judgels.jophiel.models.entities.RefreshTokenModel;
import org.iatoki.judgels.jophiel.models.entities.RefreshTokenModel_;
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
@Named("refreshTokenDao")
public final class RefreshTokenJedisHibernateDao extends AbstractJedisHibernateDao<Long, RefreshTokenModel> implements RefreshTokenDao {

    @Inject
    public RefreshTokenJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, RefreshTokenModel.class);
    }

    @Override
    public RefreshTokenModel findByCode(String code) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<RefreshTokenModel> query = cb.createQuery(RefreshTokenModel.class);

        Root<RefreshTokenModel> root =  query.from(RefreshTokenModel.class);

        query.where(cb.equal(root.get(RefreshTokenModel_.code), code));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public RefreshTokenModel findByToken(String token) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<RefreshTokenModel> query = cb.createQuery(RefreshTokenModel.class);

        Root<RefreshTokenModel> root =  query.from(RefreshTokenModel.class);

        query.where(cb.equal(root.get(RefreshTokenModel_.token), token));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
