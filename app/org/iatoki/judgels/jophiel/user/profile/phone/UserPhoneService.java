package org.iatoki.judgels.jophiel.user.profile.phone;

import com.google.inject.ImplementedBy;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@ImplementedBy(UserPhoneServiceImpl.class)
public interface UserPhoneService {

    UserPhone findPhoneById(long phoneId) throws UserPhoneNotFoundException;

    UserPhone findPhoneByJid(String phoneJid);

    List<UserPhone> getPhonesByUserJid(String userJid);

    Map<String, List<UserPhone>> getPhonesMapByUserJids(Collection<String> userJids);

    void makePhonePrimary(String userJid, String phoneJid, String userIpAddress);

    UserPhone addFirstPhone(String userJid, String phone, String userIpAddress);

    UserPhone addPhone(String userJid, String phone, String userIpAddress);

    void removePhone(String phoneJid);
}
