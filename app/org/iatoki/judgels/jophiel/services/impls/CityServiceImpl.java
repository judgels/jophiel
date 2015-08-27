package org.iatoki.judgels.jophiel.services.impls;

import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jophiel.City;
import org.iatoki.judgels.jophiel.CityNotFoundException;
import org.iatoki.judgels.jophiel.models.daos.CityDao;
import org.iatoki.judgels.jophiel.models.entities.CityModel;
import org.iatoki.judgels.jophiel.services.CityService;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.Page;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named("cityService")
public final class CityServiceImpl implements CityService {

    private final CityDao cityDao;

    @Inject
    public CityServiceImpl(CityDao cityDao) {
        this.cityDao = cityDao;
    }

    @Override
    public List<City> getCitiesByTerm(String term) {
        List<CityModel> cityModels = cityDao.findSortedByFilters("id", "asc", term, 0, -1);

        return cityModels.stream().map(m -> createFromModel(m)).collect(Collectors.toList());
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

        return createFromModel(cityModel);
    }

    @Override
    public void createCity(String name) {
        CityModel cityModel = new CityModel();
        cityModel.city = name;
        cityModel.referenceCount = 0;

        cityDao.persist(cityModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
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
        long totalPages = cityDao.countByFilters(filterString, ImmutableMap.of(), ImmutableMap.of());
        List<CityModel> cityModels = cityDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<City> clients = cityModels.stream().map(m -> createFromModel(m)).collect(Collectors.toList());

        return new Page<>(clients, totalPages, pageIndex, pageSize);
    }

    private City createFromModel(CityModel cityModel) {
        return new City(cityModel.id, cityModel.city, cityModel.referenceCount);
    }
}
