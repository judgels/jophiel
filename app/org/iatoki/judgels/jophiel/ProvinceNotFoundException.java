package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class ProvinceNotFoundException extends EntityNotFoundException {

    public ProvinceNotFoundException() {
        super();
    }

    public ProvinceNotFoundException(String s) {
        super(s);
    }

    public ProvinceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProvinceNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Province";
    }
}
