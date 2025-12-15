package logic;

import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private List<Observer> observers = new ArrayList<>();

    public void subscribe(Observer obs) {
        observers.add(obs);
    }

    public void notify(String eventType) {
        for (Observer obs : observers) {
            obs.update(eventType);
        }
    }
}