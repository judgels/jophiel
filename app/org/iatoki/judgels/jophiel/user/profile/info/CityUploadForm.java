package org.iatoki.judgels.jophiel.user.profile.info;

import play.data.validation.Constraints;

import java.io.File;

public final class CityUploadForm {

    @Constraints.Required
    public File cities;
}
