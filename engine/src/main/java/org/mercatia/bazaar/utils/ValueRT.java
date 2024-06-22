package org.mercatia.bazaar.utils;

public class ValueRT extends Range.RangeType<ValueRT> {
    public double value;

    public static ValueRT of(double value) {
        return new ValueRT(value);
    }

    public ValueRT(double value) {
        this.value = value;
    }

    @Override
    public double as() {
        return value;
    }

    @Override
    public ValueRT add(ValueRT other) {
        if (other==null){
            return this;
        }
        return of(value + other.as());
    }

    @Override
    public ValueRT subtract(ValueRT other) {
        if (other==null){
            return this;
        }

        return of(value - other.as());
    }

    @Override
    public ValueRT multiply(ValueRT other) {        
        return of(value * other.as());
    }

    @Override
    public ValueRT multiply(double other) {
        return of(value + other);
    }

    @Override
    public ValueRT toNew(double f) {
       return new ValueRT(f);
    }

    @Override
    public boolean zeroOrLess() {
        return value<=0.0f;
    }

    @Override
    public boolean zeroOrGreater() {
       return value>=0.0f;
    }

    @Override
    public boolean greater(ValueRT other) {
        return value>other.value;
    }

    @Override
    public boolean less(ValueRT other) {
        return value<other.value;
    }

}
