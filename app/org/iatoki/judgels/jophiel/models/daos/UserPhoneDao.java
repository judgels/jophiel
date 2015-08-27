package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.jophiel.models.entities.UserPhoneModel;
import org.iatoki.judgels.play.models.daos.JudgelsDao;

import java.util.List;

public interface UserPhoneDao extends JudgelsDao<UserPhoneModel> {

    List<UserPhoneModel> getByUserJid(String userJid);
}
