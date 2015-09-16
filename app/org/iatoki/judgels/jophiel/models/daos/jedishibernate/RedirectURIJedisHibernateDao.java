package org.iatoki.judgels.jophiel.models.daos.jedishibernate;

import org.iatoki.judgels.jophiel.models.daos.RedirectURIDao;
import org.iatoki.judgels.jophiel.models.entities.RedirectURIModel;
import org.iatoki.judgels.jophiel.models.entities.RedirectURIModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("redirectURIDao")
public final class RedirectURIJedisHibernateDao extends AbstractJedisHibernateDao<Long, RedirectURIModel> implements RedirectURIDao {

    @Inject
    public RedirectURIJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, RedirectURIModel.class);
    }

    @Override
    public List<RedirectURIModel> getByClientJid(String clientJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<RedirectURIModel> query = cb.createQuery(RedirectURIModel.class);

        Root<RedirectURIModel> root = query.from(RedirectURIModel.class);

        query.where(cb.equal(root.get(RedirectURIModel_.clientJid), clientJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
