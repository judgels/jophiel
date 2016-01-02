package org.iatoki.judgels.jophiel.user.profile.info;

import play.data.validation.Constraints;

public final class InstitutionCreateForm {

    @Constraints.Required
    public String name;
}
