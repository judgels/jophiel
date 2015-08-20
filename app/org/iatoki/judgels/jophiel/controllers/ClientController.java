package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.ClientNotFoundException;
import org.iatoki.judgels.jophiel.forms.ClientCreateForm;
import org.iatoki.judgels.jophiel.forms.ClientUpdateForm;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.Authorized;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.services.ClientService;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.views.html.client.createClientView;
import org.iatoki.judgels.jophiel.views.html.client.listClientsView;
import org.iatoki.judgels.jophiel.views.html.client.updateClientView;
import org.iatoki.judgels.jophiel.views.html.client.viewClientView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
@Singleton
@Named
public final class ClientController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final ClientService clientService;
    private final UserActivityService userActivityService;

    @Inject
    public ClientController(ClientService clientService, UserActivityService userActivityService) {
        this.clientService = clientService;
        this.userActivityService = userActivityService;
    }

    @Transactional
    public Result index() {
        return listClients(0, "id", "asc", "");
    }

    @Transactional
    @AddCSRFToken
    public Result createClient() {
        Form<ClientCreateForm> clientCreateForm = Form.form(ClientCreateForm.class);

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Try to create client <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

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
        clientService.createClient(clientCreateData.name, clientCreateData.applicationType, clientCreateData.scopes, Arrays.asList(clientCreateData.redirectURIs.split(",")));

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Create client " + clientCreateData.name + ".");

        return redirect(routes.ClientController.index());
    }

    @Transactional
    public Result viewClient(long clientId) throws ClientNotFoundException {
        Client client = clientService.findClientById(clientId);

        LazyHtml content = new LazyHtml(viewClientView.render(client));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("client.client") + " #" + clientId + ": " + client.getName(), new InternalLink(Messages.get("commons.update"), routes.ClientController.updateClient(clientId)), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("client.view"), routes.ClientController.viewClient(clientId))
        );
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Client - View");

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "View client " + client.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional
    public Result listClients(long page, String orderBy, String orderDir, String filterString) {
        Page<Client> pageOfClients = clientService.getPageOfClients(page, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listClientsView.render(pageOfClients, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("client.list"), new InternalLink(Messages.get("commons.create"), routes.ClientController.createClient()), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content);
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Clients");

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Open all clients <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional
    @AddCSRFToken
    public Result updateClient(long clientId) throws ClientNotFoundException {
        Client client = clientService.findClientById(clientId);
        ClientUpdateForm clientUpdateForm = new ClientUpdateForm();

        clientUpdateForm.name = client.getName();
        clientUpdateForm.redirectURIs = StringUtils.join(client.getRedirectURIs(), ",");
        clientUpdateForm.scopes = Lists.newArrayList(client.getScopes());

        Form<ClientUpdateForm> clientUpdateData = Form.form(ClientUpdateForm.class).fill(clientUpdateForm);

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Try to update client " + client.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateClient(clientUpdateData, clientId, client.getName());
    }

    @Transactional
    public Result postUpdateClient(long clientId) throws ClientNotFoundException {
        Client client = clientService.findClientById(clientId);

        Form<ClientUpdateForm> clientUpdateForm = Form.form(ClientUpdateForm.class).bindFromRequest();
        if (formHasErrors(clientUpdateForm)) {
            return showUpdateClient(clientUpdateForm, client.getId(), client.getName());
        }

        ClientUpdateForm clientUpdateData = clientUpdateForm.get();
        clientService.updateClient(client.getId(), clientUpdateData.name, clientUpdateData.scopes, Arrays.asList(clientUpdateData.redirectURIs.split(",")));

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Update client " + client.getName() + ".");

        return redirect(routes.ClientController.index());
    }

    @Transactional
    public Result deleteClient(long clientId) throws ClientNotFoundException {
        Client client = clientService.findClientById(clientId);
        clientService.deleteClient(client.getId());

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Delete client " + client.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.ClientController.index());
    }

    private Result showCreateClient(Form<ClientCreateForm> clientCreateForm) {
        LazyHtml content = new LazyHtml(createClientView.render(clientCreateForm));
        content.appendLayout(c -> headingLayout.render(Messages.get("client.create"), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("client.create"), routes.ClientController.createClient())
        );
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Client - Create");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateClient(Form<ClientUpdateForm> clientUpdateForm, long clientId, String clientName) {
        LazyHtml content = new LazyHtml(updateClientView.render(clientUpdateForm, clientId));
        content.appendLayout(c -> headingLayout.render(Messages.get("client.client") + " #" + clientId + ": " + clientName, c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("client.update"), routes.ClientController.updateClient(clientId))
        );
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Client - Update");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ImmutableList.builder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("client.clients"), routes.ClientController.index()));
        breadcrumbsBuilder.add(lastLinks);

        JophielControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
