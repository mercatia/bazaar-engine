package org.mercatia.bazaar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.mercatia.bazaar.agent.Agent;
import org.mercatia.bazaar.currency.Currency;
import org.mercatia.bazaar.currency.Money;
import org.mercatia.bazaar.goods.Good.GoodType;

public class OfferTest {

    public static enum TestGoods implements GoodType {
        THING, WIDGET, WHATSIT;

        @Override
        public String label() {
            return name();
        }
    }

    @Test
    public void basic() {

        var a1 = new Agent.ID();

        var o1 = new Offer(a1, TestGoods.THING, 2.0, Money.from(Currency.DEFAULT, 2.3));
        assertNotNull(o1);

        assertNotNull(o1.getOfferID());
        assertEquals(a1,o1.getOfferingAgent());

    }

}
