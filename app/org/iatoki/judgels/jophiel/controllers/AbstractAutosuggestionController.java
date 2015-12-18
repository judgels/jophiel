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
    protected HtmlTemplate getBaseHtmlTemplate() {
        HtmlTemplate htmlTemplate = super.getBaseHtmlTemplate();
        htmlTemplate.markBreadcrumbLocation(Messages.get("autosuggestion.text.autosuggestions"), routes.AutosuggestionController.index());
        htmlTemplate.addMainTab(Messages.get("institution.text.institutions"), routes.InstitutionController.index());
        htmlTemplate.addMainTab(Messages.get("city.text.cities"), routes.CityController.index());
        htmlTemplate.addMainTab(Messages.get("province.text.provinces"), routes.ProvinceController.index());
        htmlTemplate.setMainTitle(Messages.get("autosuggestion.text.list"));

        return htmlTemplate;
    }

    @Override
    protected Result renderTemplate(HtmlTemplate template) {
        return super.renderTemplate(template);
    }
}
