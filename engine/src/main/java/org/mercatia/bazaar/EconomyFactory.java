package org.mercatia.bazaar;

public abstract class EconomyFactory extends AbstractFactory{
    protected String name;

    public EconomyFactory withName(String name) {
        this.name = name;
        return this;
    }

    public abstract Economy build();
}
