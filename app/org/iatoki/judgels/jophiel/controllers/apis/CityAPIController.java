package org.iatoki.judgels.jophiel.controllers.apis;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.AutoComplete;
import org.iatoki.judgels.jophiel.City;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.services.CityService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.play.IdentityUtils;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.iatoki.judgels.play.controllers.api.JudgelsAPIControllerUtils.createJsonPResponse;
import static org.iatoki.judgels.play.controllers.api.JudgelsAPIControllerUtils.setAccessControlOrigin;

@Singleton
@Named
public final class CityAPIController extends Controller {

    private final CityService cityService;
    private final UserService userService;

    @Inject
    public CityAPIController(CityService cityService, UserService userService) {
        this.cityService = cityService;
        this.userService = userService;
    }

    public Result preCityAutocompleteList() {
        setAccessControlOrigin("*", "GET", TimeUnit.SECONDS.convert(5, TimeUnit.MINUTES));
        return ok();
    }

    @Authenticated(LoggedIn.class)
    @Transactional(readOnly = true)
    public Result cityAutoCompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setContentType("application/javascript");

        DynamicForm dForm = DynamicForm.form().bindFromRequest();
        String callback = dForm.get("callback");

        User user = userService.findUserByJid(IdentityUtils.getUserJid());
        String term = dForm.get("term");
        List<City> cities = cityService.getCitiesByTerm(term);
        ImmutableList.Builder<AutoComplete> autoCompleteBuilder = ImmutableList.builder();
        for (City city : cities) {
            autoCompleteBuilder.add(new AutoComplete(city.getId() + "", city.getName(), city.getName()));
        }

        return ok(createJsonPResponse(callback, Json.toJson(autoCompleteBuilder.build()).toString()));
    }
}
