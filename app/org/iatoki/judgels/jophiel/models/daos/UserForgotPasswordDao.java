package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.play.models.daos.interfaces.Dao;
import org.iatoki.judgels.jophiel.models.entities.UserForgotPasswordModel;

public interface UserForgotPasswordDao extends Dao<Long, UserForgotPasswordModel> {

    boolean isCodeValid(String forgotPasswordCode, long currentMillis);

    UserForgotPasswordModel findByCode(String forgotPasswordCode);

}
