package org.iatoki.judgels.jophiel.controllers.apis;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.AutoComplete;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.services.ClientService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.play.JudgelsPlayProperties;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.iatoki.judgels.play.controllers.apis.JudgelsAPIControllerUtils.createJsonPResponse;
import static org.iatoki.judgels.play.controllers.apis.JudgelsAPIControllerUtils.setAccessControlOrigin;

@Singleton
@Named
public final class ClientAPIController extends Controller {

    private final ClientService clientService;
    private final UserService userService;

    @Inject
    public ClientAPIController(ClientService clientService, UserService userService) {
        this.clientService = clientService;
        this.userService = userService;
    }

    public Result preClientAutocompleteList() {
        setAccessControlOrigin("*", "GET", TimeUnit.SECONDS.convert(5, TimeUnit.MINUTES));
        return ok();
    }

    @Authenticated(LoggedIn.class)
    @Transactional(readOnly = true)
    public Result clientAutoCompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");

        DynamicForm dForm = DynamicForm.form().bindFromRequest();

        String term = dForm.get("term");
        List<Client> clients = clientService.getClientsByTerm(term);
        ImmutableList.Builder<AutoComplete> autoCompleteBuilder = ImmutableList.builder();
        for (Client client : clients) {
            autoCompleteBuilder.add(new AutoComplete(client.getJid(), client.getName(), client.getName()));
        }

        return ok(Json.toJson(autoCompleteBuilder.build()));
    }

    @Transactional(readOnly = true)
    public Result linkedClientList() {
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setContentType("application/javascript");

        DynamicForm dform = DynamicForm.form().bindFromRequest();
        String callback = dform.get("callback");

        String referer = request().getHeader("Referer");
        ImmutableMap.Builder<String, String> clientMapBuilder = ImmutableMap.builder();

        if (!referer.startsWith(JophielProperties.getInstance().getJophielBaseUrl())) {
            clientMapBuilder.put(JophielProperties.getInstance().getJophielBaseUrl(), JudgelsPlayProperties.getInstance().getAppTitle());
        }
        for (int i = 0; i < JophielProperties.getInstance().getJophielClientLabels().size(); ++i) {
            String target = JophielProperties.getInstance().getJophielClientTargets().get(i);
            String label = JophielProperties.getInstance().getJophielClientLabels().get(i);
            if (!referer.startsWith(target)) {
                clientMapBuilder.put(target, label);
            }
        }

        return ok(createJsonPResponse(callback, Json.toJson(clientMapBuilder.build()).toString()));
    }
}
