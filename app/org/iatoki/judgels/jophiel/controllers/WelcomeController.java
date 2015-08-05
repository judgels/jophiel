package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.views.html.welcome.welcomeView;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
public class WelcomeController {

    private final UserActivityService userActivityService;

    @Inject
    public WelcomeController(UserActivityService userActivityService) {
        this.userActivityService = userActivityService;
    }

    @Transactional
    public Result index() {
        ImmutableMap.Builder<String, String> clientMapBuilder = ImmutableMap.builder();
        for (int i=0;i<JophielProperties.getInstance().getJophielClientLabels().size();++i) {
            String target = JophielProperties.getInstance().getJophielClientTargets().get(i);
            String label = JophielProperties.getInstance().getJophielClientLabels().get(i);
            clientMapBuilder.put(target, label);
        }

        LazyHtml content = new LazyHtml(welcomeView.render(clientMapBuilder.build()));

        content.appendLayout(c -> headingLayout.render(Messages.get("welcome.welcome"), c));

        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                        new InternalLink(Messages.get("welcome.welcome"), routes.WelcomeController.index()))
        );

        ControllerUtils.getInstance().appendTemplateLayout(content, "Welcome");

        ControllerUtils.getInstance().addActivityLog(userActivityService, "View welcome page <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }
}
