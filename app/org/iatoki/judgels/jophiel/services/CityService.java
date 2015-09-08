package org.iatoki.judgels.jophiel.services;

import org.iatoki.judgels.jophiel.City;
import org.iatoki.judgels.jophiel.CityNotFoundException;
import org.iatoki.judgels.play.Page;

import java.util.List;

public interface CityService {

    List<City> getCitiesByTerm(String term);

    boolean cityExistsByName(String name);

    City findCityById(long cityId) throws CityNotFoundException;

    void createCity(String name, String userJid, String userIpAddress);

    void deleteCity(long cityId) throws CityNotFoundException;

    Page<City> getPageOfCities(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);
}
