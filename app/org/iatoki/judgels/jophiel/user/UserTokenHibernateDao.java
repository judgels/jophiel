package org.iatoki.judgels.jophiel.user;

import com.google.inject.Singleton;
import org.iatoki.judgels.play.model.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Optional;

@Singleton
public final class UserTokenHibernateDao extends AbstractHibernateDao<Long, UserTokenModel> implements UserTokenDao {

    public UserTokenHibernateDao() {
        super(UserTokenModel.class);
    }

    @Override
    public Optional<UserTokenModel> getByToken(String token) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserTokenModel> query = cb.createQuery(UserTokenModel.class);
        Root<UserTokenModel> root = query.from(UserTokenModel.class);

        query.where(cb.equal(root.get(UserTokenModel_.token), token));

        UserTokenModel result = (JPA.em().createQuery(query).getSingleResult());

        return (result != null) ? Optional.of(result) : Optional.empty();
    }

    @Override
    public Optional<UserTokenModel> getByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserTokenModel> query = cb.createQuery(UserTokenModel.class);
        Root<UserTokenModel> root = query.from(UserTokenModel.class);

        query.where(cb.equal(root.get(UserTokenModel_.userJid), userJid));

        UserTokenModel result = (JPA.em().createQuery(query).getSingleResult());

        return (result != null) ? Optional.of(result) : Optional.empty();
    }
}
