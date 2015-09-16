package org.iatoki.judgels.jophiel.models.daos.hibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jophiel.models.daos.ClientDao;
import org.iatoki.judgels.jophiel.models.entities.ClientModel;
import org.iatoki.judgels.jophiel.models.entities.ClientModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.List;

@Singleton
@Named("clientDao")
public final class ClientHibernateDao extends AbstractJudgelsHibernateDao<ClientModel> implements ClientDao {

    public ClientHibernateDao() {
        super(ClientModel.class);
    }

    @Override
    public boolean existsByName(String name) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ClientModel> root = query.from(ClientModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(ClientModel_.name), name));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<String> getJidsByNames(Collection<String> names) {
        if (names.isEmpty()) {
            return ImmutableList.of();
        } else {
            CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
            CriteriaQuery<String> query = cb.createQuery(String.class);
            Root<ClientModel> root = query.from(ClientModel.class);

            query.select(root.get(ClientModel_.jid)).where(root.get(ClientModel_.name).in(names));

            return JPA.em().createQuery(query).getResultList();
        }
    }

    @Override
    protected List<SingularAttribute<ClientModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ClientModel_.name, ClientModel_.applicationType);
    }
}
