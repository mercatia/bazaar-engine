package org.mercatia.bazaar;

import io.vertx.core.AbstractVerticle;

public class BootstrappedEntity extends AbstractVerticle {

    private Bootstrap bootstrap;

    public BootstrappedEntity() {

    }

    public BootstrappedEntity setBootstrap(Bootstrap bootstrap) {
        if (bootstrap == null) {
            this.bootstrap = bootstrap;
            return this;
        } else {
            throw new RuntimeException("Already bootstrapped");
        }
    }

    public Bootstrap getBootstrap() {
        return this.bootstrap;
    }

}
