package org.iatoki.judgels.jophiel.controllers.forms;

import play.data.validation.Constraints;

public final class ForgotPasswordForm {
    @Constraints.Required
    public String username;

    @Constraints.Required
    @Constraints.Email
    public String email;

}
