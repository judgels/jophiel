package org.iatoki.judgels.jophiel.user.profile.info;

import org.iatoki.judgels.play.Page;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public final class ProvinceServiceImpl implements ProvinceService {

    private final ProvinceDao provinceDao;

    @Inject
    public ProvinceServiceImpl(ProvinceDao provinceDao) {
        this.provinceDao = provinceDao;
    }

    @Override
    public List<Province> getProvincesByTerm(String term) {
        List<ProvinceModel> provinceModels = provinceDao.findSortedByFilters("id", "asc", term, 0, -1);

        return provinceModels.stream().map(m -> ProvinceServiceUtils.createProvinceFromModel(m)).collect(Collectors.toList());
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

        return ProvinceServiceUtils.createProvinceFromModel(provinceModel);
    }

    @Override
    public void createProvince(String name, String userJid, String userIpAddress) {
        ProvinceModel provinceModel = new ProvinceModel();
        provinceModel.province = name;
        provinceModel.referenceCount = 0;

        provinceDao.persist(provinceModel, userJid, userIpAddress);
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
        long totalPages = provinceDao.countByFilters(filterString);
        List<ProvinceModel> provinceModels = provinceDao.findSortedByFilters(orderBy, orderDir, filterString, pageIndex * pageSize, pageSize);

        List<Province> clients = provinceModels.stream().map(m -> ProvinceServiceUtils.createProvinceFromModel(m)).collect(Collectors.toList());

        return new Page<>(clients, totalPages, pageIndex, pageSize);
    }
}
