package org.iatoki.judgels.jophiel.client;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.JudgelsDao;

import java.util.Collection;
import java.util.List;

@ImplementedBy(ClientHibernateDao.class)
public interface ClientDao extends JudgelsDao<ClientModel> {

    boolean existsByName(String name);

    List<String> getJidsByNames(Collection<String> names);
}
