package org.proterra.events;
import java.util.ArrayList;
import java.util.List;


public interface EventsOrigin {
    final List<EventsListener> listeners = new ArrayList<EventsListener>();

    public default void addListener(EventsListener listener){
        listeners.add(listener);
    }

    public default void fireEvent(EventsObject event){
        for (EventsListener l : listeners) {
            event.dispatch(l);
        }
    }

}
