package org.iatoki.judgels.jophiel.user.profile.info;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.Page;

import java.util.List;

@ImplementedBy(CityServiceImpl.class)
public interface CityService {

    List<City> getCitiesByTerm(String term);

    boolean cityExistsByName(String name);

    City findCityById(long cityId) throws CityNotFoundException;

    void createCity(String name, String userJid, String userIpAddress);

    void deleteCity(long cityId) throws CityNotFoundException;

    Page<City> getPageOfCities(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);
}
