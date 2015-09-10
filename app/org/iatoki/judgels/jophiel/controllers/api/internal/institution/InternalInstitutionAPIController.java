package org.iatoki.judgels.jophiel.controllers.api.internal.institution;

import com.google.gson.Gson;
import org.iatoki.judgels.AutoComplete;
import org.iatoki.judgels.jophiel.Institution;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.services.InstitutionService;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named
public final class InternalInstitutionAPIController extends AbstractJudgelsAPIController {

    private final InstitutionService institutionService;

    @Inject
    public InternalInstitutionAPIController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @Authenticated(LoggedIn.class)
    @Transactional(readOnly = true)
    public Result autocompleteInstitution(String term) {
        List<Institution> institutions = institutionService.getInstitutionsByTerm(term);
        List<AutoComplete> autocompletedInstitutions = institutions.stream()
                .map(c -> new AutoComplete("" + c.getId(), c.getName(), c.getName()))
                .collect(Collectors.toList());
        return ok(new Gson().toJson(autocompletedInstitutions));
    }
}
