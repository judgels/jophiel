package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.jophiel.Province;
import org.iatoki.judgels.jophiel.models.entities.ProvinceModel;

final class ProvinceServiceUtils {

    private ProvinceServiceUtils() {
        // prevent instantiation
    }

    static Province createProvinceFromModel(ProvinceModel provinceModel) {
        return new Province(provinceModel.id, provinceModel.province, provinceModel.referenceCount);
    }
}
