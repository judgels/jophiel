package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserActivity;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.Authorized;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.services.ClientService;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.activity.listOwnActivitiesView;
import org.iatoki.judgels.jophiel.views.html.activity.listUserActivitiesView;
import org.iatoki.judgels.jophiel.views.html.activity.listUsersActivitiesView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
public final class UserActivityController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final ClientService clientService;
    private final UserActivityService userActivityService;
    private final UserService userService;

    @Inject
    public UserActivityController(ClientService clientService, UserActivityService userActivityService, UserService userService) {
        this.clientService = clientService;
        this.userActivityService = userActivityService;
        this.userService = userService;
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
    @Transactional
    public Result index() {
        return listUsersActivities(0, "time", "desc", "", "", "");
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
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

        LazyHtml content = new LazyHtml(listUsersActivitiesView.render(pageOfUserActivities, orderBy, orderDir, filterString, clientNames, usernames));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.activity.list"), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        JophielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("user.activities"), routes.UserActivityController.index())
        ));
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "User - Activities");

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Open all user activities <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(LoggedIn.class)
    @Transactional
    public Result viewOwnActivities() {
        return listOwnActivities(0, "time", "desc", "", "");
    }

    @Authenticated(LoggedIn.class)
    @Transactional
    public Result listOwnActivities(long page, String orderBy, String orderDir, String filterString, String clientNames) {
        String username = IdentityUtils.getUsername();

        String[] clientName = clientNames.split(",");
        ImmutableSet.Builder<String> clientNamesSetBuilder = ImmutableSet.builder();
        for (String client : clientName) {
            if (!"".equals(client) && clientService.clientExistsByName(client)) {
                clientNamesSetBuilder.add(client);
            }
        }

        Page<UserActivity> pageOfUserActivities = userActivityService.getPageOfUserActivities(page, PAGE_SIZE, orderBy, orderDir, filterString, clientNamesSetBuilder.build(), username);

        LazyHtml content = new LazyHtml(listOwnActivitiesView.render(pageOfUserActivities, orderBy, orderDir, filterString, clientNames));
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.editOwnProfile()), new InternalLink(Messages.get("profile.activities"), routes.UserActivityController.viewOwnActivities())), c));
        content.appendLayout(c -> headingLayout.render("user.activities", c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        JophielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.editOwnProfile()),
                new InternalLink(Messages.get("user.activities"), routes.UserActivityController.viewOwnActivities())
        ));
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "User - Activities");

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Open own activities <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
    @Transactional
    public Result viewUserActivities(String username) {
        return listUserActivities(username, 0, "time", "desc", "", "");
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
    @Transactional
    public Result listUserActivities(String username, long page, String orderBy, String orderDir, String filterString, String clientNames) {
        if (!userService.userExistsByUsername(username)) {
            return notFound();
        }

        User user = userService.findUserByUsername(username);
        String[] clientName = clientNames.split(",");
        ImmutableSet.Builder<String> clientNamesSetBuilder = ImmutableSet.builder();
        for (String client : clientName) {
            if (!"".equals(client) && clientService.clientExistsByName(client)) {
                clientNamesSetBuilder.add(client);
            }
        }

        Page<UserActivity> pageOfUserActivities = userActivityService.getPageOfUserActivities(page, PAGE_SIZE, orderBy, orderDir, filterString, clientNamesSetBuilder.build(), username);

        LazyHtml content = new LazyHtml(listUserActivitiesView.render(username, pageOfUserActivities, orderBy, orderDir, filterString, clientNames));
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.viewProfile(username)), new InternalLink(Messages.get("profile.activities"), routes.UserActivityController.viewUserActivities(username))), c));
        content.appendLayout(c -> headingLayout.render(username, c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        JophielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.viewProfile(username)),
                new InternalLink(Messages.get("user.activities"), routes.UserActivityController.viewUserActivities(username))
        ));
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "User - Activities");

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Open user " + user.getUsername() + " activities <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }
}
