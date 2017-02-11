package org.iatoki.judgels.jophiel.user.profile.info;

import play.data.validation.Constraints;

public final class UserInfoEditForm {

    @Constraints.Required
    public String gender;

    public String birthDate;

    public String streetAddress;

    public int postalCode;

    public String institution;

    public String city;

    public String provinceOrState;

    @Constraints.Required
    public String country;

    public String shirtSize;

    public String biodata;
}
