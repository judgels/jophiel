package org.iatoki.judgels.jophiel.forms;

import play.data.validation.Constraints;

import java.io.File;

public final class ProvinceUploadForm {

    @Constraints.Required
    public File provinces;
}
