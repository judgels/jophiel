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
        userActivityService.createUserActivity(null, IdentityUtils.getUserJid(), time, session("username") + " " + activityKey.toString(), IdentityUtils.getIpAddress());
    }

    @Override
    protected Result renderTemplate(HtmlTemplate template) {
        String linkedClientsAPIEndpoint = getAbsoluteUrl(org.iatoki.judgels.jophiel.controllers.api.client.v1.routes.ClientClientAPIControllerV1.getLinkedClients());
        Html linkedClientsWidget = linkedClientsView.render(linkedClientsAPIEndpoint);
        template.addLowerSidebarWidget(linkedClientsWidget);

        if (isCurrentUserGuest()) {
            String registerUrl = getAbsoluteUrl(routes.UserAccountController.register());
            String loginUrl = getAbsoluteUrl(routes.UserAccountController.login());
            Html guestWidget = guestView.render(registerUrl, loginUrl);
            template.addUpperSidebarWidget(guestWidget);
        } else {
            String editProfileUrl = getAbsoluteUrl(routes.UserProfileController.index());
            String logoutUrl = getAbsoluteUrl(routes.UserAccountController.logout());

            Html userProfileWidget = userProfileView.render(getCurrentUsername(), getCurrentUserRealName(), getCurrentUserAvatarUrl(), editProfileUrl, logoutUrl);
            template.addUpperSidebarWidget(userProfileWidget);

            template.addSidebarMenu(Messages.get("welcome.text.welcome"), routes.WelcomeController.index());
            template.addSidebarMenu(Messages.get("profile.text.profile"), routes.UserProfileController.index());

            if (isCurrentUserAdmin()) {
                template.addSidebarMenu(Messages.get("user.text.users"), routes.UserController.index());
                template.addSidebarMenu(Messages.get("activity.text.activities"), routes.UserActivityController.index());
                template.addSidebarMenu(Messages.get("client.text.clients"), routes.ClientController.index());
                template.addSidebarMenu(Messages.get("autosuggestion.text.autosuggestions"), routes.AutosuggestionController.index());
            }
        }

        Form<UserProfileSearchForm> userProfileSearchForm = Form.form(UserProfileSearchForm.class);
        String autocompleteUserAPIEndpoint = getAbsoluteUrl(org.iatoki.judgels.jophiel.controllers.api.pub.v1.routes.PublicUserAPIControllerV1.autocompleteUser(null));
        String postSearchUserProfileUrl = getAbsoluteUrl(routes.UserProfileController.postSearchProfile());
        Html userProfileSearchWidget =  userProfileSearchView.render(userProfileSearchForm, autocompleteUserAPIEndpoint, postSearchUserProfileUrl);
        template.addLowerSidebarWidget(userProfileSearchWidget);

        return super.renderTemplate(template);
    }
}
