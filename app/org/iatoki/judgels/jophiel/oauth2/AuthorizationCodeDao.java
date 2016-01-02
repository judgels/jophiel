package org.iatoki.judgels.jophiel.oauth2;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

@ImplementedBy(AuthorizationCodeHibernateDao.class)
public interface AuthorizationCodeDao extends Dao<Long, AuthorizationCodeModel> {

    boolean isAuthorized(String clientJid, String userJid, String scopes);

    AuthorizationCodeModel findByCode(String code);
}
