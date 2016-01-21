package org.iatoki.judgels.jophiel.controllers.api.client.v1;

import com.google.gson.reflect.TypeToken;
import org.iatoki.judgels.jophiel.controllers.api.AbstractJophielAPIController;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.ActivityMessageV1;
import org.iatoki.judgels.jophiel.client.ClientService;
import org.iatoki.judgels.jophiel.activity.UserActivityService;
import org.iatoki.judgels.play.api.JudgelsAppClientAPIIdentity;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Named
public final class ClientActivityAPIControllerV1 extends AbstractJophielAPIController {

    private final ClientService clientService;
    private final UserActivityService userActivityService;

    @Inject
    public ClientActivityAPIControllerV1(ClientService clientService, UserActivityService userActivityService) {
        this.clientService = clientService;
        this.userActivityService = userActivityService;
    }

    @Transactional
    public Result createActivity() {
        JudgelsAppClientAPIIdentity identity = authenticateAsJudgelsAppClient(clientService);
        List<ActivityMessageV1> messages = parseRequestBody(new TypeToken<List<ActivityMessageV1>>() { }.getType());

        for (ActivityMessageV1 message : messages) {
            userActivityService.createUserActivity(identity.getClientJid(), message.userJid, message.time, message.log, message.ipAddress);
        }

        return ok();
    }
}
