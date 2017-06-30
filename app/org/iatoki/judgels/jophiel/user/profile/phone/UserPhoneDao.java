package org.iatoki.judgels.jophiel.user.profile.phone;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.JudgelsDao;

import java.util.Collection;
import java.util.List;

@ImplementedBy(UserPhoneHibernateDao.class)
public interface UserPhoneDao extends JudgelsDao<UserPhoneModel> {

    List<UserPhoneModel> getByUserJid(String userJid);

    List<UserPhoneModel> getByUserJids(Collection<String> userJids);
}
