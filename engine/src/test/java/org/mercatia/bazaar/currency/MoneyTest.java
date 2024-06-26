package org.mercatia.bazaar.currency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.math.*;

public class MoneyTest {

    @Test
    public void testBasic() {
        assertEquals(Money.NONE().as(), 0.0f);
    }

    @Test
    public void testSum() {
        var builder = Money.builder();
        assertNotNull(builder);

        for (var x = 0; x < 105; x++) {
            var y = 105 - x;
            var mx = builder.currency(Currency.DEFAULT).unit(0).fractional(x).build();
            var my = builder.currency(Currency.DEFAULT).unit(0).fractional(y).build();
            assertNotNull(mx);
            assertNotNull(my);

            assertEquals(0.0, mx.subtract(mx).as());
            assertEquals(1.05, mx.add(my).as());
        }
    }

    @Test
    public void testSumDouble() {
        var builder = Money.builder();
        assertNotNull(builder);

        var x1 = new BigDecimal(0.0);
        var y1 = new BigDecimal(1.05);
        var yf = new BigDecimal(1.05);
        for (var x = 0; x < 105; x++) {

            // var y = 1.05f-x;

            x1 = x1.add(new BigDecimal(0.01));
            y1 = yf.subtract(x1);

            var mx = Money.from(Currency.DEFAULT, x1.floatValue());
            var my = Money.from(Currency.DEFAULT, y1.floatValue());
            assertNotNull(mx);
            assertNotNull(my);
            // System.err.println(String.format("%3d %02f %02f  mx=%02f my=%02f",x,x1,y1,mx.as(),my.as()));

            assertEquals(0.0, mx.subtract(mx).as());
            assertEquals(1.05, mx.add(my).as());
            assertEquals("1.05", String.format("%01.2f", mx.as() + my.as()));

        }
    }

    @Test
    public void testAverage() {
        var builder = Money.builder();
        assertNotNull(builder);

        var x1 = new BigDecimal(0.0);
        var yf = new BigDecimal(1.05);
        for (var x = 0; x < 105; x++) {

            x1 = x1.add(new BigDecimal(0.01));
            var y1 = yf.subtract(x1);

            
            var mx = Money.from(Currency.DEFAULT,y1.doubleValue());
            var my = Money.from(Currency.DEFAULT,y1.doubleValue());

            var a = mx.average(my);
            var b = my.average(mx);
            assertEquals(a,b);
            assertEquals(0,a.subtract(b).as());

        }
    }

    @Test
    public void testMultiply() {
        var builder = Money.builder();
        assertNotNull(builder);

        var x1 = new BigDecimal(0.0);
        var yf = new BigDecimal(0.5);
        var my = Money.from(Currency.DEFAULT,yf.doubleValue());
        for (var x = 0; x < 105; x++) {

            x1 = x1.add(new BigDecimal(0.1));
           
           
            var mx = Money.from(Currency.DEFAULT,x1.doubleValue());
            var p = mx.multiply(my);
            System.out.println(mx+" "+p);

            // var a = mx.average(my);
            // var b = my.average(mx);
            // assertEquals(a,b);
            // assertEquals(0,a.subtract(b).as());

        }
    }
}
