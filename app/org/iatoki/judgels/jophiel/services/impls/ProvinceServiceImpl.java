package org.iatoki.judgels.jophiel.services.impls;

import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jophiel.Province;
import org.iatoki.judgels.jophiel.ProvinceNotFoundException;
import org.iatoki.judgels.jophiel.models.daos.ProvinceDao;
import org.iatoki.judgels.jophiel.models.entities.ProvinceModel;
import org.iatoki.judgels.jophiel.services.ProvinceService;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.Page;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named("provinceService")
public final class ProvinceServiceImpl implements ProvinceService {

    private final ProvinceDao provinceDao;

    @Inject
    public ProvinceServiceImpl(ProvinceDao provinceDao) {
        this.provinceDao = provinceDao;
    }

    @Override
    public List<Province> getProvincesByTerm(String term) {
        List<ProvinceModel> provinceModels = provinceDao.findSortedByFilters("id", "asc", term, 0, -1);

        return provinceModels.stream().map(m -> createFromModel(m)).collect(Collectors.toList());
    }

    @Override
    public boolean provinceExistsByName(String name) {
        return provinceDao.existsByName(name);
    }

    @Override
    public Province findProvinceById(long provinceId) throws ProvinceNotFoundException {
        ProvinceModel provinceModel = provinceDao.findById(provinceId);

        if (provinceModel == null) {
            throw new ProvinceNotFoundException("Province Not Found.");
        }

        return createFromModel(provinceModel);
    }

    @Override
    public void createProvince(String name) {
        ProvinceModel provinceModel = new ProvinceModel();
        provinceModel.province = name;
        provinceModel.referenceCount = 0;

        provinceDao.persist(provinceModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void deleteProvince(long provinceId) throws ProvinceNotFoundException {
        ProvinceModel provinceModel = provinceDao.findById(provinceId);

        if (provinceModel == null) {
            throw new ProvinceNotFoundException("Province Not Found.");
        }

        provinceDao.remove(provinceModel);
    }

    @Override
    public Page<Province> getPageOfProvinces(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = provinceDao.countByFilters(filterString, ImmutableMap.of(), ImmutableMap.of());
        List<ProvinceModel> provinceModels = provinceDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<Province> clients = provinceModels.stream().map(m -> createFromModel(m)).collect(Collectors.toList());

        return new Page<>(clients, totalPages, pageIndex, pageSize);
    }

    private Province createFromModel(ProvinceModel provinceModel) {
        return new Province(provinceModel.id, provinceModel.province, provinceModel.referenceCount);
    }
}
