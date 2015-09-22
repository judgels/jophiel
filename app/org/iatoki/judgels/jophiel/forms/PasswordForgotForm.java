package org.iatoki.judgels.jophiel.forms;

import play.data.validation.Constraints;

public final class PasswordForgotForm {

    @Constraints.Required
    public String username;

    @Constraints.Required
    @Constraints.Email
    public String email;
}