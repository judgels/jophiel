package org.iatoki.judgels.jophiel.services;

import org.iatoki.judgels.jophiel.Institution;
import org.iatoki.judgels.jophiel.InstitutionNotFoundException;
import org.iatoki.judgels.play.Page;

import java.util.List;

public interface InstitutionService {

    List<Institution> getInstitutionsByTerm(String term);

    boolean institutionExistsByName(String name);

    Institution findInstitutionById(long institutionId) throws InstitutionNotFoundException;

    void createInstitution(String name);

    void deleteInstitution(long institutionId) throws InstitutionNotFoundException;

    Page<Institution> getPageOfInstitutions(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);
}
