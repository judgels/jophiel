package org.iatoki.judgels.jophiel.controllers;

import org.iatoki.judgels.jophiel.services.UserActivityService;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
public final class AutosuggestionController extends AbstractAutosuggestionController {

    @Inject
    public AutosuggestionController(UserActivityService userActivityService) {
        super(userActivityService);
    }

    public Result index() {
        return redirect(routes.InstitutionController.index());
    }
}
