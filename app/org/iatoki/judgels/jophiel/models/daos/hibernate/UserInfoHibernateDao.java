package org.iatoki.judgels.jophiel.models.daos.hibernate;

import org.iatoki.judgels.jophiel.models.daos.UserInfoDao;
import org.iatoki.judgels.jophiel.models.entities.UserInfoModel;
import org.iatoki.judgels.jophiel.models.entities.UserInfoModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("userInfoDao")
public final class UserInfoHibernateDao extends AbstractHibernateDao<Long, UserInfoModel> implements UserInfoDao {

    public UserInfoHibernateDao() {
        super(UserInfoModel.class);
    }

    @Override
    public boolean existsByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserInfoModel> root = query.from(UserInfoModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(UserInfoModel_.userJid), userJid));

        return JPA.em().createQuery(query).getSingleResult() != 0;
    }

    @Override
    public UserInfoModel findByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserInfoModel> query = cb.createQuery(UserInfoModel.class);
        Root<UserInfoModel> root = query.from(UserInfoModel.class);

        query.where(cb.equal(root.get(UserInfoModel_.userJid), userJid));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
