package org.iatoki.judgels.jophiel.models.daos.impls;

import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.AccessTokenDao;
import org.iatoki.judgels.jophiel.models.entities.AccessTokenModel;
import org.iatoki.judgels.jophiel.models.entities.AccessTokenModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("accessTokenDao")
public final class AccessTokenHibernateDao extends AbstractHibernateDao<Long, AccessTokenModel> implements AccessTokenDao {

    public AccessTokenHibernateDao() {
        super(AccessTokenModel.class);
    }

    @Override
    public boolean existsByToken(String token) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<AccessTokenModel> root = query.from(AccessTokenModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(AccessTokenModel_.token), token));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public AccessTokenModel findByCode(String code) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<AccessTokenModel> query = cb.createQuery(AccessTokenModel.class);

        Root<AccessTokenModel> root =  query.from(AccessTokenModel.class);

        query.where(cb.equal(root.get(AccessTokenModel_.code), code));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public AccessTokenModel findByToken(String token) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<AccessTokenModel> query = cb.createQuery(AccessTokenModel.class);

        Root<AccessTokenModel> root =  query.from(AccessTokenModel.class);

        query.where(cb.equal(root.get(AccessTokenModel_.token), token));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
