package org.iatoki.judgels.jophiel.models.daos.jedishibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jophiel.models.daos.UserActivityDao;
import org.iatoki.judgels.jophiel.models.entities.UserActivityModel;
import org.iatoki.judgels.jophiel.models.entities.UserActivityModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("userActivityDao")
public final class UserActivityJedisHibernateDao extends AbstractJedisHibernateDao<Long, UserActivityModel> implements UserActivityDao {

    @Inject
    public UserActivityJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, UserActivityModel.class);
    }

    @Override
    protected List<SingularAttribute<UserActivityModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(UserActivityModel_.log);
    }
}
