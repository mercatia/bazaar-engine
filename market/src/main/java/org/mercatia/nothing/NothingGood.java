package org.mercatia.nothing;

import java.util.List;

import org.mercatia.bazaar.goods.Good;
import org.mercatia.bazaar.goods.GoodFactory;

public class NothingGood extends Good {

    public NothingGood(GoodType type) {
        super(type);
    }

    public static enum NothingGoods implements Good.GoodType {
        WIDGET,
        THINGY;

        @Override
        public String label() {
            return this.name();
        }

    }

    public static class Factory extends GoodFactory {

        @Override
        public Good buildGood() {
            return new NothingGood(type);
        }

        @Override
        public List<GoodType> listTypes() {
            return List.of(NothingGoods.WIDGET, NothingGoods.THINGY);
        }

    }

}
