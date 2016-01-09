package org.iatoki.judgels.jophiel.welcome;

import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jophiel.AbstractJophielController;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.activity.UserActivityService;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.welcome.html.welcomeView;
import org.iatoki.judgels.play.template.HtmlTemplate;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
public class WelcomeController extends AbstractJophielController {

    @Inject
    public WelcomeController(UserActivityService userActivityService) {
        super(userActivityService);
    }

    @Transactional
    public Result index() {
        ImmutableMap.Builder<String, String> linkedClientsBuilder = ImmutableMap.builder();
        for (int i = 0; i < JophielProperties.getInstance().getJophielClientLabels().size(); ++i) {
            String target = JophielProperties.getInstance().getJophielClientTargets().get(i);
            String label = JophielProperties.getInstance().getJophielClientLabels().get(i);
            linkedClientsBuilder.put(target, label);
        }

        return showWelcome(linkedClientsBuilder.build());
    }

    private Result showWelcome(Map<String, String> linkedClients) {
        HtmlTemplate template = getBaseHtmlTemplate();

        template.setContent(welcomeView.render(linkedClients));
        template.setMainTitle(Messages.get("welcome.text.welcome"));
        template.markBreadcrumbLocation(Messages.get("welcome.text.welcome"), routes.WelcomeController.index());

        return renderTemplate(template);
    }
}
