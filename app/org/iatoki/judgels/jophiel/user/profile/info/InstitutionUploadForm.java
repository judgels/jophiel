package org.iatoki.judgels.jophiel.user.profile.info;

import play.data.validation.Constraints;

import java.io.File;

public final class InstitutionUploadForm {

    @Constraints.Required
    public File institutions;
}
