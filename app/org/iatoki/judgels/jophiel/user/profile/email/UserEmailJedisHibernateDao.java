package org.iatoki.judgels.jophiel.user.profile.email;

import org.iatoki.judgels.play.model.AbstractJudgelsJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Singleton
public final class UserEmailJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<UserEmailModel> implements UserEmailDao {

    @Inject
    public UserEmailJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, UserEmailModel.class);
    }

    @Override
    public boolean existsByEmail(String email) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(UserEmailModel_.email), email));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public boolean existsVerifiedEmail(String email) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(UserEmailModel_.email), email), cb.equal(root.get(UserEmailModel_.emailVerified), true)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public boolean existsUnverifiedEmailByJid(String jid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(UserEmailModel_.jid), jid), cb.equal(root.get(UserEmailModel_.emailVerified), false)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public boolean existsByEmailCode(String emailCode) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(UserEmailModel_.emailCode), emailCode));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<UserEmailModel> getByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserEmailModel> query = cb.createQuery(UserEmailModel.class);

        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.where(cb.equal(root.get(UserEmailModel_.userJid), userJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<String> getUserJidsByFilter(String filter) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(UserEmailModel_.email), "%" + filter + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        query.select(root.get(UserEmailModel_.userJid)).where(condition);
        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<String> getUserJidsWithUnverifiedEmail() {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.select(root.get(UserEmailModel_.userJid)).where(cb.equal(root.get(UserEmailModel_.emailVerified), false));
        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<String> getSortedUserJidsByEmail(Collection<String> userJids, String sortBy, String order) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        Predicate condition = root.get(UserEmailModel_.userJid).in(userJids);

        Order orderBy = null;
        if ("asc".equals(order)) {
            orderBy = cb.asc(root.get(sortBy));
        } else {
            orderBy = cb.desc(root.get(sortBy));
        }

        query.select(root.get(UserEmailModel_.userJid)).where(condition).orderBy(orderBy);
        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<UserEmailModel> getByUserJids(Collection<String> userJids, long first, long max) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserEmailModel> query = cb.createQuery(UserEmailModel.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        Predicate condition = root.get(UserEmailModel_.userJid).in(userJids);

        CriteriaBuilder.Case<Long> orderCase = cb.selectCase();
        long i = 0;
        for (String userJid : userJids) {
            orderCase = orderCase.when(cb.equal(root.get(UserEmailModel_.userJid), userJid), i);
            ++i;
        }
        Order order = cb.asc(orderCase.otherwise(i));

        query
            .where(condition)
            .orderBy(order);

        List<UserEmailModel> list = JPA.em().createQuery(query).setFirstResult((int) first).setMaxResults((int) max).getResultList();

        return list;
    }

    @Override
    public Optional<UserEmailModel> findByEmail(String email) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserEmailModel> query = cb.createQuery(UserEmailModel.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.where(cb.equal(root.get(UserEmailModel_.email), email));

        try {
            return Optional.ofNullable(JPA.em().createQuery(query).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserEmailModel> findByEmailCode(String emailCode) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserEmailModel> query = cb.createQuery(UserEmailModel.class);
        Root<UserEmailModel> root = query.from(UserEmailModel.class);

        query.where(cb.equal(root.get(UserEmailModel_.emailCode), emailCode));

        try {
            return Optional.ofNullable(JPA.em().createQuery(query).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
