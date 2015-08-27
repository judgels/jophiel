package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.jophiel.models.entities.UserInfoModel;
import org.iatoki.judgels.play.models.daos.Dao;

public interface UserInfoDao extends Dao<Long, UserInfoModel> {

    boolean existsByUserJid(String userJid);

    UserInfoModel findByUserJid(String userJid);
}
