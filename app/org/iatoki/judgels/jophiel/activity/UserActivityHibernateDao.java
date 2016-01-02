package org.iatoki.judgels.jophiel.activity;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.model.AbstractHibernateDao;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("userActivityDao")
public final class UserActivityHibernateDao extends AbstractHibernateDao<Long, UserActivityModel> implements UserActivityDao {

    public UserActivityHibernateDao() {
        super(UserActivityModel.class);
    }

    @Override
    protected List<SingularAttribute<UserActivityModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(UserActivityModel_.log);
    }
}
