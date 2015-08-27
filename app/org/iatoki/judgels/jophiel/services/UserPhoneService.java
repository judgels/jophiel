package org.iatoki.judgels.jophiel.services;

import org.iatoki.judgels.jophiel.UserPhone;
import org.iatoki.judgels.jophiel.UserPhoneNotFoundException;

import java.util.List;

public interface UserPhoneService {

    UserPhone findPhoneById(long phoneId) throws UserPhoneNotFoundException;

    UserPhone findPhoneByJid(String phoneJid);

    List<UserPhone> getPhonesByUserJid(String userJid);

    void makePhonePrimary(String userJid, String phoneJid);

    void addFirstPhone(String userJid, String phone);

    void addPhone(String userJid, String phone);

    void removePhone(String phoneJid);
}
