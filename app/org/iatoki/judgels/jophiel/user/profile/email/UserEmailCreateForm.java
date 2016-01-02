package org.iatoki.judgels.jophiel.user.profile.email;

import play.data.validation.Constraints;

public final class UserEmailCreateForm {

    @Constraints.Required
    @Constraints.Email
    public String email;
}
