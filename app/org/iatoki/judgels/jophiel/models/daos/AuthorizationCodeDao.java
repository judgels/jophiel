package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.play.models.daos.interfaces.Dao;
import org.iatoki.judgels.jophiel.models.entities.AuthorizationCodeModel;

public interface AuthorizationCodeDao extends Dao<Long, AuthorizationCodeModel> {

    AuthorizationCodeModel findByCode(String code);

    boolean checkIfAuthorized(String clientJid, String userJid, String scopes);

}
