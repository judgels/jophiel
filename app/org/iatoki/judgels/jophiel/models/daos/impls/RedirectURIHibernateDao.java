package org.iatoki.judgels.jophiel.models.daos.impls;

import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.RedirectURIDao;
import org.iatoki.judgels.jophiel.models.entities.RedirectURIModel;
import org.iatoki.judgels.jophiel.models.entities.RedirectURIModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("redirectURIDao")
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
