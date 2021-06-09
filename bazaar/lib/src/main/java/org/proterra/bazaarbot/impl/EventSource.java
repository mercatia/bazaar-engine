package org.proterra.bazaarbot.impl;
import java.util.EventObject;
import java.util.EventListener;
import java.util.*;


public class EventSource {
    private List<EventListener> listeners = new ArrayList<EventListener>();

    public void addListener(EventListener listener){
        listeners.add(listener);
    }

    public void fireEvent(EventObject event){
        for (PropertyChangeListener name : listener) {
            name.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }

}
