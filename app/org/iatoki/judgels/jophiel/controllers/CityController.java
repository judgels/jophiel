package org.iatoki.judgels.jophiel.controllers;

import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.jophiel.BasicActivityKeys;
import org.iatoki.judgels.jophiel.City;
import org.iatoki.judgels.jophiel.CityNotFoundException;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.Authorized;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.forms.CityCreateForm;
import org.iatoki.judgels.jophiel.forms.CityUploadForm;
import org.iatoki.judgels.jophiel.services.CityService;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.views.html.suggestion.city.listCreateCitiesView;
import org.iatoki.judgels.play.HtmlTemplate;
import org.iatoki.judgels.play.Page;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
@Singleton
@Named
public final class CityController extends AbstractAutosuggestionController {

    private static final long PAGE_SIZE = 20;
    private static final String CITY = "city";

    private final CityService cityService;

    @Inject
    public CityController(UserActivityService userActivityService, CityService cityService) {
        super(userActivityService);

        this.cityService = cityService;
    }

    @Transactional
    @AddCSRFToken
    public Result index() {
        return listCreateCities(0, "id", "asc", "");
    }

    @Transactional
    @AddCSRFToken
    public Result listCreateCities(long page, String orderBy, String orderDir, String filterString) {
        Page<City> pageOfCities = cityService.getPageOfCities(page, PAGE_SIZE, orderBy, orderDir, filterString);
        Form<CityCreateForm> cityCreateForm = Form.form(CityCreateForm.class);
        Form<CityUploadForm> cityUploadForm = Form.form(CityUploadForm.class);

        return showListCreateCities(pageOfCities, orderBy, orderDir, filterString, cityCreateForm, cityUploadForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateCity(long page, String orderBy, String orderDir, String filterString) throws CityNotFoundException {
        Form<CityCreateForm> cityCreateForm = Form.form(CityCreateForm.class).bindFromRequest();

        if (formHasErrors(cityCreateForm)) {
            Page<City> pageOfCities = cityService.getPageOfCities(page, PAGE_SIZE, orderBy, orderDir, filterString);
            Form<CityUploadForm> cityUploadForm = Form.form(CityUploadForm.class);

            return showListCreateCities(pageOfCities, orderBy, orderDir, filterString, cityCreateForm, cityUploadForm);
        }

        CityCreateForm cityCreateData = cityCreateForm.get();
        cityService.createCity(cityCreateData.name, getCurrentUserJid(), getCurrentUserIpAddress());

        addActivityLog(BasicActivityKeys.CREATE.construct(CITY, null, cityCreateData.name));

        return redirect(routes.CityController.listCreateCities(page, orderBy, orderDir, filterString));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadCity(long page, String orderBy, String orderDir, String filterString) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file;

        file = body.getFile("cities");
        if (file != null) {
            File cityFile = file.getFile();
            try {
                String[] citys = FileUtils.readFileToString(cityFile).split("\n");
                for (String city : citys) {
                    if (!city.isEmpty() && !cityService.cityExistsByName(city)) {
                        cityService.createCity(city, getCurrentUserJid(), getCurrentUserIpAddress());
                    }

                }

                addActivityLog(BasicActivityKeys.UPLOAD.construct(CITY, null, cityFile.getName()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return redirect(routes.CityController.listCreateCities(page, orderBy, orderDir, filterString));
    }

    @Transactional
    public Result deleteCity(long cityId) throws CityNotFoundException {
        City city = cityService.findCityById(cityId);
        cityService.deleteCity(city.getId());

        addActivityLog(BasicActivityKeys.REMOVE.construct(CITY, null, city.getName()));

        return redirect(routes.CityController.index());
    }

    private Result showListCreateCities(Page<City> pageOfCities, String orderBy, String orderDir, String filterString, Form<CityCreateForm> cityCreateForm, Form<CityUploadForm> cityUploadForm) {
        HtmlTemplate template = super.getBaseHtmlTemplate();

        template.setContent(listCreateCitiesView.render(pageOfCities, orderBy, orderDir, filterString, cityCreateForm, cityUploadForm));
        template.markBreadcrumbLocation(Messages.get("city.text.cities"), routes.CityController.index());

        return renderTemplate(template);
    }
}
