package org.iatoki.judgels.jophiel.user.profile.phone;

import org.iatoki.judgels.play.model.AbstractJudgelsJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
public final class UserPhoneJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<UserPhoneModel> implements UserPhoneDao {

    @Inject
    public UserPhoneJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, UserPhoneModel.class);
    }

    @Override
    public List<UserPhoneModel> getByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserPhoneModel> query = cb.createQuery(UserPhoneModel.class);
        Root<UserPhoneModel> root = query.from(UserPhoneModel.class);

        query.where(cb.equal(root.get(UserPhoneModel_.userJid), userJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
