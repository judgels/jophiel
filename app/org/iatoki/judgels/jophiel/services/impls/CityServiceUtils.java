package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.jophiel.City;
import org.iatoki.judgels.jophiel.models.entities.CityModel;

final class CityServiceUtils {

    private CityServiceUtils() {
        // prevent instantiation
    }

    static City createCityFromModel(CityModel cityModel) {
        return new City(cityModel.id, cityModel.city, cityModel.referenceCount);
    }
}
