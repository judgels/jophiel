package org.iatoki.judgels.jophiel.controllers.api.internal;

import org.iatoki.judgels.AutoComplete;
import org.iatoki.judgels.jophiel.user.profile.info.Province;
import org.iatoki.judgels.jophiel.controllers.api.AbstractJophielAPIController;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.user.profile.info.ProvinceService;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named
public final class InternalProvinceAPIController extends AbstractJophielAPIController {

    private final ProvinceService provinceService;

    @Inject
    public InternalProvinceAPIController(ProvinceService provinceService) {
        this.provinceService = provinceService;
    }

    @Authenticated(LoggedIn.class)
    @Transactional(readOnly = true)
    public Result autocompleteProvince(String term) {
        List<Province> provinces = provinceService.getProvincesByTerm(term);
        List<AutoComplete> autocompletedProvinces = provinces.stream()
                .map(c -> new AutoComplete("" + c.getId(), c.getName(), c.getName()))
                .collect(Collectors.toList());
        return okAsJson(autocompletedProvinces);
    }
}
