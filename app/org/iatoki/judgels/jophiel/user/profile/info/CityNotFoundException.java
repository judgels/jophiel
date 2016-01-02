package org.iatoki.judgels.jophiel.user.profile.info;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class CityNotFoundException extends EntityNotFoundException {

    public CityNotFoundException() {
        super();
    }

    public CityNotFoundException(String s) {
        super(s);
    }

    public CityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CityNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "City";
    }
}
