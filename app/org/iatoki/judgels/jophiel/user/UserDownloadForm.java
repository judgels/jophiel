package org.iatoki.judgels.jophiel.user;

import play.data.validation.Constraints;

import java.io.File;

public final class UserDownloadForm {

    @Constraints.Required
    public File users;
}
