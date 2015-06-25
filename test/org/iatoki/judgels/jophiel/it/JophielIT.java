package org.iatoki.judgels.jophiel.it;

import org.fluentlenium.core.Fluent;
import org.iatoki.judgels.jophiel.JophielTestProperties;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import play.libs.F;
import play.test.Helpers;
import play.test.TestBrowser;
import play.test.TestServer;

public final class JophielIT {

    private static final int TEST_PORT = 3333;
    private static final String TEST_URL = "http://localhost:" + TEST_PORT;
    private TestServer testServer;

    @BeforeTest
    public void startServer() {
        testServer = Helpers.testServer(TEST_PORT);
        testServer.start();

        JophielTestProperties.buildInstance(testServer.application().configuration().underlying());
    }

    @AfterTest
    public void stopServer() {
        testServer.stop();
    }

    @Test
    public void listUsers() {
        TestBrowser testBrowser = Helpers.testBrowser(Helpers.FIREFOX);
        try {
            F.Callback<TestBrowser> callback = browser -> {
                Fluent fluent = browser.goTo(TEST_URL);
                long startMillis = System.currentTimeMillis();
                fluent.await().untilPage().isLoaded();
                fillAndSubmitLoginCredentials(fluent);
                fluent.await().untilPage().isLoaded();
                fluent.goTo(TEST_URL+"/users");
                fluent.await().untilPage().isLoaded();
                System.out.println("Open List User Page: " + (System.currentTimeMillis() - startMillis));
            };
            callback.invoke(testBrowser);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            testBrowser.quit();
        }
    }

    @Test
    public void openCreateUserPage() {
        TestBrowser testBrowser = Helpers.testBrowser(Helpers.FIREFOX);
        try {
            F.Callback<TestBrowser> callback = browser -> {
                Fluent fluent = browser.goTo(TEST_URL);
                long startMillis = System.currentTimeMillis();
                fluent.await().untilPage().isLoaded();
                fillAndSubmitLoginCredentials(fluent);
                fluent.await().untilPage().isLoaded();
                fluent.goTo(TEST_URL + "/users/create");
                fluent.await().untilPage().isLoaded();
                System.out.println("Open Create User Page: " + (System.currentTimeMillis() - startMillis));
            };
            callback.invoke(testBrowser);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            testBrowser.quit();
        }
    }

    @Test
    public void login() {
        TestBrowser testBrowser = Helpers.testBrowser(Helpers.FIREFOX);
        try {
            F.Callback<TestBrowser> callback = browser -> {
                Fluent fluent = browser.goTo(TEST_URL);
                long startMillis = System.currentTimeMillis();
                fluent.await().untilPage().isLoaded();
                fillAndSubmitLoginCredentials(fluent);
                fluent.await().untilPage().isLoaded();
                System.out.println("Login: " + (System.currentTimeMillis() - startMillis));
            };
            callback.invoke(testBrowser);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            testBrowser.quit();
        }
    }

    @Test
    public void openHomePage() {
        TestBrowser testBrowser = Helpers.testBrowser(Helpers.FIREFOX);
        try {
            F.Callback<TestBrowser> callback = browser -> {
                Fluent fluent = browser.goTo(TEST_URL);
                long startMillis = System.currentTimeMillis();
                fluent.await().untilPage().isLoaded();
                System.out.println("Open Home Page: " + (System.currentTimeMillis() - startMillis));
            };
            callback.invoke(testBrowser);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            testBrowser.quit();
        }
    }

    private void fillAndSubmitLoginCredentials(Fluent fluent) {
        fluent.fill("#usernameOrEmail").with(JophielTestProperties.getInstance().getTestUsername());
        fluent.fill("#password").with(JophielTestProperties.getInstance().getTestPassword());
        fluent.submit(".row.login-content form");
    }
}
