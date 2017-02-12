package org.iatoki.judgels.jophiel.user.profile.phone;

import com.google.inject.ImplementedBy;

import java.util.List;
import java.util.Optional;

@ImplementedBy(UserPhoneServiceImpl.class)
public interface UserPhoneService {

    Optional<UserPhone> findPhoneById(long phoneId);

    Optional<UserPhone> findPhoneByJid(String phoneJid);

    List<UserPhone> getPhonesByUserJid(String userJid);

    void makePhonePrimary(String userJid, String phoneJid, String userIpAddress);

    UserPhone addFirstPhone(String userJid, String phone, String userIpAddress);

    UserPhone addPhone(String userJid, String phone, String userIpAddress);

    void removePhone(String phoneJid);
}
