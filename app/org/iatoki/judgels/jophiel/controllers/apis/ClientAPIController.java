package org.iatoki.judgels.jophiel.controllers.apis;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.services.ClientService;
import org.iatoki.judgels.commons.AutoComplete;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

@Component
public final class ClientAPIController extends Controller {

    @Autowired
    private ClientService clientService;
    @Autowired
    private UserService userService;

    public Result preClientAutocompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");       // Need to add the correct domain in here!!
        response().setHeader("Access-Control-Allow-Methods", "GET");    // Only allow POST
        response().setHeader("Access-Control-Max-Age", "300");          // Cache response for 5 minutes
        response().setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");         // Ensure this header is also allowed!
        return ok();
    }

    @Authenticated(LoggedIn.class)
    @Transactional
    public Result clientAutoCompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");

        DynamicForm form = DynamicForm.form().bindFromRequest();
        UserInfo user = userService.findUserByUserJid(IdentityUtils.getUserJid());
        String term = form.get("term");
        List<Client> clients = clientService.findAllClientByTerm(term);
        ImmutableList.Builder<AutoComplete> responseBuilder = ImmutableList.builder();

        for (Client client : clients) {
            responseBuilder.add(new AutoComplete(client.getJid(), client.getName(), client.getName()));
        }
        return ok(Json.toJson(responseBuilder.build()));
    }

}
