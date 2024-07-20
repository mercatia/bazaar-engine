package org.mercatia.danp.goods;

import java.util.List;

import org.mercatia.bazaar.goods.Good;
import org.mercatia.bazaar.goods.GoodFactory;
import org.mercatia.bazaar.goods.Good.GoodType;
import org.mercatia.danp.DoranParberryEconomy.DPEGoods;

public class DPGoodFactory extends GoodFactory {

    @Override
    public Good buildGood() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GoodType> listTypes() {
        return List.of(DPEGoods.values());
    }

}