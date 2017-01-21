package org.iatoki.judgels.jophiel.user;

import com.google.inject.Singleton;
import org.iatoki.judgels.play.model.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
public final class UserTokenHibernateDao extends AbstractHibernateDao<Long, UserTokenModel> implements UserTokenDao {

    public UserTokenHibernateDao() {
        super(UserTokenModel.class);
    }

    @Override
    public String getUserJidByToken(String token) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<UserTokenModel> root = query.from(UserTokenModel.class);

        query.select(root.get(UserTokenModel_.userJid)).where(cb.equal(root.get(UserTokenModel_.token), token));

        return (JPA.em().createQuery(query).getSingleResult());
    }
}
