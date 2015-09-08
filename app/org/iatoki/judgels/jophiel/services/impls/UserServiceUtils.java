package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.jophiel.PublicUser;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.models.entities.UserModel;
import org.iatoki.judgels.jophiel.controllers.apis.routes;
import play.mvc.Http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

final class UserServiceUtils {

    private UserServiceUtils() {
        // prevent instantiation
    }

    static PublicUser createPublicUserFromModels(UserModel userModel) {
        if (userModel.showName) {
            return new PublicUser(userModel.jid, userModel.username, userModel.name, getAvatarImageUrl(userModel.profilePictureImageName));
        }

        return new PublicUser(userModel.jid, userModel.username, getAvatarImageUrl(userModel.profilePictureImageName));
    }

    static User createUserFromModel(UserModel userModel) {
        return new User(userModel.id, userModel.jid, userModel.username, userModel.name, userModel.emailJid, userModel.phoneJid, userModel.showName, getAvatarImageUrl(userModel.profilePictureImageName), Arrays.asList(userModel.roles.split(",")));
    }

    static URL getAvatarImageUrl(String imageName) {
        try {
            return new URL(routes.UserAPIController.renderAvatarImage(imageName).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
