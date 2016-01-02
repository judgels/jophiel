package org.iatoki.judgels.jophiel.user.profile.info;

import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.jophiel.BasicActivityKeys;
import org.iatoki.judgels.jophiel.activity.UserActivityService;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.Authorized;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.user.profile.info.html.listCreateProvincesView;
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
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
@Singleton
public final class ProvinceController extends AbstractAutosuggestionController {

    private static final long PAGE_SIZE = 20;
    private static final String PROVINCE = "province";

    private final ProvinceService provinceService;

    @Inject
    public ProvinceController(UserActivityService userActivityService, ProvinceService provinceService) {
        super(userActivityService);

        this.provinceService = provinceService;
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
        provinceService.createProvince(provinceCreateData.name, getCurrentUserJid(), getCurrentUserIpAddress());

        addActivityLog(BasicActivityKeys.CREATE.construct(PROVINCE, null, provinceCreateData.name));

        return redirect(routes.ProvinceController.listCreateProvinces(page, orderBy, orderDir, filterString));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadProvince(long page, String orderBy, String orderDir, String filterString) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file;

        file = body.getFile("provinces");
        if (file != null) {
            File provinceFile = file.getFile();
            try {
                String[] provinces = FileUtils.readFileToString(provinceFile).split("\n");
                for (String province : provinces) {
                    if (!province.isEmpty() && !provinceService.provinceExistsByName(province)) {
                        provinceService.createProvince(province, getCurrentUserJid(), getCurrentUserIpAddress());
                    }

                }

                addActivityLog(BasicActivityKeys.UPLOAD.construct(PROVINCE, null, provinceFile.getName()));

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

        addActivityLog(BasicActivityKeys.REMOVE.construct(PROVINCE, null, province.getName()));

        return redirect(routes.ProvinceController.index());
    }

    private Result showListCreateProvince(Page<Province> pageOfProvinces, String orderBy, String orderDir, String filterString, Form<ProvinceCreateForm> provinceCreateForm, Form<ProvinceUploadForm> provinceUploadForm) {
        HtmlTemplate template = new HtmlTemplate();

        template.setContent(listCreateProvincesView.render(pageOfProvinces, orderBy, orderDir, filterString, provinceCreateForm, provinceUploadForm));
        template.markBreadcrumbLocation(Messages.get("province.text.provinces"), routes.ProvinceController.index());

        return renderTemplate(template);
    }
}
