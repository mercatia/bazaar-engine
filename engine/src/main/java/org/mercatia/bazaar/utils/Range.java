package org.mercatia.bazaar.utils;

public class Range<T extends Range.RangeType<T>> {

    public static abstract class RangeType<F> {
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
    }

    public static enum LIMIT {
        LOWER, UPPER
    };

    private T lower;
    private T upper;

    public Range(T lower, T upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public void drop(T amount, LIMIT l) {
        if (l == LIMIT.LOWER) {
            this.lower = this.lower.subtract(amount);
        } else {
            this.upper = this.upper.subtract(amount);
        }
    }

    public void drop(T amount) {
        this.lower = this.lower.subtract(amount);
        this.upper = this.upper.subtract(amount);
    }

    public void raise(T amount, LIMIT l) {
        if (l == LIMIT.LOWER) {
            this.lower = this.lower.add(amount);
        } else {
            this.upper = this.upper.add(amount);
        }
    }

    public void raise(T amount) {
        this.lower = this.lower.add(amount);
        this.upper = this.upper.add(amount);
    }

    public void increasePc(float f) {
        this.lower = this.lower.multiply(f);
        this.upper = this.upper.multiply(f);
    }

    public void setLower(T lower) {
        this.lower = lower;
    }

    public void setUpper(T upper) {
        this.upper = upper;
    }

    public T getLower() {
        return lower;
    }

    public T getUpper() {
        return upper;
    }

    public T mean() {
        return (T) (lower.add(upper).multiply(0.5f));
    }

    public T randomInRange() {
        var a = lower.as();
        var b = upper.as();

        double min = a < b ? a : b;
        double max = a < b ? b : a;

        T value = lower.toNew((float) (Math.random() * (max - min)) + min);

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

}
