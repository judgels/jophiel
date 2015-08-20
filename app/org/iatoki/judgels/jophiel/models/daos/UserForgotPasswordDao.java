package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.jophiel.models.entities.UserForgotPasswordModel;

public interface UserForgotPasswordDao extends Dao<Long, UserForgotPasswordModel> {

    boolean isForgotPasswordCodeValid(String forgotPasswordCode, long currentMillis);

    UserForgotPasswordModel findByForgotPasswordCode(String forgotPasswordCode);
}
