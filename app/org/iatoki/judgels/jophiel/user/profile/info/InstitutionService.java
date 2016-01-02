package org.iatoki.judgels.jophiel.user.profile.info;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.Page;

import java.util.List;

@ImplementedBy(InstitutionServiceImpl.class)
public interface InstitutionService {

    List<Institution> getInstitutionsByTerm(String term);

    boolean institutionExistsByName(String name);

    Institution findInstitutionById(long institutionId) throws InstitutionNotFoundException;

    void createInstitution(String name, String userJid, String userIpAddress);

    void deleteInstitution(long institutionId) throws InstitutionNotFoundException;

    Page<Institution> getPageOfInstitutions(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);
}
