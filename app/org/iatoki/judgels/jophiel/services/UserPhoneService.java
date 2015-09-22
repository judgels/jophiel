package org.iatoki.judgels.jophiel.services;

import org.iatoki.judgels.jophiel.UserPhone;
import org.iatoki.judgels.jophiel.UserPhoneNotFoundException;

import java.util.List;

public interface UserPhoneService {

    UserPhone findPhoneById(long phoneId) throws UserPhoneNotFoundException;

    UserPhone findPhoneByJid(String phoneJid);

    List<UserPhone> getPhonesByUserJid(String userJid);

    void makePhonePrimary(String userJid, String phoneJid, String userIpAddress);

    UserPhone addFirstPhone(String userJid, String phone, String userIpAddress);

    UserPhone addPhone(String userJid, String phone, String userIpAddress);

    void removePhone(String phoneJid);
}
