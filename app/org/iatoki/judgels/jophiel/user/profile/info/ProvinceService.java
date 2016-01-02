package org.iatoki.judgels.jophiel.user.profile.info;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.Page;

import java.util.List;

@ImplementedBy(ProvinceServiceImpl.class)
public interface ProvinceService {

    List<Province> getProvincesByTerm(String term);

    boolean provinceExistsByName(String name);

    Province findProvinceById(long provinceId) throws ProvinceNotFoundException;

    void createProvince(String name, String userJid, String userIpAddress);

    void deleteProvince(long provinceId) throws ProvinceNotFoundException;

    Page<Province> getPageOfProvinces(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);
}
