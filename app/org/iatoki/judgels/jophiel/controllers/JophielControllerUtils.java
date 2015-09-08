package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jophiel.JophielUtils;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.client.linkedClientsLayout;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsControllerUtils;
import org.iatoki.judgels.play.views.html.layouts.menusLayout;
import org.iatoki.judgels.play.views.html.layouts.profileView;
import org.iatoki.judgels.play.views.html.layouts.sidebarLayout;
import play.api.mvc.Call;
import play.i18n.Messages;
import play.mvc.Http;

final class JophielControllerUtils extends AbstractJudgelsControllerUtils {

    private static final JophielControllerUtils INSTANCE = new JophielControllerUtils();

    @Override
    public void appendSidebarLayout(LazyHtml content) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("welcome.welcome"), routes.WelcomeController.index()));
        internalLinkBuilder.add(new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.updateProfile()));
        if (JophielUtils.hasRole("admin")) {
            internalLinkBuilder.add(new InternalLink(Messages.get("user.users"), routes.UserController.index()));
            internalLinkBuilder.add(new InternalLink(Messages.get("user.activities"), routes.UserActivityController.index()));
            internalLinkBuilder.add(new InternalLink(Messages.get("client.clients"), routes.ClientController.index()));
            internalLinkBuilder.add(new InternalLink(Messages.get("autoSuggestions"), routes.AutoSuggestionController.index()));
        }

        LazyHtml sidebarContent = new LazyHtml(profileView.render(
              IdentityUtils.getUsername(),
              IdentityUtils.getUserRealName(),
              org.iatoki.judgels.jophiel.controllers.routes.UserProfileController.updateProfile().absoluteURL(Http.Context.current().request()),
              org.iatoki.judgels.jophiel.controllers.routes.UserAccountController.logout().absoluteURL(Http.Context.current().request())
        ));
        sidebarContent.appendLayout(c -> menusLayout.render(internalLinkBuilder.build(), c));
        sidebarContent.appendLayout(c -> linkedClientsLayout.render(org.iatoki.judgels.jophiel.controllers.apis.routes.ClientAPIController.linkedClientList().path(), "lib/jophielcommons/javascripts/linkedClients.js", c));
        content.appendLayout(c -> sidebarLayout.render(sidebarContent.render(), c));
    }

    void addActivityLog(UserActivityService userActivityService, String log) {
        userActivityService.createUserActivity("localhost", IdentityUtils.getUserJid(), System.currentTimeMillis(), log, IdentityUtils.getIpAddress());
    }

    Call mainPage() {
        return routes.WelcomeController.index();
    }

    boolean loggedIn(UserService userService) {
        return (IdentityUtils.getUserJid() != null) && userService.userExistsByJid(IdentityUtils.getUserJid());
    }

    static JophielControllerUtils getInstance() {
        return INSTANCE;
    }
}
