package org.iatoki.judgels.jophiel.user.profile.info;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

@ImplementedBy(UserInfoHibernateDao.class)
public interface UserInfoDao extends Dao<Long, UserInfoModel> {

    boolean existsByUserJid(String userJid);

    UserInfoModel findByUserJid(String userJid);
}
