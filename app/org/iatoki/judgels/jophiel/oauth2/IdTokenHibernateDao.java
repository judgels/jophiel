package org.iatoki.judgels.jophiel.oauth2;

import org.iatoki.judgels.play.model.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
public final class IdTokenHibernateDao extends AbstractHibernateDao<Long, IdTokenModel> implements IdTokenDao {

    public IdTokenHibernateDao() {
        super(IdTokenModel.class);
    }

    @Override
    public IdTokenModel findByCode(String code) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<IdTokenModel> query = cb.createQuery(IdTokenModel.class);

        Root<IdTokenModel> root =  query.from(IdTokenModel.class);

        query.where(cb.equal(root.get(IdTokenModel_.code), code));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
