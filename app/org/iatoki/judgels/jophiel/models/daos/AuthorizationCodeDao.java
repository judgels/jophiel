package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.jophiel.models.entities.AuthorizationCodeModel;

public interface AuthorizationCodeDao extends Dao<Long, AuthorizationCodeModel> {

    boolean isAuthorized(String clientJid, String userJid, String scopes);

    AuthorizationCodeModel findByCode(String code);
}
