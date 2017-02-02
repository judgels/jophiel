package org.iatoki.judgels.jophiel.user.account;

import play.data.validation.Constraints;

public class PasswordChangeApiForm {

    @Constraints.Required
    public String password;
}
