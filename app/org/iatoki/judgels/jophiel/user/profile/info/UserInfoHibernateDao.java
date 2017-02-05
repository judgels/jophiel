package org.iatoki.judgels.jophiel.user.profile.info;

import org.iatoki.judgels.play.model.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Optional;

@Singleton
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
    public Optional<UserInfoModel> findByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserInfoModel> query = cb.createQuery(UserInfoModel.class);
        Root<UserInfoModel> root = query.from(UserInfoModel.class);

        query.where(cb.equal(root.get(UserInfoModel_.userJid), userJid));

        return Optional.ofNullable(JPA.em().createQuery(query).getSingleResult());
    }
}
