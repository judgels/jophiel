package org.iatoki.judgels.jophiel.models.daos.hibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jophiel.models.daos.UserDao;
import org.iatoki.judgels.jophiel.models.entities.UserModel;
import org.iatoki.judgels.jophiel.models.entities.UserModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
@Named("userDao")
public final class UserHibernateDao extends AbstractJudgelsHibernateDao<UserModel> implements UserDao {

    public UserHibernateDao() {
        super(UserModel.class);
    }

    @Override
    public boolean existByUsername(String username) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserModel> root = query.from(UserModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(UserModel_.username), username));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<String> getJidsByUsernames(Collection<String> usernames) {
        if (usernames.isEmpty()) {
            return ImmutableList.of();
        } else {
            CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
            CriteriaQuery<String> query = cb.createQuery(String.class);
            Root<UserModel> root = query.from(UserModel.class);

            query.select(root.get(UserModel_.jid)).where(root.get(UserModel_.username).in(usernames));

            return JPA.em().createQuery(query).getResultList();
        }
    }

    @Override
    public List<String> getJidsByFilter(String filter) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<UserModel> root = query.from(UserModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(UserModel_.username), "%" + filter + "%"));
        predicates.add(cb.like(root.get(UserModel_.name), "%" + filter + "%"));
        predicates.add(cb.like(root.get(UserModel_.roles), "%" + filter + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        query.select(root.get(UserModel_.jid)).where(condition);
        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<String> getSortedJidsByOrder(Collection<String> userJids, String sortBy, String order) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<UserModel> root = query.from(UserModel.class);

        Predicate condition = root.get(UserModel_.jid).in(userJids);

        Order orderBy = null;
        if ("asc".equals(order)) {
            orderBy = cb.asc(root.get(sortBy));
        } else {
            orderBy = cb.desc(root.get(sortBy));
        }

        query.select(root.get(UserModel_.jid)).where(condition).orderBy(orderBy);
        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<UserModel> getByJids(Collection<String> userJids, long first, long max) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserModel> query = cb.createQuery(UserModel.class);
        Root<UserModel> root = query.from(UserModel.class);

        List<Selection<?>> selection = new ArrayList<>();
        selection.add(root.get(UserModel_.id));
        selection.add(root.get(UserModel_.username));
        selection.add(root.get(UserModel_.name));
        selection.add(root.get(UserModel_.profilePictureImageName));
        selection.add(root.get(UserModel_.roles));

        Predicate condition = root.get(UserModel_.jid).in(userJids);

        CriteriaBuilder.Case<Long> orderCase = cb.selectCase();
        long i = 0;
        for (String userJid : userJids) {
            orderCase = orderCase.when(cb.equal(root.get(UserModel_.jid), userJid), i);
            ++i;
        }
        Order order = cb.asc(orderCase.otherwise(i));

        query
            .multiselect(selection)
            .where(condition)
            .orderBy(order);

        List<UserModel> list = JPA.em().createQuery(query).setFirstResult((int) first).setMaxResults((int) max).getResultList();

        return list;
    }

    @Override
    public UserModel findByUsername(String username) throws NoResultException {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserModel> query = cb.createQuery(UserModel.class);
        Root<UserModel> root = query.from(UserModel.class);

        query.where(cb.equal(root.get(UserModel_.username), username));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    protected List<SingularAttribute<UserModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(UserModel_.name, UserModel_.username);
    }
}
