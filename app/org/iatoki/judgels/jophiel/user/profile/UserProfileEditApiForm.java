package org.iatoki.judgels.jophiel.user.profile;

import play.data.validation.Constraints;

public class UserProfileEditApiForm {

    @Constraints.Required
    public String name;

    public boolean showName;

    public String password;
}
