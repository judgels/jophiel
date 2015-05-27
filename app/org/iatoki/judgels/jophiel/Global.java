package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.config.ControllerConfig;
import org.iatoki.judgels.jophiel.config.PersistenceConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import play.Application;

public final class Global extends org.iatoki.judgels.commons.Global {
    private ApplicationContext applicationContext;

    @Override
    public void onStart(Application application) {
        applicationContext = new AnnotationConfigApplicationContext(PersistenceConfig.class, ControllerConfig.class);
        super.onStart(application);
    }

    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        return applicationContext.getBean(controllerClass);
    }

}