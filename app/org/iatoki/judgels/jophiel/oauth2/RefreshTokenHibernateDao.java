package org.iatoki.judgels.jophiel.oauth2;

import org.iatoki.judgels.play.model.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
public final class RefreshTokenHibernateDao extends AbstractHibernateDao<Long, RefreshTokenModel> implements RefreshTokenDao {

    public RefreshTokenHibernateDao() {
        super(RefreshTokenModel.class);
    }

    @Override
    public RefreshTokenModel findByCode(String code) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<RefreshTokenModel> query = cb.createQuery(RefreshTokenModel.class);

        Root<RefreshTokenModel> root =  query.from(RefreshTokenModel.class);

        query.where(cb.equal(root.get(RefreshTokenModel_.code), code));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public RefreshTokenModel findByToken(String token) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<RefreshTokenModel> query = cb.createQuery(RefreshTokenModel.class);

        Root<RefreshTokenModel> root =  query.from(RefreshTokenModel.class);

        query.where(cb.equal(root.get(RefreshTokenModel_.token), token));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
