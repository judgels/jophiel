package org.iatoki.judgels.jophiel.controllers.api.client.v1.client;

import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.play.JudgelsPlayProperties;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import play.db.jpa.Transactional;
import play.mvc.Result;

public final class ClientClientAPIControllerV1 extends AbstractJudgelsAPIController {

    @Transactional(readOnly = true)
    public Result getLinkedClients() {
        String referer = request().getHeader("Referer");
        ImmutableMap.Builder<String, String> linkedClientsBuilder = ImmutableMap.builder();

        if (!referer.startsWith(JophielProperties.getInstance().getJophielBaseUrl())) {
            linkedClientsBuilder.put(JophielProperties.getInstance().getJophielBaseUrl(), JudgelsPlayProperties.getInstance().getAppTitle());
        }
        for (int i = 0; i < JophielProperties.getInstance().getJophielClientLabels().size(); ++i) {
            String target = JophielProperties.getInstance().getJophielClientTargets().get(i);
            String label = JophielProperties.getInstance().getJophielClientLabels().get(i);
            if (!referer.startsWith(target)) {
                linkedClientsBuilder.put(target, label);
            }
        }

        return okAsJson(linkedClientsBuilder.build());
    }
}

