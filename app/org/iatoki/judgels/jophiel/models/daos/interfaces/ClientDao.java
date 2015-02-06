package org.iatoki.judgels.jophiel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.JudgelsDao;
import org.iatoki.judgels.jophiel.models.domains.ClientModel;

import java.util.List;

public interface ClientDao extends JudgelsDao<ClientModel> {

    boolean isClientExistByJid(String clientJid);

    long countByFilter(String filterString);

    List<ClientModel> findByFilterAndSort(String filterString, String sortBy, String order, long first, long max);

}
