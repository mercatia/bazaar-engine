package org.mercatia.nothing;

import java.util.List;

import org.mercatia.bazaar.Economy;
import org.mercatia.bazaar.goods.Good;
import org.mercatia.bazaar.market.Market;
import org.mercatia.bazaar.market.MarketFactory;
import org.mercatia.nothing.NothingGood.NothingGoods;

public class NothingMarket extends Market {

    public NothingMarket(String name, Economy economy) {
        super(name, economy);
    }

    public static class Factory extends MarketFactory {

        @Override
        public Market build() {
            return new NothingMarket(name, economy);
        }

    }

    @Override
    public Jsony jsonify() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'jsonify'");
    }

    @Override
    public List<Good.GoodType> getGoodsTraded() {
        return List.of(NothingGoods.THINGY, NothingGoods.WIDGET);
    }

}
