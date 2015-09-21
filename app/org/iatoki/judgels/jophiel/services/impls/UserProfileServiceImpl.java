package org.iatoki.judgels.jophiel.services.impls;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.UserInfoBuilder;
import org.iatoki.judgels.jophiel.config.AvatarFileSystemProvider;
import org.iatoki.judgels.jophiel.models.daos.CityDao;
import org.iatoki.judgels.jophiel.models.daos.InstitutionDao;
import org.iatoki.judgels.jophiel.models.daos.ProvinceDao;
import org.iatoki.judgels.jophiel.models.daos.UserDao;
import org.iatoki.judgels.jophiel.models.daos.UserInfoDao;
import org.iatoki.judgels.jophiel.models.entities.CityModel;
import org.iatoki.judgels.jophiel.models.entities.InstitutionModel;
import org.iatoki.judgels.jophiel.models.entities.ProvinceModel;
import org.iatoki.judgels.jophiel.models.entities.UserInfoModel;
import org.iatoki.judgels.jophiel.models.entities.UserModel;
import org.iatoki.judgels.jophiel.services.UserProfileService;
import org.iatoki.judgels.play.JudgelsPlayUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Singleton
@Named("userProfileService")
public final class UserProfileServiceImpl implements UserProfileService {

    private final FileSystemProvider avatarFileSystemProvider;
    private final CityDao cityDao;
    private final InstitutionDao institutionDao;
    private final ProvinceDao provinceDao;
    private final UserDao userDao;
    private final UserInfoDao userInfoDao;

    @Inject
    public UserProfileServiceImpl(@AvatarFileSystemProvider FileSystemProvider avatarFileSystemProvider, CityDao cityDao, InstitutionDao institutionDao, ProvinceDao provinceDao, UserDao userDao, UserInfoDao userInfoDao) {
        this.avatarFileSystemProvider = avatarFileSystemProvider;
        this.cityDao = cityDao;
        this.institutionDao = institutionDao;
        this.provinceDao = provinceDao;
        this.userDao = userDao;
        this.userInfoDao = userInfoDao;
    }

    @PostConstruct
    public void init() {
        if (!avatarFileSystemProvider.fileExists(ImmutableList.of("avatar-default.png"))) {
            try {
                avatarFileSystemProvider.uploadFileFromStream(ImmutableList.of(), getClass().getResourceAsStream("/public/images/avatar/avatar-default.png"), "avatar-default.png");
                avatarFileSystemProvider.makeFilePublic(ImmutableList.of("avatar-default.png"));
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create default avatar.");
            }
        }
    }

    @Override
    public boolean infoExists(String userJid) {
        return userInfoDao.existsByUserJid(userJid);
    }

    @Override
    public void updateProfile(String userJid, String name, boolean showName, String userIpAddress) {
        UserModel userModel = userDao.findByJid(userJid);
        userModel.name = name;
        userModel.showName = showName;

        userDao.edit(userModel, userJid, userIpAddress);
    }

    @Override
    public void updateProfile(String userJid, String name, boolean showName, String password, String userIpAddress) {
        UserModel userModel = userDao.findByJid(userJid);
        userModel.name = name;
        userModel.showName = showName;
        userModel.password = JudgelsPlayUtils.hashSHA256(password);

        userDao.edit(userModel, userJid, userIpAddress);
    }

    @Override
    public void upsertInfo(String userJid, String gender, Date birthDate, String streetAddress, int postalCode, String institution, String city, String provinceOrState, String country, String shirtSize, String userIpAddress) {
        UserInfoModel userInfoModel;
        boolean recordExists = userInfoDao.existsByUserJid(userJid);

        if (recordExists) {
            userInfoModel = userInfoDao.findByUserJid(userJid);
        } else {
            userInfoModel = new UserInfoModel();
            userInfoModel.userJid = userJid;
        }
        userInfoModel.gender = gender;
        userInfoModel.birthDate = birthDate.getTime();
        userInfoModel.streetAddress = streetAddress;
        userInfoModel.postalCode = postalCode;
        if (!institution.isEmpty()) {
            if (!institutionDao.existsByName(institution)) {
                InstitutionModel institutionModel = new InstitutionModel();
                institutionModel.institution = institution;
                institutionModel.referenceCount = 0;

                institutionDao.persist(institutionModel, userJid, userIpAddress);
            } else {
                InstitutionModel institutionModel = institutionDao.findByName(institution);
                institutionModel.referenceCount++;

                institutionDao.edit(institutionModel, userJid, userIpAddress);
            }
        }
        userInfoModel.institution = institution;
        userInfoModel.city = city;
        if (!city.isEmpty()) {
            if (!cityDao.existsByName(city)) {
                CityModel cityModel = new CityModel();
                cityModel.city = city;
                cityModel.referenceCount = 0;

                cityDao.persist(cityModel, userJid, userIpAddress);
            } else {
                CityModel cityModel = cityDao.findByName(city);
                cityModel.referenceCount++;

                cityDao.edit(cityModel, userJid, userIpAddress);
            }
        }
        userInfoModel.provinceOrState = provinceOrState;
        if (!provinceOrState.isEmpty()) {
            if (!provinceDao.existsByName(provinceOrState)) {
                ProvinceModel provinceModel = new ProvinceModel();
                provinceModel.province = provinceOrState;
                provinceModel.referenceCount = 0;

                provinceDao.persist(provinceModel, userJid, userIpAddress);
            } else {
                ProvinceModel provinceModel = provinceDao.findByName(provinceOrState);
                provinceModel.referenceCount++;

                provinceDao.edit(provinceModel, userJid, userIpAddress);
            }
        }
        userInfoModel.country = country;
        userInfoModel.shirtSize = shirtSize;

        if (recordExists) {
            userInfoDao.edit(userInfoModel, userJid, userIpAddress);
        } else {
            userInfoDao.persist(userInfoModel, userJid, userIpAddress);
        }
    }

    @Override
    public UserInfo getInfo(String userJid) {
        UserInfoModel userInfoModel = userInfoDao.findByUserJid(userJid);

        UserInfoBuilder userInfoBuilder = new UserInfoBuilder();
        userInfoBuilder
                .setBirthDate(userInfoModel.birthDate)
                .setCity(userInfoModel.city)
                .setCountry(userInfoModel.country)
                .setGender(userInfoModel.gender)
                .setId(userInfoModel.id)
                .setInstitution(userInfoModel.institution)
                .setPostalCode(userInfoModel.postalCode)
                .setProvinceOrState(userInfoModel.provinceOrState)
                .setShirtSize(userInfoModel.shirtSize)
                .setStreetAddress(userInfoModel.streetAddress)
                .setUserJid(userInfoModel.userJid);

        return userInfoBuilder.createUserInfo();
    }

    @Override
    public String updateAvatarWithGeneratedFilename(String userJid, File imageFile, String extension, String ipAddress) throws IOException {
        String newImageName = userJid + "-" + JudgelsPlayUtils.hashMD5(UUID.randomUUID().toString()) + "." + extension;
        List<String> filePath = ImmutableList.of(newImageName);
        avatarFileSystemProvider.uploadFile(ImmutableList.of(), imageFile, newImageName);
        avatarFileSystemProvider.makeFilePublic(filePath);

        UserModel userModel = userDao.findByJid(userJid);
        userModel.profilePictureImageName = newImageName;

        userDao.edit(userModel, userJid, ipAddress);
        return newImageName;
    }

    @Override
    public String getAvatarImageUrlString(String imageName) {
        return avatarFileSystemProvider.getURL(ImmutableList.of(imageName));
    }
}
