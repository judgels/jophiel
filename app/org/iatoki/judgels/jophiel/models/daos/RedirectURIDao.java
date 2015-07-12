package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.play.models.daos.interfaces.Dao;
import org.iatoki.judgels.jophiel.models.entities.RedirectURIModel;

import java.util.List;

public interface RedirectURIDao extends Dao<Long, RedirectURIModel> {

    List<RedirectURIModel> findByClientJid(String clientJid);

}
