package org.iatoki.judgels.jophiel.user.profile.info;

final class ProvinceServiceUtils {

    private ProvinceServiceUtils() {
        // prevent instantiation
    }

    static Province createProvinceFromModel(ProvinceModel provinceModel) {
        return new Province(provinceModel.id, provinceModel.province, provinceModel.referenceCount);
    }
}
