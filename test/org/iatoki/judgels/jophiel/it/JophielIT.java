package org.iatoki.judgels.jophiel.it;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.fluentlenium.core.Fluent;
import org.iatoki.judgels.play.JudgelsPlayProperties;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.JophielTestProperties;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import play.Application;
import play.test.Helpers;
import play.test.TestBrowser;
import play.test.WithServer;

import java.util.Map;
import java.util.stream.Collectors;

public final class JophielIT extends WithServer {

    private String testURL;
    private TestBrowser browser;

    @Override
    protected Application provideApplication() {
        return Helpers.fakeApplication(ConfigFactory.load().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @BeforeTest
    public void beforeTest() {
        org.iatoki.judgels.jophiel.BuildInfo$ buildInfo = org.iatoki.judgels.jophiel.BuildInfo$.MODULE$;
        JudgelsPlayProperties.buildInstance(buildInfo.name(), buildInfo.version(), ConfigFactory.load());

        Config config = ConfigFactory.load();
        JophielProperties.buildInstance(config);
        JophielTestProperties.buildInstance(config);

        this.startServer();
        testURL = "http://localhost:" + port;
    }

    @AfterTest
    public void afterTest() {
        this.stopServer();
        this.quitBrowser();
    }

    @AfterTest
    public void stopServer() {
        testServer.stop();
    }

    @BeforeMethod
    public void startBrowser() {
        this.browser = Helpers.testBrowser(Helpers.FIREFOX);
    }

    @AfterMethod
    public void quitBrowser() {
        browser.quit();
    }

    @Test
    public void listUsers() {
        Fluent fluent = browser.goTo(testURL);
        long startMillis = System.currentTimeMillis();
        fluent.await().untilPage().isLoaded();
        fillAndSubmitLoginCredentials(fluent);
        fluent.await().untilPage().isLoaded();
        fluent.goTo(testURL + "/users");
        fluent.await().untilPage().isLoaded();
        System.out.println("Open List User Page: " + (System.currentTimeMillis() - startMillis));
    }

    @Test
    public void openCreateUserPage() {
        Fluent fluent = browser.goTo(testURL);
        long startMillis = System.currentTimeMillis();
        fluent.await().untilPage().isLoaded();
        fillAndSubmitLoginCredentials(fluent);
        fluent.await().untilPage().isLoaded();
        fluent.goTo(testURL + "/users/create");
        fluent.await().untilPage().isLoaded();
        System.out.println("Open Create User Page: " + (System.currentTimeMillis() - startMillis));
    }

    @Test
    public void login() {
        Fluent fluent = browser.goTo(testURL);
        long startMillis = System.currentTimeMillis();
        fluent.await().untilPage().isLoaded();
        fillAndSubmitLoginCredentials(fluent);
        fluent.await().untilPage().isLoaded();
        System.out.println("Login: " + (System.currentTimeMillis() - startMillis));
    }

    @Test
    public void openHomePage() {
        Fluent fluent = browser.goTo(testURL);
        long startMillis = System.currentTimeMillis();
        fluent.await().untilPage().isLoaded();
        System.out.println("Open Home Page: " + (System.currentTimeMillis() - startMillis));
    }

    private void fillAndSubmitLoginCredentials(Fluent fluent) {
        fluent.fill("#usernameOrEmail").with(JophielTestProperties.getInstance().getTestUsername());
        fluent.fill("#password").with(JophielTestProperties.getInstance().getTestPassword());
        fluent.submit(".row.login-content form");
    }
}
