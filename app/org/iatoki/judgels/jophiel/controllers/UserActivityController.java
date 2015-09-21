package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableSet;
import org.iatoki.judgels.jophiel.UserActivity;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.Authorized;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.services.ClientService;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.activity.listUsersActivitiesView;
import org.iatoki.judgels.play.HtmlTemplate;
import org.iatoki.judgels.play.Page;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Authenticated({LoggedIn.class, HasRole.class})
@Authorized("admin")
@Singleton
@Named
public final class UserActivityController extends AbstractJophielController {

    private static final long PAGE_SIZE = 20;

    private final ClientService clientService;
    private final UserService userService;

    @Inject
    public UserActivityController(UserActivityService userActivityService, ClientService clientService, UserService userService) {
        super(userActivityService);

        this.clientService = clientService;
        this.userService = userService;
    }

    @Transactional
    public Result index() {
        return listUsersActivities(0, "time", "desc", "", "", "");
    }

    @Transactional
    public Result listUsersActivities(long page, String orderBy, String orderDir, String filterString, String clientNames, String usernames) {
        String[] clientName = clientNames.split(",");
        ImmutableSet.Builder<String> clientNamesSetBuilder = ImmutableSet.builder();
        for (String client : clientName) {
            if (!"".equals(client) && clientService.clientExistsByName(client)) {
                clientNamesSetBuilder.add(client);
            }
        }
        String[] username = usernames.split(",");
        ImmutableSet.Builder<String> usernamesSetBuilder = ImmutableSet.builder();
        for (String user : username) {
            if (!"".equals(user) && userService.userExistsByUsername(user)) {
                usernamesSetBuilder.add(user);
            }
        }

        Page<UserActivity> pageOfUserActivities = userActivityService.getPageOfUsersActivities(page, PAGE_SIZE, orderBy, orderDir, filterString, clientNamesSetBuilder.build(), usernamesSetBuilder.build());

        return showListUsersActivites(pageOfUserActivities, orderBy, orderDir, filterString, clientNames, usernames);
    }

    @Override
    protected Result renderTemplate(HtmlTemplate template) {
        template.markBreadcrumbLocation(Messages.get("activity.text.activities"), routes.UserActivityController.index());
        template.setMainTitle(Messages.get("activity.text.activities"));

        return super.renderTemplate(template);
    }

    private Result showListUsersActivites(Page<UserActivity> pageOfActivities, String orderBy, String orderDir, String filterString, String clientNames, String usernames) {
        HtmlTemplate template = new HtmlTemplate();

        template.setContent(listUsersActivitiesView.render(pageOfActivities, orderBy, orderDir, filterString, clientNames, usernames));

        return renderTemplate(template);
    }
}
