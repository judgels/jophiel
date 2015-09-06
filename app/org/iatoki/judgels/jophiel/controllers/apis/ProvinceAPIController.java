package org.iatoki.judgels.jophiel.controllers.apis;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.AutoComplete;
import org.iatoki.judgels.jophiel.Province;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.services.ProvinceService;
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
public final class ProvinceAPIController extends Controller {

    private final ProvinceService provinceService;
    private final UserService userService;

    @Inject
    public ProvinceAPIController(ProvinceService provinceService, UserService userService) {
        this.provinceService = provinceService;
        this.userService = userService;
    }

    public Result preProvinceAutocompleteList() {
        setAccessControlOrigin("*", "GET", TimeUnit.SECONDS.convert(5, TimeUnit.MINUTES));
        return ok();
    }

    @Authenticated(LoggedIn.class)
    @Transactional(readOnly = true)
    public Result provinceAutoCompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setContentType("application/javascript");

        DynamicForm dForm = DynamicForm.form().bindFromRequest();
        String callback = dForm.get("callback");

        User user = userService.findUserByJid(IdentityUtils.getUserJid());
        String term = dForm.get("term");
        List<Province> provinces = provinceService.getProvincesByTerm(term);
        ImmutableList.Builder<AutoComplete> autoCompleteBuilder = ImmutableList.builder();
        for (Province province : provinces) {
            autoCompleteBuilder.add(new AutoComplete(province.getId() + "", province.getName(), province.getName()));
        }

        return ok(createJsonPResponse(callback, Json.toJson(autoCompleteBuilder.build()).toString()));
    }
}
