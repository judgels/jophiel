package org.iatoki.judgels.jophiel.controllers;

import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.jophiel.BasicActivityKeys;
import org.iatoki.judgels.jophiel.Institution;
import org.iatoki.judgels.jophiel.InstitutionNotFoundException;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.Authorized;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.forms.InstitutionCreateForm;
import org.iatoki.judgels.jophiel.forms.InstitutionUploadForm;
import org.iatoki.judgels.jophiel.services.InstitutionService;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.views.html.suggestion.institution.listCreateInstitutionsView;
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
public final class InstitutionController extends AbstractAutosuggestionController {

    private static final long PAGE_SIZE = 20;
    private static final String INSTITUTION = "institution";

    private final InstitutionService institutionService;

    @Inject
    public InstitutionController(UserActivityService userActivityService, InstitutionService institutionService) {
        super(userActivityService);

        this.institutionService = institutionService;
    }

    @Transactional
    @AddCSRFToken
    public Result index() {
        return listCreateInstitutions(0, "id", "asc", "");
    }

    @Transactional
    @AddCSRFToken
    public Result listCreateInstitutions(long page, String orderBy, String orderDir, String filterString) {
        Page<Institution> pageOfInstitutions = institutionService.getPageOfInstitutions(page, PAGE_SIZE, orderBy, orderDir, filterString);
        Form<InstitutionCreateForm> institutionCreateForm = Form.form(InstitutionCreateForm.class);
        Form<InstitutionUploadForm> institutionUploadForm = Form.form(InstitutionUploadForm.class);

        return showListCreateInstitution(pageOfInstitutions, orderBy, orderDir, filterString, institutionCreateForm, institutionUploadForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateInstitution(long page, String orderBy, String orderDir, String filterString) throws InstitutionNotFoundException {
        Form<InstitutionCreateForm> institutionCreateForm = Form.form(InstitutionCreateForm.class).bindFromRequest();

        if (formHasErrors(institutionCreateForm)) {
            Page<Institution> pageOfInstitutions = institutionService.getPageOfInstitutions(page, PAGE_SIZE, orderBy, orderDir, filterString);
            Form<InstitutionUploadForm> institutionUploadForm = Form.form(InstitutionUploadForm.class);

            return showListCreateInstitution(pageOfInstitutions, orderBy, orderDir, filterString, institutionCreateForm, institutionUploadForm);
        }

        InstitutionCreateForm institutionCreateData = institutionCreateForm.get();
        institutionService.createInstitution(institutionCreateData.name, getCurrentUserJid(), getCurrentUserIpAddress());

        addActivityLog(BasicActivityKeys.CREATE.construct(INSTITUTION, null, institutionCreateData.name));

        return redirect(routes.InstitutionController.listCreateInstitutions(page, orderBy, orderDir, filterString));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadInstitution(long page, String orderBy, String orderDir, String filterString) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file;

        file = body.getFile("institutions");
        if (file != null) {
            File institutionFile = file.getFile();
            try {
                String[] institutions = FileUtils.readFileToString(institutionFile).split("\n");
                for (String institution : institutions) {
                    if (!institution.isEmpty() && !institutionService.institutionExistsByName(institution)) {
                        institutionService.createInstitution(institution, getCurrentUserJid(), getCurrentUserIpAddress());
                    }

                }

                addActivityLog(BasicActivityKeys.UPLOAD.construct(INSTITUTION, null, institutionFile.getName()));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return redirect(routes.InstitutionController.listCreateInstitutions(page, orderBy, orderDir, filterString));
    }

    @Transactional
    public Result deleteInstitution(long institutionId) throws InstitutionNotFoundException {
        Institution institution = institutionService.findInstitutionById(institutionId);
        institutionService.deleteInstitution(institution.getId());

        addActivityLog(BasicActivityKeys.REMOVE.construct(INSTITUTION, null, institution.getName()));

        return redirect(routes.InstitutionController.index());
    }

    private Result showListCreateInstitution(Page<Institution> pageOfInstitutions, String orderBy, String orderDir, String filterString, Form<InstitutionCreateForm> institutionCreateForm, Form<InstitutionUploadForm> institutionUploadForm) {
        HtmlTemplate template = super.getBaseHtmlTemplate();

        template.setContent(listCreateInstitutionsView.render(pageOfInstitutions, orderBy, orderDir, filterString, institutionCreateForm, institutionUploadForm));
        template.markBreadcrumbLocation(Messages.get("institution.text.institutions"), routes.InstitutionController.index());

        return renderTemplate(template);
    }
}
