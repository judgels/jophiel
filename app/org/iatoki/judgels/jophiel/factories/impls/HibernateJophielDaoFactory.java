package org.iatoki.judgels.jophiel.factories.impls;

import org.iatoki.judgels.jophiel.factories.JophielDaoFactory;
import org.iatoki.judgels.jophiel.models.daos.impls.AccessTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.impls.AuthorizationCodeHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.impls.ClientHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.impls.IdTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.impls.RedirectURIHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.impls.RefreshTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.impls.UserActivityHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.impls.UserEmailHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.impls.UserForgotPasswordHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.impls.UserHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.AccessTokenDao;
import org.iatoki.judgels.jophiel.models.daos.AuthorizationCodeDao;
import org.iatoki.judgels.jophiel.models.daos.ClientDao;
import org.iatoki.judgels.jophiel.models.daos.IdTokenDao;
import org.iatoki.judgels.jophiel.models.daos.RedirectURIDao;
import org.iatoki.judgels.jophiel.models.daos.RefreshTokenDao;
import org.iatoki.judgels.jophiel.models.daos.UserActivityDao;
import org.iatoki.judgels.jophiel.models.daos.UserDao;
import org.iatoki.judgels.jophiel.models.daos.UserEmailDao;
import org.iatoki.judgels.jophiel.models.daos.UserForgotPasswordDao;

public final class HibernateJophielDaoFactory implements JophielDaoFactory {

    @Override
    public AccessTokenDao createAccessTokenDao() {
        return new AccessTokenHibernateDao();
    }

    @Override
    public AuthorizationCodeDao createAuthorizationCodeDao() {
        return new AuthorizationCodeHibernateDao();
    }

    @Override
    public ClientDao createClientDao() {
        return new ClientHibernateDao();
    }

    @Override
    public IdTokenDao createIdTokenDao() {
        return new IdTokenHibernateDao();
    }

    @Override
    public RedirectURIDao createRedirectURIDao() {
        return new RedirectURIHibernateDao();
    }

    @Override
    public RefreshTokenDao createRefreshTokenDao() {
        return new RefreshTokenHibernateDao();
    }

    @Override
    public UserActivityDao createUserActivityDao() {
        return new UserActivityHibernateDao();
    }

    @Override
    public UserDao createUserDao() {
        return new UserHibernateDao();
    }

    @Override
    public UserEmailDao createUserEmailDao() {
        return new UserEmailHibernateDao();
    }

    @Override
    public UserForgotPasswordDao createUserForgotPasswordDao() {
        return new UserForgotPasswordHibernateDao();
    }
}
