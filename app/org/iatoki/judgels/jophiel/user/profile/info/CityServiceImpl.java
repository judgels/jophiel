package org.iatoki.judgels.jophiel.user.profile.info;

import org.iatoki.judgels.play.Page;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public final class CityServiceImpl implements CityService {

    private final CityDao cityDao;

    @Inject
    public CityServiceImpl(CityDao cityDao) {
        this.cityDao = cityDao;
    }

    @Override
    public List<City> getCitiesByTerm(String term) {
        List<CityModel> cityModels = cityDao.findSortedByFilters("id", "asc", term, 0, -1);

        return cityModels.stream().map(m -> CityServiceUtils.createCityFromModel(m)).collect(Collectors.toList());
    }

    @Override
    public boolean cityExistsByName(String name) {
        return cityDao.existsByName(name);
    }

    @Override
    public City findCityById(long cityId) throws CityNotFoundException {
        CityModel cityModel = cityDao.findById(cityId);

        if (cityModel == null) {
            throw new CityNotFoundException("City Not Found.");
        }

        return CityServiceUtils.createCityFromModel(cityModel);
    }

    @Override
    public void createCity(String name, String userJid, String userIpAddress) {
        CityModel cityModel = new CityModel();
        cityModel.city = name;
        cityModel.referenceCount = 0;

        cityDao.persist(cityModel, userJid, userIpAddress);
    }

    @Override
    public void deleteCity(long cityId) throws CityNotFoundException {
        CityModel cityModel = cityDao.findById(cityId);

        if (cityModel == null) {
            throw new CityNotFoundException("City Not Found.");
        }

        cityDao.remove(cityModel);
    }

    @Override
    public Page<City> getPageOfCities(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = cityDao.countByFilters(filterString);
        List<CityModel> cityModels = cityDao.findSortedByFilters(orderBy, orderDir, filterString, pageIndex * pageSize, pageSize);

        List<City> clients = cityModels.stream().map(m -> CityServiceUtils.createCityFromModel(m)).collect(Collectors.toList());

        return new Page<>(clients, totalPages, pageIndex, pageSize);
    }
}
