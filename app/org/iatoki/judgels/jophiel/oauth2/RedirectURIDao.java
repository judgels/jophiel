package org.iatoki.judgels.jophiel.oauth2;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.List;

@ImplementedBy(RedirectURIHibernateDao.class)
public interface RedirectURIDao extends Dao<Long, RedirectURIModel> {

    List<RedirectURIModel> getByClientJid(String clientJid);
}
