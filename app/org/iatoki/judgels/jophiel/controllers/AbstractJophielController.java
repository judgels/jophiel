package org.iatoki.judgels.jophiel.controllers;

import org.iatoki.judgels.jophiel.ActivityKey;
import org.iatoki.judgels.jophiel.forms.UserProfileSearchForm;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.impls.ActivityLogServiceImpl;
import org.iatoki.judgels.jophiel.views.html.sidebar.linkedClientsView;
import org.iatoki.judgels.jophiel.views.html.sidebar.userProfileSearchView;
import org.iatoki.judgels.jophiel.views.html.sidebar.userProfileView;
import org.iatoki.judgels.play.HtmlTemplate;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.views.html.sidebar.guestView;
import play.api.mvc.Call;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Result;
import play.twirl.api.Html;

public abstract class AbstractJophielController extends AbstractBaseJophielController {

    protected final UserActivityService userActivityService;

    protected AbstractJophielController(UserActivityService userActivityService) {
        this.userActivityService = userActivityService;
    }

    protected Call getMainPage() {
        return routes.WelcomeController.index();
    }

    protected void addActivityLog(ActivityKey activityKey) {
        long time = System.currentTimeMillis();
        ActivityLogServiceImpl.getInstance().addActivityLog(activityKey, session("username"), time, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        userActivityService.createUserActivity(null, IdentityUtils.getUserJid(), time, activityKey.toString(), IdentityUtils.getIpAddress());
    }

    @Override
    protected HtmlTemplate getBaseHtmlTemplate() {
        HtmlTemplate htmlTemplate = super.getBaseHtmlTemplate();

        String linkedClientsAPIEndpoint = getAbsoluteUrl(org.iatoki.judgels.jophiel.controllers.api.client.v1.routes.ClientClientAPIControllerV1.getLinkedClients());
        Html linkedClientsWidget = linkedClientsView.render(linkedClientsAPIEndpoint);
        htmlTemplate.addLowerSidebarWidget(linkedClientsWidget);

        if (isCurrentUserGuest()) {
            String registerUrl = getAbsoluteUrl(routes.UserAccountController.register());
            String loginUrl = getAbsoluteUrl(routes.UserAccountController.login());
            Html guestWidget = guestView.render(registerUrl, loginUrl);
            htmlTemplate.addUpperSidebarWidget(guestWidget);
        } else {
            String editProfileUrl = getAbsoluteUrl(routes.UserProfileController.index());
            String logoutUrl = getAbsoluteUrl(routes.UserAccountController.logout());

            Html userProfileWidget = userProfileView.render(getCurrentUsername(), getCurrentUserRealName(), getCurrentUserAvatarUrl(), editProfileUrl, logoutUrl);
            htmlTemplate.addUpperSidebarWidget(userProfileWidget);

            htmlTemplate.addSidebarMenu(Messages.get("welcome.text.welcome"), routes.WelcomeController.index());
            htmlTemplate.addSidebarMenu(Messages.get("profile.text.profile"), routes.UserProfileController.index());

            if (isCurrentUserAdmin()) {
                htmlTemplate.addSidebarMenu(Messages.get("user.text.users"), routes.UserController.index());
                htmlTemplate.addSidebarMenu(Messages.get("activity.text.activities"), routes.UserActivityController.index());
                htmlTemplate.addSidebarMenu(Messages.get("client.text.clients"), routes.ClientController.index());
                htmlTemplate.addSidebarMenu(Messages.get("autosuggestion.text.autosuggestions"), routes.AutosuggestionController.index());
            }
        }

        Form<UserProfileSearchForm> userProfileSearchForm = Form.form(UserProfileSearchForm.class);
        String autocompleteUserAPIEndpoint = getAbsoluteUrl(org.iatoki.judgels.jophiel.controllers.api.pub.v1.routes.PublicUserAPIControllerV1.autocompleteUser(null));
        String postSearchUserProfileUrl = getAbsoluteUrl(routes.UserProfileController.postSearchProfile());
        Html userProfileSearchWidget =  userProfileSearchView.render(userProfileSearchForm, autocompleteUserAPIEndpoint, postSearchUserProfileUrl);
        htmlTemplate.addLowerSidebarWidget(userProfileSearchWidget);

        return htmlTemplate;
    }

    @Override
    protected Result renderTemplate(HtmlTemplate template) {
        return super.renderTemplate(template);
    }
}
