package org.mercatia.bazaar.utils;

import org.mercatia.Jsonable;

public class Range<T extends Range.RangeType<T>> implements Jsonable {

    public static abstract class RangeType<F> implements Jsonable, Cloneable {
        public abstract F add(F other);

        public abstract F subtract(F other);

        public abstract F multiply(F other);

        public abstract F multiply(double other);

        public abstract double as();

        public abstract boolean zeroOrLess();

        public abstract boolean zeroOrGreater();

        public abstract boolean greater(F other);

        public abstract boolean less(F other);

        public abstract F toNew(double value);

        public abstract F clone();
    }

    public static enum LIMIT {
        LOWER, UPPER
    };

    private T lower;
    private T upper;
    private T lowerLimit = null;
    private T upperLimit = null;

    public Range(T lower, T upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public Range(T lower, T upper, T lowerLimit, T upperLimit) {
        this(lower, upper);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;

    }

    private void limit() {
        if (lowerLimit != null && lower.less(lowerLimit)) {
            this.lower = lowerLimit.clone();
        }

        if (upperLimit != null && upper.greater(upperLimit)) {
            this.upper = upperLimit.clone();
        }

    }

    public void drop(T amount, LIMIT l) {

        if (l == LIMIT.LOWER) {
            this.lower =  this.lower.subtract(amount);
        } else {
            this.upper = this.upper.subtract(amount);
        }

        limit();
    }

    public void drop(T amount) {
        drop(amount, LIMIT.LOWER);
        drop(amount, LIMIT.UPPER);
    }

    public void raise(T amount, LIMIT l) {
        if (l == LIMIT.LOWER) {
            this.lower = this.lower.add(amount);
        } else {
            this.upper = this.upper.add(amount);
        }

        limit();
    }

    public void raise(T amount) {
        raise(amount,LIMIT.UPPER);
        raise(amount,LIMIT.LOWER);
    }

    public void increasePc(float f) {
        this.lower = this.lower.multiply(f);
        this.upper = this.upper.multiply(f);

        limit();
    }

    public void setLower(T lower) {
        this.lower = lower;
        limit();
    }

    public void setUpper(T upper) {
        this.upper = upper;

        limit();
    }

    public T getLower() {
        return lower;
    }

    public T getUpper() {
        return upper;
    }

    public T mean() {
        return (T) (lower.add(upper).multiply(0.5));
    }

    public T randomInRange() {
        var min = lower.as();
        var max = upper.as();

        double v = (double) (Math.random() * (max - min)) + min;
        T value = lower.toNew(v);

        return value;
    }

    public double positionInRange(T value) {
        return _positionInRange(value.as(), lower.as(), upper.as(), false);
    }

    private double _positionInRange(double value, double min, double max, boolean clamp) {
        value -= min;
        max -= min;
        min = 0;
        value = (value / (max - min));
        if (clamp) {
            if (value < 0) {
                value = 0;
            }
            if (value > 1) {
                value = 1;
            }
        }
        return value;
    }

    public String toString() {
        return this.lower + "__" + this.upper;
    }

    private record J(double lower, double upper) implements Jsony {
    };

    @Override
    public Jsony jsonify() {
        return new J(lower.as(), upper.as());
    }

}
