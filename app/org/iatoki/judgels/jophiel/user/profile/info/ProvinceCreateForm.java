package org.iatoki.judgels.jophiel.user.profile.info;

import play.data.validation.Constraints;

public final class ProvinceCreateForm {

    @Constraints.Required
    public String name;
}
