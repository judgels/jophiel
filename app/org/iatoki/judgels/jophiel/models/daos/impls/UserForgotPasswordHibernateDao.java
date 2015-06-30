package org.iatoki.judgels.jophiel.models.daos.impls;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.UserForgotPasswordDao;
import org.iatoki.judgels.jophiel.models.entities.UserForgotPasswordModel;
import org.iatoki.judgels.jophiel.models.entities.UserForgotPasswordModel_;
import org.springframework.stereotype.Repository;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.concurrent.TimeUnit;

@Singleton
@Named("userForgotPasswordDao")
public final class UserForgotPasswordHibernateDao extends AbstractHibernateDao<Long, UserForgotPasswordModel> implements UserForgotPasswordDao {

    public UserForgotPasswordHibernateDao() {
        super(UserForgotPasswordModel.class);
    }

    @Override
    public boolean isCodeValid(String forgotPasswordCode, long currentMillis) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserForgotPasswordModel> root = query.from(UserForgotPasswordModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(UserForgotPasswordModel_.code), forgotPasswordCode), cb.equal(root.get(UserForgotPasswordModel_.used), false), cb.ge(root.get(UserForgotPasswordModel_.timeCreate), currentMillis - (TimeUnit.MILLISECONDS.convert(20, TimeUnit.MINUTES)))));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public UserForgotPasswordModel findByCode(String forgotPasswordCode) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserForgotPasswordModel> query = cb.createQuery(UserForgotPasswordModel.class);
        Root<UserForgotPasswordModel> root = query.from(UserForgotPasswordModel.class);

        query.where(cb.and(cb.equal(root.get(UserForgotPasswordModel_.code), forgotPasswordCode), cb.equal(root.get(UserForgotPasswordModel_.used), false)));

        return JPA.em().createQuery(query).getSingleResult();
    }

}
