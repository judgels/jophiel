package org.iatoki.judgels.jophiel.factories;

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

public interface JophielDaoFactory {

    AccessTokenDao createAccessTokenDao();

    AuthorizationCodeDao createAuthorizationCodeDao();

    ClientDao createClientDao();

    IdTokenDao createIdTokenDao();

    RedirectURIDao createRedirectURIDao();

    RefreshTokenDao createRefreshTokenDao();

    UserActivityDao createUserActivityDao();

    UserDao createUserDao();

    UserEmailDao createUserEmailDao();

    UserForgotPasswordDao createUserForgotPasswordDao();
}
