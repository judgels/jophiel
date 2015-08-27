package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
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
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
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
public final class CityController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final CityService cityService;
    private final UserActivityService userActivityService;

    @Inject
    public CityController(CityService cityService, UserActivityService userActivityService) {
        this.cityService = cityService;
        this.userActivityService = userActivityService;
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

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Open all cities <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showListCreateCity(pageOfCities, orderBy, orderDir, filterString, cityCreateForm, cityUploadForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateCity(long page, String orderBy, String orderDir, String filterString) throws CityNotFoundException {
        Form<CityCreateForm> cityCreateForm = Form.form(CityCreateForm.class).bindFromRequest();

        if (formHasErrors(cityCreateForm)) {
            Page<City> pageOfCities = cityService.getPageOfCities(page, PAGE_SIZE, orderBy, orderDir, filterString);
            Form<CityUploadForm> cityUploadForm = Form.form(CityUploadForm.class);

            return showListCreateCity(pageOfCities, orderBy, orderDir, filterString, cityCreateForm, cityUploadForm);
        }

        CityCreateForm cityCreateData = cityCreateForm.get();
        cityService.createCity(cityCreateData.name);

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Create city " + cityCreateData.name + ".");

        return redirect(routes.CityController.listCreateCities(page, orderBy, orderDir, filterString));
    }


    @Transactional
    @RequireCSRFCheck
    public Result postUploadCity(long page, String orderBy, String orderDir, String filterString) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file;

        file = body.getFile("cities");
        if (file != null) {
            File userFile = file.getFile();
            try {
                String[] cities = FileUtils.readFileToString(userFile).split("\n");
                for (String city : cities) {
                    if (!"".equals(city) && !cityService.cityExistsByName(city)) {
                        cityService.createCity(city);
                    }

                }

                JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Upload cities.");

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

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Delete city " + city.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.CityController.index());
    }

    private Result showListCreateCity(Page<City> pageOfCities, String orderBy, String orderDir, String filterString, Form<CityCreateForm> cityCreateForm, Form<CityUploadForm> cityUploadForm) {
        LazyHtml content = new LazyHtml(listCreateCitiesView.render(pageOfCities, orderBy, orderDir, filterString, cityCreateForm, cityUploadForm));
        content.appendLayout(c -> headingLayout.render(Messages.get("city.list"), c));
        AutoSuggestionControllerUtils.appendTabLayout(content);
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content);
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Cities");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = AutoSuggestionControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("city.cities"), routes.AutoSuggestionController.jumpToCities()));
        breadcrumbsBuilder.add(lastLinks);

        JophielControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
