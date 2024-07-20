package org.mercatia.bazaar.goods;

import java.util.List;
import org.mercatia.bazaar.AbstractFactory;
import org.mercatia.bazaar.goods.Good.GoodType;

public abstract class GoodFactory extends AbstractFactory {
    protected Good.GoodType type;

    public Good build() {
        var g = buildGood();
        return g;
    }

    public GoodFactory withType(GoodType type) {
        this.type = type;
        return this;
    }

    public abstract Good buildGood();

    public abstract List<GoodType> listTypes();
}