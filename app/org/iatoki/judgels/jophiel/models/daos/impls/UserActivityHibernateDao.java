package org.iatoki.judgels.jophiel.models.daos.impls;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.UserActivityDao;
import org.iatoki.judgels.jophiel.models.entities.UserActivityModel;
import org.iatoki.judgels.jophiel.models.entities.UserActivityModel_;

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
