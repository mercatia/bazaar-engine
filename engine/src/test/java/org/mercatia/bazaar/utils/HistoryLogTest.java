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

    public static class TestKey implements Typed {

        String label;

        public TestKey(String label) {
            this.label = label;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((label == null) ? 0 : label.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TestKey other = (TestKey) obj;
            if (label == null) {
                if (other.label != null)
                    return false;
            } else if (!label.equals(other.label))
                return false;
            return true;
        }

    }

    @Test
    public void testBasic() {
        new HistoryLog<>(HistoryClass.Ask);
        new HistoryLog<>(HistoryClass.Bid);
        new HistoryLog<>(HistoryClass.Price);
        new HistoryLog<>(HistoryClass.Profit);
        new HistoryLog<>(HistoryClass.Trade);
    }

    @Test
    public void testAddGet() {
        var hl = new HistoryLog<TestKey, Money>(HistoryClass.Price);

        assertEquals(null, hl.get(new TestKey("fred")));

        hl.add(new TestKey("fred"), Money.from(Currency.DEFAULT, 3.14));
        assertEquals(Arrays.asList(Money.from(Currency.DEFAULT, 3.14)), hl.get(new TestKey("fred")));

    }

    @Test
    public void testAverage_I() {
        var m1 = Money.from(Currency.DEFAULT, 3);

        var hl = new HistoryLog<TestKey, Money>(HistoryClass.Price);
        assertNull(hl.average(new TestKey("fred"), 1));
        assertNull(hl.average(new TestKey("fred"), 0));
        assertNull(hl.average(new TestKey("fred"), -1));

        assertNull(hl.average(new TestKey("av1"), 1));

        hl.add(new TestKey("av1"), m1);
        assertNull(hl.average(new TestKey("av1"), 0));
        assertNull(hl.average(new TestKey("av1"), -1));

        var c1 = m1.clone();
        assertEquals(c1, hl.average(new TestKey("av1"), 1));
        hl.add(new TestKey("av1"), m1.clone());
        hl.add(new TestKey("av1"), m1.clone());
        hl.add(new TestKey("av1"), m1.clone());
        assertEquals(c1, hl.average(new TestKey("av1"), 4));

    }

    @Test
    public void testAverage_II() {

        var hl = new HistoryLog<TestKey, Money>(HistoryClass.Price);

        var mxs = new ArrayList<Money>();
        var c = 0;
        for (var x = 0; x < 15; x++) {
            var mx = Money.from(Currency.DEFAULT, x);
            hl.add(new TestKey("mx"), mx);
            mxs.add(mx.clone());
            c += x;
        }

        System.err.println(c + " " + mxs.size() + " " + c / mxs.size());
        assertEquals(mxs, hl.get(new TestKey("mx")));
        assertEquals(Money.from(Currency.DEFAULT, 14.0), hl.average(new TestKey("mx"), 1));
        assertEquals(Money.from(Currency.DEFAULT, 13.5), hl.average(new TestKey("mx"), 2));
        assertEquals(Money.from(Currency.DEFAULT, 7.0), hl.average(new TestKey("mx"), 15));
        assertEquals(Money.from(Currency.DEFAULT, 7.0), hl.average(new TestKey("mx"), 25));
    }

}
