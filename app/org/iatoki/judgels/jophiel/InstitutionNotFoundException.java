package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class InstitutionNotFoundException extends EntityNotFoundException {

    public InstitutionNotFoundException() {
        super();
    }

    public InstitutionNotFoundException(String s) {
        super(s);
    }

    public InstitutionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstitutionNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Institution";
    }
}
