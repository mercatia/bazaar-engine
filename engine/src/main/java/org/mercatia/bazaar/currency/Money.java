package org.mercatia.bazaar.currency;

public class Money {
    
    private Currency currency;

    private long unit;
    private long fractional;

    protected Money(){

    }

    protected Money(Currency currency, long unit, long fractional){
        this.currency = currency;
        this.unit = unit;
        this.fractional = fractional;
    }


    public static class Builder {
        Money m;
        private Builder(){
            this.m = new Money();
        }

        public Builder currency(Currency c){
            m.currency = c;
            return this;
        }

        public Builder unit(long u){
            m.addUnit(u);
            return this;
        }

        public Builder fractional(long u){
            m.addFractional(u);
            return this;
        }

        public Money build(){
            return m;
        }
    }


    public void addUnit(long u) {
        this.unit += u;
    }

    public Money addFractional(long u) {

        var f = u % 100;
        var u1 = u - f;

        var f2 = (this.fractional+f);
        this.fractional = f2 % 100;

        this.unit = u1 + this.unit + f2-this.fractional;

        return this;
    }

    public String toString(){
        return unit+":"+fractional;
    }


    public static void main(String args[]){
        var m1 = new Money.Builder().unit(14).fractional(67).build();
        System.out.println(m1);

        var m2 = new Money.Builder().unit(14).fractional(67).build();
        System.out.println(m2.addFractional(40));

        // ASMOA
    }
}
