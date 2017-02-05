package org.iatoki.judgels.jophiel.controllers.api.internal;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.iatoki.judgels.jophiel.UserToken;
import org.iatoki.judgels.jophiel.controllers.api.AbstractJophielAPIController;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserNotFoundException;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.UserTokenService;
import org.iatoki.judgels.jophiel.user.account.LoginForm;
import org.iatoki.judgels.jophiel.user.account.UserAccountService;
import org.iatoki.judgels.jophiel.user.profile.email.EmailNotVerifiedException;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Named;
import java.util.Optional;

@Singleton
@Named
public class InternalAuthApiController extends AbstractJophielAPIController {

    private final UserAccountService userAccountService;
    private final UserService userService;
    private final UserTokenService userTokenService;

    @Inject
    public InternalAuthApiController(UserAccountService userAccountService, UserService userService, UserTokenService userTokenService) {
        this.userAccountService = userAccountService;
        this.userService = userService;
        this.userTokenService = userTokenService;
    }

    @Transactional
    public Result postApiLogin() {
        Form<LoginForm> loginForm = Form.form(LoginForm.class).bindFromRequest();
        if (formHasErrors(loginForm)) {
            return badRequestAsJson("BAD_FIELD");
        }

        try {
            LoginForm loginData = loginForm.get();
            User user = userAccountService.processLogin(loginData.usernameOrEmail, loginData.password, getCurrentUserIpAddress());
            if (user == null) {
                return badRequestAsJson("INVALID_USERNAME_OR_PASSWORD");
            }

            UserToken userToken = userTokenService.createNewToken(user.getJid(), getCurrentUserJid(), getCurrentUserIpAddress());
            return okAsJson(userToken);
        } catch (UserNotFoundException e) {
            return badRequestAsJson("USER_NOT_FOUND");
        } catch (EmailNotVerifiedException e) {
            return badRequestAsJson("EMAIL_NOT_VERIFIED");
        }
    }

    @Transactional(readOnly = true)
    public Result getUserInfoByToken(String token) {
        Optional<UserToken> userToken = userTokenService.getUserTokenByToken(token);

        // TODO: refactor UserService.getByJid() to Optional
        if (userToken.isPresent()) {
            User user = userService.findUserByJid(userToken.get().getUserJid()).get();
            return okAsJson(user);
        } else {
            return badRequestAsJson("INVALID_TOKEN");
        }
    }
}
