package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.models.entities.UserModel;
import play.mvc.Http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

final class UserServiceUtils {

    private UserServiceUtils() {
        // prevent instantiation
    }

    static User createUserFromModel(UserModel userModel) {
        return new User(userModel.id, userModel.jid, userModel.username, userModel.name, userModel.emailJid, userModel.phoneJid, userModel.showName, getAvatarImageUrl(userModel.profilePictureImageName), Arrays.asList(userModel.roles.split(",")));
    }

    static URL getAvatarImageUrl(String imageName) {
        try {
            return new URL(org.iatoki.judgels.jophiel.controllers.api.pub.v1.routes.PublicUserAPIControllerV1.renderAvatarImage(imageName).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
