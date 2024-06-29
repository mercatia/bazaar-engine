package org.mercatia.bazaar.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mercatia.bazaar.currency.Currency;
import org.mercatia.bazaar.currency.Money;

public class HistoryLogTest {

    @Test
    public void testBasic() {
        new HistoryLog<>(EconNoun.Ask);
        new HistoryLog<>(EconNoun.Bid);
        new HistoryLog<>(EconNoun.Price);
        new HistoryLog<>(EconNoun.Profit);
        new HistoryLog<>(EconNoun.Trade);
    }

    @Test
    public void testAddGet() {
        var hl = new HistoryLog<Money>(EconNoun.Price);

        assertNull(hl.get("fred"));
        hl.add("fred", Money.from(Currency.DEFAULT, 3.14));
        assertEquals(null, hl.get("fred"));

        hl.register("fred");
        assertEquals(Collections.EMPTY_LIST, hl.get("fred"));
        hl.add("fred", Money.from(Currency.DEFAULT, 3.14));
        assertEquals(Arrays.asList(Money.from(Currency.DEFAULT, 3.14)), hl.get("fred"));

    }

    @Test
    public void testAverage() {
        var m1 = Money.from(Currency.DEFAULT, 3);

        var hl = new HistoryLog<Money>(EconNoun.Price);
        assertNull(hl.average("fred", 1));
        assertNull(hl.average("fred", 0));
        assertNull(hl.average("fred", -1));

        hl.register("av1");
        assertNull(hl.average("av1", 1));

        hl.add("av1", m1);
        assertNull(hl.average("av1", 0));
        assertNull(hl.average("av1", -1));

        var c1 = m1.clone();
        assertEquals(c1, hl.average("av1", 1));
        hl.add("av1", m1.clone());
        hl.add("av1", m1.clone());
        hl.add("av1", m1.clone());
        assertEquals(c1, hl.average("av1", 4));

        hl.register("mx");
        var mxs = new ArrayList<Money>();
        var c = 0;
        for (var x = 0; x < 15; x++) {
            var mx = Money.from(Currency.DEFAULT, x);
            hl.add("mx", mx);
            mxs.add(mx.clone());
            c+=x;
        }

        System.err.println(c+" "+mxs.size()+" "+c/mxs.size());
        assertEquals(mxs, hl.get("mx"));
        assertEquals(Money.from(Currency.DEFAULT, 14.0), hl.average("mx", 1));
        assertEquals(Money.from(Currency.DEFAULT, 13.5), hl.average("mx", 2));
        assertEquals(Money.from(Currency.DEFAULT, 7.0), hl.average("mx", 15));
        assertEquals(Money.from(Currency.DEFAULT, 7.0), hl.average("mx", 25));
    }

}
