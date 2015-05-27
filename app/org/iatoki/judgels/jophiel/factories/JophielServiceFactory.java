package org.iatoki.judgels.jophiel.factories;

import org.iatoki.judgels.jophiel.services.*;

public interface JophielServiceFactory {

    ClientService createClientService();

    UserService createUserService();

    UserAccountService createUserAccountService();

    UserActivityService createUserActivityService();

    UserEmailService createUserEmailService();

    UserProfileService createUserProfileService();

}
