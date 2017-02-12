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
public final class CityHibernateDao extends AbstractHibernateDao<Long, CityModel> implements CityDao {

    public CityHibernateDao() {
        super(CityModel.class);
    }

    @Override
    public boolean existsByName(String name) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<CityModel> root = query.from(CityModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(CityModel_.city), name));

        return JPA.em().createQuery(query).getSingleResult() != 0;
    }

    @Override
    public Optional<CityModel> findByName(String name) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<CityModel> query = cb.createQuery(CityModel.class);
        Root<CityModel> root = query.from(CityModel.class);

        query.where(cb.equal(root.get(CityModel_.city), name));

        return Optional.ofNullable(JPA.em().createQuery(query).getSingleResult());
    }

    @Override
    protected List<SingularAttribute<CityModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(CityModel_.city);
    }
}
