package org.iatoki.judgels.jophiel.user.profile.info;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.model.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
public final class InstitutionJedisHibernateDao extends AbstractJedisHibernateDao<Long, InstitutionModel> implements InstitutionDao {

    @Inject
    public InstitutionJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, InstitutionModel.class);
        System.out.println(jedisPool.toString());
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
