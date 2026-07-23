package moe.plushie.armourers_workshop.core.utils;

import java.util.List;

public class Scheduler {

    public static final Scheduler SERVER = new Scheduler();
    public static final Scheduler CLIENT = new Scheduler();

    private List<Runnable> nextTickCallbacks;
    private List<Runnable> postTickCallbacks;

    public void begin() {
        // ignore when no task
        if (nextTickCallbacks == null) {
            return;
        }
        var tasks = nextTickCallbacks;
        nextTickCallbacks = null;
        tasks.forEach(Runnable::run);
    }

    public void end() {
        // ignore when no task
        if (postTickCallbacks == null) {
            return;
        }
        var tasks = postTickCallbacks;
        postTickCallbacks = null;
        tasks.forEach(Runnable::run);
    }

    public void addNextTickCallback(Runnable handler) {
        if (nextTickCallbacks == null) {
            nextTickCallbacks = Collections.newList(handler);
        } else {
            nextTickCallbacks.add(handler);
        }
    }

    public void addPostTickCallback(Runnable handler) {
        if (postTickCallbacks == null) {
            postTickCallbacks = Collections.newList(handler);
        } else {
            postTickCallbacks.add(handler);
        }
    }
}
