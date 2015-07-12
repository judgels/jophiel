package org.iatoki.judgels.jophiel.controllers;

import org.iatoki.judgels.play.controllers.BaseController;
import play.mvc.Result;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
public final class ApplicationController extends BaseController {

    public ApplicationController() {
    }

    public Result index() {
        return redirect(routes.UserAccountController.login().absoluteURL(request(), request().secure()));
    }
}
