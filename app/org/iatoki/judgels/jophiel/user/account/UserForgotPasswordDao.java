package org.iatoki.judgels.jophiel.user.account;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

@ImplementedBy(UserForgotPasswordHibernateDao.class)
public interface UserForgotPasswordDao extends Dao<Long, UserForgotPasswordModel> {

    boolean isForgotPasswordCodeValid(String forgotPasswordCode, long currentMillis);

    UserForgotPasswordModel findByForgotPasswordCode(String forgotPasswordCode);
}
