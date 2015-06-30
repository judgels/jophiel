package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.config.ControllerConfig;
import org.iatoki.judgels.jophiel.config.PersistenceConfig;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import play.Application;

import java.util.Optional;

public final class Global extends org.iatoki.judgels.commons.Global {

    private ApplicationContext applicationContext;

    /**
     * Initialize Spring Application Context at onStart Lifecycle
     *
     * @param application Application Object from Play Framework
     */
    @Override
    public void onStart(Application application) {
        applicationContext = new AnnotationConfigApplicationContext(PersistenceConfig.class, ControllerConfig.class);
        super.onStart(application);
    }

    /**
     * Base Play Framework Integration with Spring Context.
     * Controller Instance fetched from Spring Context.
     * Controller with composition Action will be fetched from Global Controller registry
     * Such as @EntityNotFoundGuard and @UnsupportedOperationGuard
     * <p>
     * See org.iatoki.judgels.commons.controllers.BaseController
     * See https://www.playframework.com/documentation/2.3.x/JavaActionsComposition
     * <p>
     *
     * @param controllerClass Class Definition to find
     * @return Controller Instance to used by Play Framework
     * @throws Exception
     */
    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        return getContextBean(controllerClass).orElse(super.getControllerInstance(controllerClass));
    }

    /**
     * Helper method to wrap applicationContext.getBean with Optional Container.
     * Return Optional.empty() if NoSuchDefinitionException throws to allow system finding instance from other registry
     * <p>
     * See https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html
     * See http://www.oracle.com/technetwork/articles/java/java8-optional-2175753.html
     *
     * @param controllerClass Class Definition to find
     * @return Object From Application Context or Empty if NoSuchDefinitionException throws.
     * @throws Exception When application context throws errors besides NoSuchBeanDefinition
     */
    private <A> Optional<A> getContextBean(Class<A> controllerClass) throws Exception {
        if (applicationContext == null) {
            throw new Exception("Application Context not Initialized");
        } else {
            try {
                return Optional.of(applicationContext.getBean(controllerClass));
            } catch (NoSuchBeanDefinitionException ex) {
                return Optional.empty();
            }
        }
    }
}
