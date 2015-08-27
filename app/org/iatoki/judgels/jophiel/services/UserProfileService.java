package org.iatoki.judgels.jophiel.services;

import org.iatoki.judgels.jophiel.UserInfo;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public interface UserProfileService {

    boolean infoExists(String userJid);

    void updateProfile(String userJid, String name, boolean showName);

    void updateProfile(String userJid, String name, boolean showName, String password);

    void upsertInfo(String userJid, String gender, Date birthDate, String streetAddress, int postalCode, String institution, String city, String provinceOrState, String country, String shirtSize);

    UserInfo getInfo(String userJid);

    String updateAvatarWithGeneratedFilename(String userJid, File imageFile, String imageType) throws IOException;

    String getAvatarImageUrlString(String imageName);
}
