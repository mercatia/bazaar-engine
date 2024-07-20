package org.mercatia.bazaar;

public abstract class AbstractFactory {
   

    protected Bootstrap bootstrap;

    public AbstractFactory withBootstrap(Bootstrap bootstrap){
        this.bootstrap = bootstrap;
        return this;
    }   
}
