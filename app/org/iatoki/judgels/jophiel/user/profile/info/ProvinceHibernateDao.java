package org.iatoki.judgels.jophiel.user.profile.info;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.model.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.Optional;

@Singleton
public final class ProvinceHibernateDao extends AbstractHibernateDao<Long, ProvinceModel> implements ProvinceDao {

    public ProvinceHibernateDao() {
        super(ProvinceModel.class);
    }

    @Override
    public boolean existsByName(String name) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ProvinceModel> root = query.from(ProvinceModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(ProvinceModel_.province), name));

        return JPA.em().createQuery(query).getSingleResult() != 0;
    }

    @Override
    public Optional<ProvinceModel> findByName(String name) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ProvinceModel> query = cb.createQuery(ProvinceModel.class);
        Root<ProvinceModel> root = query.from(ProvinceModel.class);

        query.where(cb.equal(root.get(ProvinceModel_.province), name));

        return Optional.ofNullable(JPA.em().createQuery(query).getSingleResult());
    }

    @Override
    protected List<SingularAttribute<ProvinceModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ProvinceModel_.province);
    }
}
