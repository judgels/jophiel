package org.iatoki.judgels.jophiel.models.daos.jedishibernate;

import org.iatoki.judgels.jophiel.models.daos.AuthorizationCodeDao;
import org.iatoki.judgels.jophiel.models.entities.AuthorizationCodeModel;
import org.iatoki.judgels.jophiel.models.entities.AuthorizationCodeModel_;
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
@Named("authorizationCodeDao")
public final class AuthorizationCodeJedisHibernateDao extends AbstractJedisHibernateDao<Long, AuthorizationCodeModel> implements AuthorizationCodeDao {

    @Inject
    public AuthorizationCodeJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, AuthorizationCodeModel.class);
    }

    @Override
    public boolean isAuthorized(String clientJid, String userJid, String scopes) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<AuthorizationCodeModel> root = query.from(AuthorizationCodeModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(AuthorizationCodeModel_.clientJid), clientJid), cb.equal(root.get(AuthorizationCodeModel_.userJid), userJid), cb.equal(root.get(AuthorizationCodeModel_.scopes), scopes)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public AuthorizationCodeModel findByCode(String code) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<AuthorizationCodeModel> query = cb.createQuery(AuthorizationCodeModel.class);

        Root<AuthorizationCodeModel> root =  query.from(AuthorizationCodeModel.class);

        query.where(cb.equal(root.get(AuthorizationCodeModel_.code), code));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
