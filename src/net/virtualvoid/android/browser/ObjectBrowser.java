package net.virtualvoid.android.browser;

import java.util.LinkedList;

import android.app.Application;

public class ObjectBrowser extends Application{
    LinkedList<Object> history = new LinkedList<Object>();
    private final static int MAX_HISTORY = 10;

    @Override
    public void onCreate() {
        super.onCreate();
        switchTo(this);
    }

    Object getCurrent(){
        return history.getLast();
    }
    Object toPrevious(){
        if (hasPrevious())
            history.removeLast();
        return getCurrent();
    }
    boolean hasPrevious(){
        return history.size() > 1;
    }
    Object switchTo(Object o){
        history.add(o);

        while (history.size() > MAX_HISTORY)
            history.remove(0);

        return o;
    }
    Object move(Item i){
        try {
            return switchTo(i.get());
        } catch (Exception e) {
            // FAIL more noisily
            e.printStackTrace();
            return getCurrent();
        }
    }
}
