package org.iatoki.judgels.jophiel.user.profile.info;

import org.iatoki.judgels.jophiel.AbstractJophielController;
import org.iatoki.judgels.jophiel.activity.UserActivityService;
import org.iatoki.judgels.play.template.HtmlTemplate;
import play.i18n.Messages;

public abstract class AbstractAutosuggestionController extends AbstractJophielController {

    protected AbstractAutosuggestionController(UserActivityService userActivityService) {
        super(userActivityService);
    }

    @Override
    protected HtmlTemplate getBaseHtmlTemplate() {
        HtmlTemplate template = super.getBaseHtmlTemplate();

        template.markBreadcrumbLocation(Messages.get("autosuggestion.text.autosuggestions"), routes.AutosuggestionController.index());
        template.addMainTab(Messages.get("institution.text.institutions"), routes.InstitutionController.index());
        template.addMainTab(Messages.get("city.text.cities"), routes.CityController.index());
        template.addMainTab(Messages.get("province.text.provinces"), routes.ProvinceController.index());
        template.setMainTitle(Messages.get("autosuggestion.text.list"));

        return template;
    }
}
