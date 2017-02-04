package org.iatoki.judgels.jophiel.controllers.api.client.v1;

import org.iatoki.judgels.jophiel.controllers.api.AbstractJophielAPIController;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserEmailV1;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmail;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailService;
import play.db.jpa.Transactional;
import play.mvc.Result;

import java.util.List;
import java.util.stream.Collectors;

public class ClientUserEmailAPIControllerV1 extends AbstractJophielAPIController {

    private final UserEmailService userEmailService;
    private final UserService userService;

    public ClientUserEmailAPIControllerV1(UserEmailService userEmailService, UserService userService) {
        this.userEmailService = userEmailService;
        this.userService = userService;
    }

    @Transactional
    public Result getAllUserEmail() {
        User user = userService.findUserByJid(getCurrentUserJid());
        UserEmail primaryEmail = userEmailService.findEmailByJid(user.getEmailJid());
        List<UserEmail> userEmails = userEmailService.getEmailsByUserJid(user.getEmailJid());

        List<UserEmailV1> userEmailV1 = userEmails.stream()
                .map(x -> createUserEmailV1(x, primaryEmail.getEmail()))
                .collect(Collectors.toList());

        return okAsJson(userEmailV1);
    }

    @Transactional
    public Result getPrimaryEmail() {
        User user = userService.findUserByJid(getCurrentUserJid());
        UserEmail primaryEmail = userEmailService.findEmailByJid(user.getPhoneJid());
        return okAsJson(createUserEmailV1(primaryEmail, primaryEmail.getEmail()));
    }

    private UserEmailV1 createUserEmailV1(UserEmail userEmail, String primaryEmail) {
        UserEmailV1 result = new UserEmailV1();
        result.email = userEmail.getEmail();
        result.jid = userEmail.getJid();
        result.verified = userEmail.isEmailVerified();
        result.primary = userEmail.getEmail().equals(primaryEmail);

        return result;
    }
}
