package org.iatoki.judgels.jophiel.activity;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.model.AbstractJedisHibernateDao;
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
