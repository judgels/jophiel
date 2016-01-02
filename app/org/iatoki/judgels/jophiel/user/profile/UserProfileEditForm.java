package org.iatoki.judgels.jophiel.user.profile;

import play.data.validation.Constraints;

public final class UserProfileEditForm {

    @Constraints.Required
    public String name;

    public boolean showName;

    public String password;

    public String confirmPassword;
}
