package org.iatoki.judgels.jophiel.models.daos.hibernate;

import org.iatoki.judgels.jophiel.models.daos.UserPhoneDao;
import org.iatoki.judgels.jophiel.models.entities.UserPhoneModel;
import org.iatoki.judgels.jophiel.models.entities.UserPhoneModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("userPhoneDao")
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
}
