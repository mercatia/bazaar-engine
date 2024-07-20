package org.mercatia.bazaar.utils;

import org.mercatia.Jsonable;
import org.mercatia.bazaar.currency.Money;

public class QtyPrice implements Jsonable {

    protected double units;
    protected Money unit_price;

    public QtyPrice(double units, Money price) {
        this.units = units;
        this.unit_price = price;
    }

    public Money getUnitPrice() {
        return this.unit_price;
    }

    public double getUnits() {
        return units;
    }

    private record J(String units, Money unit_price) implements Jsony {
    };

    @Override
    public Jsony jsonify() {
        return new J(String.format("%.2f", this.units), unit_price);
    };

}
