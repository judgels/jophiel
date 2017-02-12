package org.iatoki.judgels.jophiel.user.profile.phone;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class UserPhoneNotFoundException extends EntityNotFoundException {

    public UserPhoneNotFoundException() {
        super();
    }

    public UserPhoneNotFoundException(String s) {
        super(s);
    }

    public UserPhoneNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserPhoneNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "User Phone";
    }
}
