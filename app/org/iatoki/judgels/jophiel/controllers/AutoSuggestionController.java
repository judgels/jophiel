package org.iatoki.judgels.jophiel.controllers;

import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import play.mvc.Result;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
public final class AutoSuggestionController extends AbstractJudgelsController {

    public Result index() {
        return redirect(routes.AutoSuggestionController.jumpToInstitutions());
    }

    public Result jumpToInstitutions() {
        return redirect(routes.InstitutionController.index());
    }

    public Result jumpToCities() {
        return redirect(routes.CityController.index());
    }

    public Result jumpToProvinces() {
        return redirect(routes.ProvinceController.index());
    }
}
