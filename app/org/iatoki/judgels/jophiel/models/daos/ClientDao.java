package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.play.models.daos.interfaces.JudgelsDao;
import org.iatoki.judgels.jophiel.models.entities.ClientModel;

import java.util.Collection;
import java.util.List;

public interface ClientDao extends JudgelsDao<ClientModel> {

    boolean existByName(String clientName);

    List<String> findClientJidsByNames(Collection<String> clientNames);

}
