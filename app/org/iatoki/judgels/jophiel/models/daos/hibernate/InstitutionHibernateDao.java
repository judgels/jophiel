package org.iatoki.judgels.jophiel.models.daos.hibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jophiel.models.daos.InstitutionDao;
import org.iatoki.judgels.jophiel.models.entities.InstitutionModel;
import org.iatoki.judgels.jophiel.models.entities.InstitutionModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("institutionDao")
public final class InstitutionHibernateDao extends AbstractHibernateDao<Long, InstitutionModel> implements InstitutionDao {

    public InstitutionHibernateDao() {
        super(InstitutionModel.class);
    }

    @Override
    public boolean existsByName(String name) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<InstitutionModel> root = query.from(InstitutionModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(InstitutionModel_.institution), name));

        return JPA.em().createQuery(query).getSingleResult() != 0;
    }

    @Override
    public InstitutionModel findByName(String name) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<InstitutionModel> query = cb.createQuery(InstitutionModel.class);
        Root<InstitutionModel> root = query.from(InstitutionModel.class);

        query.where(cb.equal(root.get(InstitutionModel_.institution), name));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    protected List<SingularAttribute<InstitutionModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(InstitutionModel_.institution);
    }
}
