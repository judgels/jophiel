package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class UserEmailNotFoundException extends EntityNotFoundException {

    public UserEmailNotFoundException() {
        super();
    }

    public UserEmailNotFoundException(String s) {
        super(s);
    }

    public UserEmailNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserEmailNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "User Email";
    }
}
