package org.iatoki.judgels.jophiel.user.account;

import play.data.validation.Constraints;

public final class PasswordChangeForm {

    @Constraints.Required
    public String password;

    @Constraints.Required
    public String confirmPassword;
}
