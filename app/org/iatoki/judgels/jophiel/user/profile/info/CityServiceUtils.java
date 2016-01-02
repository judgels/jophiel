package org.iatoki.judgels.jophiel.user.profile.info;

final class CityServiceUtils {

    private CityServiceUtils() {
        // prevent instantiation
    }

    static City createCityFromModel(CityModel cityModel) {
        return new City(cityModel.id, cityModel.city, cityModel.referenceCount);
    }
}
