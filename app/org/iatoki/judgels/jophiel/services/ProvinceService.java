package org.iatoki.judgels.jophiel.services;

import org.iatoki.judgels.jophiel.Province;
import org.iatoki.judgels.jophiel.ProvinceNotFoundException;
import org.iatoki.judgels.play.Page;

import java.util.List;

public interface ProvinceService {

    List<Province> getProvincesByTerm(String term);

    boolean provinceExistsByName(String name);

    Province findProvinceById(long provinceId) throws ProvinceNotFoundException;

    void createProvince(String name);

    void deleteProvince(long provinceId) throws ProvinceNotFoundException;

    Page<Province> getPageOfProvinces(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);
}
