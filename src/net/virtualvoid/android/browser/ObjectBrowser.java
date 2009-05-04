package net.virtualvoid.android.browser;

import java.util.LinkedList;

import android.app.Application;

public class ObjectBrowser extends Application{
    LinkedList<HistoryItem> history = new LinkedList<HistoryItem>();

    static class HistoryItem{
        Object object;
        long listPosition;
        public HistoryItem(Object object) {
            super();
            this.object = object;
        }
    }
    private static HistoryItem history(Object object){
        return new HistoryItem(object);
    }

    private final static int MAX_HISTORY = 10;

    @Override
    public void onCreate() {
        super.onCreate();
        switchTo(this,0);
    }

    HistoryItem getCurrent(){
        return history.getLast();
    }
    HistoryItem toPrevious(){
        if (hasPrevious())
            history.removeLast();
        return getCurrent();
    }
    boolean hasPrevious(){
        return history.size() > 1;
    }
    HistoryItem switchTo(Object o,long pos){
        if (!history.isEmpty()){
            getCurrent().listPosition = pos;
        }

        history.add(history(o));

        while (history.size() > MAX_HISTORY)
            history.remove(0);

        return getCurrent();
    }
    HistoryItem move(Item i,long pos){
        try {
            return switchTo(i.get(),pos);
        } catch (Exception e) {
            // FAIL more noisily
            e.printStackTrace();
            return getCurrent();
        }
    }
}
