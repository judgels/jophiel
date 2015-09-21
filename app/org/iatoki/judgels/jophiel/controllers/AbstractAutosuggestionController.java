package org.iatoki.judgels.jophiel.controllers;

import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.play.HtmlTemplate;
import play.i18n.Messages;
import play.mvc.Result;

public abstract class AbstractAutosuggestionController extends AbstractJophielController {

    protected AbstractAutosuggestionController(UserActivityService userActivityService) {
        super(userActivityService);
    }

    @Override
    protected Result renderTemplate(HtmlTemplate template) {
        template.markBreadcrumbLocation(Messages.get("autosuggestion.text.autosuggestions"), routes.AutosuggestionController.index());
        template.addMainTab(Messages.get("institution.text.institutions"), routes.InstitutionController.index());
        template.addMainTab(Messages.get("city.text.cities"), routes.CityController.index());
        template.addMainTab(Messages.get("province.text.provinces"), routes.ProvinceController.index());
        template.setMainTitle(Messages.get("autosuggestion.text.list"));

        return super.renderTemplate(template);
    }
}
