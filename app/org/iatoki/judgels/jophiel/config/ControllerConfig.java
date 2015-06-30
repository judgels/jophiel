package org.iatoki.judgels.jophiel.config;

import org.iatoki.judgels.jophiel.controllers.JophielClientController;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * Created by Deny Prasetyo
 * 26 May 2015
 * Principal Software Development Engineer
 * GDP Labs
 * deny.prasetyo@gdplabs.id
 */

@Configuration
@ComponentScan(value = {
        "org.iatoki.judgels.jophiel.controllers"
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE
                ,value = {
                JophielClientController.class
        })
})
public class ControllerConfig {

}
