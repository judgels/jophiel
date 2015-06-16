package org.iatoki.judgels.jophiel.models.daos.impls;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.RedirectURIDao;
import org.iatoki.judgels.jophiel.models.entities.RedirectURIModel;
import org.iatoki.judgels.jophiel.models.entities.RedirectURIModel_;
import org.springframework.stereotype.Repository;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository("redirectURIDao")
public final class RedirectURIHibernateDao extends AbstractHibernateDao<Long, RedirectURIModel> implements RedirectURIDao {

    public RedirectURIHibernateDao() {
        super(RedirectURIModel.class);
    }

    @Override
    public List<RedirectURIModel> findByClientJid(String clientJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<RedirectURIModel> query = cb.createQuery(RedirectURIModel.class);

        Root<RedirectURIModel> root = query.from(RedirectURIModel.class);

        query.where(cb.equal(root.get(RedirectURIModel_.clientJid), clientJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
