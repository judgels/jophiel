package org.iatoki.judgels.jophiel;

public final class JophielJedisModule extends JophielModule {

    @Override
    protected String getDaosImplPackage() {
        return "org.iatoki.judgels.jophiel.models.daos.jedishibernate";
    }
}
