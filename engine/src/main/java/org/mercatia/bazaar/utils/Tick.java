package org.mercatia.bazaar.utils;

public class Tick {
    private int tick = 0;

    private Tick() {

    }

    public Tick increment() {
        tick++;
        return this;
    }

    public String toString() {
        return "T[" + tick + "]";
    }

    public Tick pointInTime(){
        var t = new Tick();
        t.tick= this.tick;
        return t;
    }

    private static Tick theTick = new Tick();

    public static Tick getTick(){
        return theTick;
    }

}
