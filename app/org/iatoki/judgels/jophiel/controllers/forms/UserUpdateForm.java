package org.iatoki.judgels.jophiel.controllers.forms;

import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.jophiel.commons.plains.User;
import play.data.validation.Constraints;

public final class UserUpdateForm {

    public UserUpdateForm() {

    }

    public UserUpdateForm(User user) {
        this.username = user.getUsername();
        this.name = user.getName();
        this.email = user.getEmail();
        this.roles = StringUtils.join(user.getRoles(), ",");
    }

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
