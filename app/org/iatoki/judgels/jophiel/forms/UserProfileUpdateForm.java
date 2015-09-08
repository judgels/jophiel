package org.iatoki.judgels.jophiel.forms;

import play.data.validation.Constraints;

public final class UserProfileUpdateForm {

    @Constraints.Required
    public String name;

    public boolean showName;

    public String password;

    public String confirmPassword;
}