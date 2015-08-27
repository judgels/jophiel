package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.jophiel.Province;
import org.iatoki.judgels.jophiel.ProvinceNotFoundException;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.Authorized;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.forms.ProvinceCreateForm;
import org.iatoki.judgels.jophiel.forms.ProvinceUploadForm;
import org.iatoki.judgels.jophiel.services.ProvinceService;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.views.html.suggestion.province.listCreateProvincesView;
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
public final class ProvinceController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final ProvinceService provinceService;
    private final UserActivityService userActivityService;

    @Inject
    public ProvinceController(ProvinceService provinceService, UserActivityService userActivityService) {
        this.provinceService = provinceService;
        this.userActivityService = userActivityService;
    }

    @Transactional
    @AddCSRFToken
    public Result index() {
        return listCreateProvinces(0, "id", "asc", "");
    }

    @Transactional
    @AddCSRFToken
    public Result listCreateProvinces(long page, String orderBy, String orderDir, String filterString) {
        Page<Province> pageOfProvinces = provinceService.getPageOfProvinces(page, PAGE_SIZE, orderBy, orderDir, filterString);
        Form<ProvinceCreateForm> provinceCreateForm = Form.form(ProvinceCreateForm.class);
        Form<ProvinceUploadForm> provinceUploadForm = Form.form(ProvinceUploadForm.class);

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Open all provinces <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showListCreateProvince(pageOfProvinces, orderBy, orderDir, filterString, provinceCreateForm, provinceUploadForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateProvince(long page, String orderBy, String orderDir, String filterString) throws ProvinceNotFoundException {
        Form<ProvinceCreateForm> provinceCreateForm = Form.form(ProvinceCreateForm.class).bindFromRequest();

        if (formHasErrors(provinceCreateForm)) {
            Page<Province> pageOfProvinces = provinceService.getPageOfProvinces(page, PAGE_SIZE, orderBy, orderDir, filterString);
            Form<ProvinceUploadForm> provinceUploadForm = Form.form(ProvinceUploadForm.class);

            return showListCreateProvince(pageOfProvinces, orderBy, orderDir, filterString, provinceCreateForm, provinceUploadForm);
        }

        ProvinceCreateForm provinceCreateData = provinceCreateForm.get();
        provinceService.createProvince(provinceCreateData.name);

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Create province " + provinceCreateData.name + ".");

        return redirect(routes.ProvinceController.listCreateProvinces(page, orderBy, orderDir, filterString));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadProvince(long page, String orderBy, String orderDir, String filterString) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file;

        file = body.getFile("provinces");
        if (file != null) {
            File userFile = file.getFile();
            try {
                String[] provinces = FileUtils.readFileToString(userFile).split("\n");
                for (String province : provinces) {
                    if (!"".equals(province) && !provinceService.provinceExistsByName(province)) {
                        provinceService.createProvince(province);
                    }

                }

                JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Upload provinces.");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return redirect(routes.ProvinceController.listCreateProvinces(page, orderBy, orderDir, filterString));
    }

    @Transactional
    public Result deleteProvince(long provinceId) throws ProvinceNotFoundException {
        Province province = provinceService.findProvinceById(provinceId);
        provinceService.deleteProvince(province.getId());

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Delete province " + province.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.ProvinceController.index());
    }

    private Result showListCreateProvince(Page<Province> pageOfProvinces, String orderBy, String orderDir, String filterString, Form<ProvinceCreateForm> provinceCreateForm, Form<ProvinceUploadForm> provinceUploadForm) {
        LazyHtml content = new LazyHtml(listCreateProvincesView.render(pageOfProvinces, orderBy, orderDir, filterString, provinceCreateForm, provinceUploadForm));
        content.appendLayout(c -> headingLayout.render(Messages.get("province.list"), c));
        AutoSuggestionControllerUtils.appendTabLayout(content);
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content);
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Provinces");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = AutoSuggestionControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("province.provinces"), routes.AutoSuggestionController.jumpToProvinces()));
        breadcrumbsBuilder.add(lastLinks);

        JophielControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
