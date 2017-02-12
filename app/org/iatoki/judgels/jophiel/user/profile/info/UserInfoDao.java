package org.iatoki.judgels.jophiel.user.profile.info;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.Optional;

@ImplementedBy(UserInfoHibernateDao.class)
public interface UserInfoDao extends Dao<Long, UserInfoModel> {

    boolean existsByUserJid(String userJid);

    Optional<UserInfoModel> findByUserJid(String userJid);
}
