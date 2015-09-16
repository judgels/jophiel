package org.iatoki.judgels.jophiel.config;

public class JophielJedisModule extends JophielModule {

    @Override
    protected String getDaosImplPackage() {
        return "org.iatoki.judgels.jophiel.models.daos.jedishibernate";
    }
}
