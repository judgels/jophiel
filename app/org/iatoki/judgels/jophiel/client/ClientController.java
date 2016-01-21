package org.iatoki.judgels.jophiel.client;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.jophiel.AbstractJophielController;
import org.iatoki.judgels.jophiel.activity.BasicActivityKeys;
import org.iatoki.judgels.jophiel.activity.UserActivityService;
import org.iatoki.judgels.jophiel.client.html.createClientView;
import org.iatoki.judgels.jophiel.client.html.editClientView;
import org.iatoki.judgels.jophiel.client.html.listClientsView;
import org.iatoki.judgels.jophiel.client.html.viewClientView;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.Authorized;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.play.template.HtmlTemplate;
import org.iatoki.judgels.play.Page;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
@Singleton
public final class ClientController extends AbstractJophielController {

    private static final long PAGE_SIZE = 20;
    private static final String CLIENT = "client";

    private final ClientService clientService;

    @Inject
    public ClientController(UserActivityService userActivityService, ClientService clientService) {
        super(userActivityService);

        this.clientService = clientService;
    }

    @Transactional
    public Result index() {
        return listClients(0, "id", "asc", "");
    }

    @Transactional
    public Result listClients(long pageIndex, String orderBy, String orderDir, String filterString) {
        Page<Client> pageOfClients = clientService.getPageOfClients(pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        return showListClients(pageOfClients, orderBy, orderDir, filterString);
    }

    @Transactional
    public Result viewClient(long clientId) throws ClientNotFoundException {
        Client client = clientService.findClientById(clientId);

        return showViewClient(client);
    }

    @Transactional
    @AddCSRFToken
    public Result createClient() {
        Form<ClientCreateForm> clientCreateForm = Form.form(ClientCreateForm.class);

        return showCreateClient(clientCreateForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateClient() {
        Form<ClientCreateForm> clientCreateForm = Form.form(ClientCreateForm.class).bindFromRequest();

        if (formHasErrors(clientCreateForm)) {
            return showCreateClient(clientCreateForm);
        }

        ClientCreateForm clientCreateData = clientCreateForm.get();
        Client client = clientService.createClient(clientCreateData.name, clientCreateData.applicationType, clientCreateData.scopes, Arrays.asList(clientCreateData.redirectURIs.split(",")), getCurrentUserJid(), getCurrentUserIpAddress());

        addActivityLog(BasicActivityKeys.CREATE.construct(CLIENT, client.getJid(), clientCreateData.name));

        return redirect(routes.ClientController.index());
    }

    @Transactional
    @AddCSRFToken
    public Result editClient(long clientId) throws ClientNotFoundException {
        Client client = clientService.findClientById(clientId);
        ClientEditForm clientEditData = new ClientEditForm();

        clientEditData.name = client.getName();
        clientEditData.redirectURIs = StringUtils.join(client.getRedirectURIs(), ",");
        clientEditData.scopes = Lists.newArrayList(client.getScopes());

        Form<ClientEditForm> clientEditForm = Form.form(ClientEditForm.class).fill(clientEditData);

        return showEditClient(client, clientEditForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditClient(long clientId) throws ClientNotFoundException {
        Client client = clientService.findClientById(clientId);

        Form<ClientEditForm> clientEditForm = Form.form(ClientEditForm.class).bindFromRequest();
        if (formHasErrors(clientEditForm)) {
            return showEditClient(client, clientEditForm);
        }

        ClientEditForm clientEditData = clientEditForm.get();
        clientService.updateClient(client.getJid(), clientEditData.name, clientEditData.scopes, Arrays.asList(clientEditData.redirectURIs.split(",")), getCurrentUserJid(), getCurrentUserIpAddress());

        if (!client.getName().equals(clientEditData.name)) {
            addActivityLog(BasicActivityKeys.RENAME.construct(CLIENT, client.getJid(), client.getName(), clientEditData.name));
        }
        addActivityLog(BasicActivityKeys.EDIT.construct(CLIENT, client.getJid(), clientEditData.name));

        return redirect(routes.ClientController.index());
    }

    @Override
    protected HtmlTemplate getBaseHtmlTemplate() {
        HtmlTemplate template = super.getBaseHtmlTemplate();

        template.markBreadcrumbLocation(Messages.get("client.text.clients"), routes.ClientController.index());

        return template;
    }

    protected HtmlTemplate getBaseHtmlTemplate(Client client) {
        HtmlTemplate template = getBaseHtmlTemplate();

        template.markBreadcrumbLocation(client.getName(), routes.ClientController.viewClient(client.getId()));
        template.setMainTitle("#" + client.getId() + ": " + client.getName());
        template.setPageTitle(client.getName());

        return template;
    }

    private Result showListClients(Page<Client> pageOfClients, String orderBy, String orderDir, String filterString) {
        HtmlTemplate template = getBaseHtmlTemplate();

        template.setContent(listClientsView.render(pageOfClients, orderBy, orderDir, filterString));
        template.setMainTitle(Messages.get("client.text.list"));
        template.addMainButton(Messages.get("client.button.new"), routes.ClientController.createClient());

        return renderTemplate(template);
    }

    private Result showViewClient(Client client) {
        HtmlTemplate template = getBaseHtmlTemplate(client);

        template.setContent(viewClientView.render(client));

        return renderTemplate(template);
    }

    private Result showCreateClient(Form<ClientCreateForm> clientCreateForm) {
        HtmlTemplate template = getBaseHtmlTemplate();

        template.setContent(createClientView.render(clientCreateForm));
        template.setMainTitle(Messages.get("client.text.new"));
        template.markBreadcrumbLocation(Messages.get("commons.text.new"), routes.ClientController.createClient());

        return renderTemplate(template);
    }

    private Result showEditClient(Client client, Form<ClientEditForm> clientEditForm) {
        HtmlTemplate template = getBaseHtmlTemplate(client);

        template.setContent(editClientView.render(client, clientEditForm));
        template.markBreadcrumbLocation(Messages.get("commons.text.edit"), routes.ClientController.editClient(client.getId()));

        return renderTemplate(template);
    }
}
