package org.iatoki.judgels.jophiel.user.profile.info;

public class UserInfoBuilder {

    private long id;
    private String userJid;
    private String gender;
    private long birthDate;
    private String streetAddress;
    private int postalCode;
    private String institution;
    private String city;
    private String provinceOrState;
    private String country;
    private String shirtSize;
    private String biodata;

    public UserInfoBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public UserInfoBuilder setUserJid(String userJid) {
        this.userJid = userJid;
        return this;
    }

    public UserInfoBuilder setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public UserInfoBuilder setBirthDate(long birthDate) {
        this.birthDate = birthDate;
        return this;
    }

    public UserInfoBuilder setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
        return this;
    }

    public UserInfoBuilder setPostalCode(int postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    public UserInfoBuilder setInstitution(String institution) {
        this.institution = institution;
        return this;
    }

    public UserInfoBuilder setCity(String city) {
        this.city = city;
        return this;
    }

    public UserInfoBuilder setProvinceOrState(String provinceOrState) {
        this.provinceOrState = provinceOrState;
        return this;
    }

    public UserInfoBuilder setCountry(String country) {
        this.country = country;
        return this;
    }

    public UserInfoBuilder setShirtSize(String shirtSize) {
        this.shirtSize = shirtSize;
        return this;
    }

    public UserInfoBuilder setBiodata(String biodata){
        this.biodata = biodata;
        return this;
    }

    public UserInfo createUserInfo() {
        return new UserInfo(id, userJid, gender, birthDate, streetAddress, postalCode, institution, city, provinceOrState, country, shirtSize, biodata);
    }
}
