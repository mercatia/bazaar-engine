package org.mercatia.events;

import java.util.EventObject;

public abstract class EventsObject extends EventObject{
    public EventsObject(EventsOrigin origin){
        super(origin);
    }

    public abstract void dispatch(EventsListener listener);

}
