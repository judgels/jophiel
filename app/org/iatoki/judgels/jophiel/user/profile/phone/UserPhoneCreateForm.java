package org.iatoki.judgels.jophiel.user.profile.phone;

import play.data.validation.Constraints;

public final class UserPhoneCreateForm {

    @Constraints.Required
    @Constraints.MinLength(6)
    @Constraints.MaxLength(15)
    @Constraints.Pattern("^\\+*[0-9]+$")
    public String phone;
}
