package org.iatoki.judgels.jophiel.forms;

import play.data.validation.Constraints;

import java.io.File;

public final class UserDownloadForm {

    @Constraints.Required
    public File users;
}
