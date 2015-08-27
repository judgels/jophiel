package org.iatoki.judgels.jophiel.services.impls;

import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jophiel.Institution;
import org.iatoki.judgels.jophiel.InstitutionNotFoundException;
import org.iatoki.judgels.jophiel.models.daos.InstitutionDao;
import org.iatoki.judgels.jophiel.models.entities.InstitutionModel;
import org.iatoki.judgels.jophiel.services.InstitutionService;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.Page;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named("institutionService")
public final class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionDao institutionDao;

    @Inject
    public InstitutionServiceImpl(InstitutionDao institutionDao) {
        this.institutionDao = institutionDao;
    }

    @Override
    public List<Institution> getInstitutionsByTerm(String term) {
        List<InstitutionModel> institutionModels = institutionDao.findSortedByFilters("id", "asc", term, 0, -1);

        return institutionModels.stream().map(m -> createFromModel(m)).collect(Collectors.toList());
    }

    @Override
    public boolean institutionExistsByName(String name) {
        return institutionDao.existsByName(name);
    }

    @Override
    public Institution findInstitutionById(long institutionId) throws InstitutionNotFoundException {
        InstitutionModel institutionModel = institutionDao.findById(institutionId);

        if (institutionModel == null) {
            throw new InstitutionNotFoundException("Institution Not Found.");
        }

        return createFromModel(institutionModel);
    }

    @Override
    public void createInstitution(String name) {
        InstitutionModel institutionModel = new InstitutionModel();
        institutionModel.institution = name;
        institutionModel.referenceCount = 0;

        institutionDao.persist(institutionModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void deleteInstitution(long institutionId) throws InstitutionNotFoundException {
        InstitutionModel institutionModel = institutionDao.findById(institutionId);

        if (institutionModel == null) {
            throw new InstitutionNotFoundException("Institution Not Found.");
        }

        institutionDao.remove(institutionModel);
    }

    @Override
    public Page<Institution> getPageOfInstitutions(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = institutionDao.countByFilters(filterString, ImmutableMap.of(), ImmutableMap.of());
        List<InstitutionModel> institutionModels = institutionDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<Institution> clients = institutionModels.stream().map(m -> createFromModel(m)).collect(Collectors.toList());

        return new Page<>(clients, totalPages, pageIndex, pageSize);
    }

    private Institution createFromModel(InstitutionModel institutionModel) {
        return new Institution(institutionModel.id, institutionModel.institution, institutionModel.referenceCount);
    }
}
