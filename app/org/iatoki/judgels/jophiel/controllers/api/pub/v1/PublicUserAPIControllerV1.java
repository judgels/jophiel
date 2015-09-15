package org.iatoki.judgels.jophiel.controllers.api.pub.v1;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.AutoComplete;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.controllers.api.AbstractJophielAPIController;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserV1;
import org.iatoki.judgels.jophiel.services.UserProfileService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.play.apis.JudgelsAPINotFoundException;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Named
public final class PublicUserAPIControllerV1 extends AbstractJophielAPIController {

    private final UserService userService;
    private final UserProfileService userProfileService;

    @Inject
    public PublicUserAPIControllerV1(UserService userService, UserProfileService userProfileService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
    }

    @Transactional(readOnly = true)
    public Result autocompleteUser(String term) {
        List<User> users = userService.getUsersByTerm(term);
        ImmutableList.Builder<AutoComplete> autocompletedUsers = ImmutableList.builder();
        for (User user : users) {
            String display = user.getUsername();
            if (user.isShowName()) {
                display += " (" + user.getName() + ")";
            }
            autocompletedUsers.add(new AutoComplete(user.getJid(), user.getUsername(), display));
        }

        return okAsJson(autocompletedUsers.build());
    }

    @Transactional(readOnly = true)
    public Result findUserByJid(String userJid) {
        if (!userService.userExistsByJid(userJid)) {
            throw new JudgelsAPINotFoundException();
        }

        User user = userService.findUserByJid(userJid);
        return okAsJson(createUserV1FromUser(user));
    }

    @Transactional(readOnly = true)
    public Result findUserByUsername(String username) {
        if (!userService.userExistsByUsername(username)) {
            throw new JudgelsAPINotFoundException();
        }

        User user = userService.findUserByUsername(username);
        return okAsJson(createUserV1FromUser(user));
    }

    public Result renderAvatarImage(String imageName) {
        String avatarUrl = userProfileService.getAvatarImageUrlString(imageName);
        return okAsImage(avatarUrl);
    }

    private UserV1 createUserV1FromUser(User user) {
        UserV1 responseBody = new UserV1();
        responseBody.jid = user.getJid();
        responseBody.username = user.getUsername();
        if (user.isShowName()) {
            responseBody.name = user.getName();
        }
        responseBody.profilePictureUrl = user.getProfilePictureUrl().toString();
        return responseBody;
    }
}
