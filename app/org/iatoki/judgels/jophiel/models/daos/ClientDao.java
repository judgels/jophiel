package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.play.models.daos.JudgelsDao;
import org.iatoki.judgels.jophiel.models.entities.ClientModel;

import java.util.Collection;
import java.util.List;

public interface ClientDao extends JudgelsDao<ClientModel> {

    boolean existsByName(String name);

    List<String> getJidsByNames(Collection<String> names);
}
