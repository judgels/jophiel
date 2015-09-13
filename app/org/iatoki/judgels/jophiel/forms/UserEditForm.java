package org.iatoki.judgels.jophiel.forms;

import play.data.validation.Constraints;

public final class UserEditForm {

    @Constraints.Required
    @Constraints.MinLength(3)
    @Constraints.MaxLength(20)
    @Constraints.Pattern("^[a-zA-Z0-9\\._]+$")
    public String username;

    @Constraints.Required
    public String name;

    @Constraints.Required
    @Constraints.Email
    public String email;

    public String password;

    @Constraints.Required
    public String roles;
}
