package org.iatoki.judgels.jophiel.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.play.JudgelsPlayProperties;
import org.iatoki.judgels.jophiel.JophielProperties;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;

public final class JophielApplicationLoader extends GuiceApplicationLoader {

    @Override
    public GuiceApplicationBuilder builder(Context context) {
        org.iatoki.judgels.jophiel.BuildInfo$ buildInfo = org.iatoki.judgels.jophiel.BuildInfo$.MODULE$;
        JudgelsPlayProperties.buildInstance(buildInfo.name(), buildInfo.version(), ConfigFactory.load());

        Config config = ConfigFactory.load();
        JophielProperties.buildInstance(config);

        return super.builder(context);
    }
}
