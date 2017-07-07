package org.iatoki.judgels.jophiel.user.profile.phone;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.model.AbstractJudgelsHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;

@Singleton
public final class UserPhoneHibernateDao extends AbstractJudgelsHibernateDao<UserPhoneModel> implements UserPhoneDao {

    public UserPhoneHibernateDao() {
        super(UserPhoneModel.class);
    }

    @Override
    public List<UserPhoneModel> getByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserPhoneModel> query = cb.createQuery(UserPhoneModel.class);
        Root<UserPhoneModel> root = query.from(UserPhoneModel.class);

        query.where(cb.equal(root.get(UserPhoneModel_.userJid), userJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<UserPhoneModel> getByUserJids(Collection<String> userJids) {
        if (userJids.isEmpty()) {
            return ImmutableList.of();
        }

        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserPhoneModel> query = cb.createQuery(UserPhoneModel.class);
        Root<UserPhoneModel> root = query.from(UserPhoneModel.class);

        Predicate condition = root.get(UserPhoneModel_.userJid).in(userJids);

        CriteriaBuilder.Case<Long> orderCase = cb.selectCase();
        long i = 0;
        for (String userJid : userJids) {
            orderCase = orderCase.when(cb.equal(root.get(UserPhoneModel_.userJid), userJid), i);
            ++i;
        }
        Order order = cb.asc(orderCase.otherwise(i));

        query
                .where(condition)
                .orderBy(order);

        return JPA.em().createQuery(query).getResultList();
    }
}
