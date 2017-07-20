package org.iatoki.judgels.jophiel.user.profile.info;

public final class UserInfo {

    private final long id;
    private final String userJid;
    private final String gender;
    private final long birthDate;
    private final String streetAddress;
    private final int postalCode;
    private final String institution;
    private final String city;
    private final String provinceOrState;
    private final String country;
    private final String shirtSize;
    private final String biodata;

    public UserInfo(long id, String userJid, String gender, long birthDate, String streetAddress, int postalCode, String institution, String city, String provinceOrState, String country, String shirtSize, String biodata) {
        this.id = id;
        this.userJid = userJid;
        this.gender = gender;
        this.birthDate = birthDate;
        this.streetAddress = streetAddress;
        this.postalCode = postalCode;
        this.institution = institution;
        this.city = city;
        this.provinceOrState = provinceOrState;
        this.country = country;
        this.shirtSize = shirtSize;
        this.biodata = biodata;
    }

    public long getId() {
        return id;
    }

    public String getUserJid() {
        return userJid;
    }

    public String getGender() {
        return gender;
    }

    public long getBirthDate() {
        return birthDate;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public int getPostalCode() {
        return postalCode;
    }

    public String getInstitution() {
        return institution;
    }

    public String getCity() {
        return city;
    }

    public String getProvinceOrState() {
        return provinceOrState;
    }

    public String getCountry() {
        return country;
    }

    public String getShirtSize() {
        return shirtSize;
    }

    public String getBiodata() {return biodata;}
}
