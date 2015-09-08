package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
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
import org.iatoki.judgels.play.IdentityUtils;
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
public final class InstitutionController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final InstitutionService institutionService;
    private final UserActivityService userActivityService;

    @Inject
    public InstitutionController(InstitutionService institutionService, UserActivityService userActivityService) {
        this.institutionService = institutionService;
        this.userActivityService = userActivityService;
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

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Open all institutions <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

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
        institutionService.createInstitution(institutionCreateData.name, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Create institution " + institutionCreateData.name + ".");

        return redirect(routes.InstitutionController.listCreateInstitutions(page, orderBy, orderDir, filterString));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadInstitution(long page, String orderBy, String orderDir, String filterString) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file;

        file = body.getFile("institutions");
        if (file != null) {
            File userFile = file.getFile();
            try {
                String[] institutions = FileUtils.readFileToString(userFile).split("\n");
                for (String institution : institutions) {
                    if (!institution.isEmpty() && !institutionService.institutionExistsByName(institution)) {
                        institutionService.createInstitution(institution, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
                    }

                }

                JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Upload institutions.");

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

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Delete institution " + institution.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.InstitutionController.index());
    }

    private Result showListCreateInstitution(Page<Institution> pageOfInstitutions, String orderBy, String orderDir, String filterString, Form<InstitutionCreateForm> institutionCreateForm, Form<InstitutionUploadForm> institutionUploadForm) {
        LazyHtml content = new LazyHtml(listCreateInstitutionsView.render(pageOfInstitutions, orderBy, orderDir, filterString, institutionCreateForm, institutionUploadForm));
        content.appendLayout(c -> headingLayout.render(Messages.get("institution.list"), c));
        AutoSuggestionControllerUtils.appendTabLayout(content);
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content);
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Institutions");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = AutoSuggestionControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("institution.institutions"), routes.AutoSuggestionController.jumpToInstitutions()));
        breadcrumbsBuilder.add(lastLinks);

        JophielControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
