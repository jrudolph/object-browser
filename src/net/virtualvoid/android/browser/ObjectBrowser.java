/*
    This file is part of the object-browser.

    The object-browser is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    The object-browser is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the object-browser.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2009 Johannes Rudolph
*/

package net.virtualvoid.android.browser;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Application;

@Textual("ObjectBrowser{history-depth = #history.size}")
public class ObjectBrowser extends Application{
    LinkedList<HistoryItem> history = new LinkedList<HistoryItem>();
    ArrayList<Object> saved = new ArrayList<Object>();
    Home mycastle = new Home(this);

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

    private final static int MAX_HISTORY = 25;

    @Override
    public void onCreate() {
        super.onCreate();
        switchTo(mycastle,0);
    }

    Home getHome(){
        return mycastle;
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

    public void addSaved(Object object) {
        saved.add(object);
    }
}
