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

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

@Textual("ObjectBrowser{history-depth = #history.size}")
public class ObjectBrowser {
    LinkedList<HistoryItem> history = new LinkedList<HistoryItem>();
    ArrayList<Object> saved = new ArrayList<Object>();
    Home mycastle = new Home(this);
    private Context ctx;

    public ObjectBrowser(Context ctx) {
        this.ctx = ctx;
    }

    static class HistoryItem{
        Object object;
        long listPosition;
        String path;
        public HistoryItem(Object object,String path) {
            super();
            this.object = object;
            this.path = path;
        }
        @Override
        public String toString() {
            return path;
        }
    }
    private static HistoryItem history(Object object,String path){
        return new HistoryItem(object,path);
    }

    private final static int MAX_HISTORY = 25;

    public Resources getResources(){
        return ctx.getResources();
    }
    public PackageManager getPackageManager(){
        return ctx.getPackageManager();
    }
    public AssetManager getAssets(){
        return ctx.getAssets();
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
        return switchTo(o, "",pos);
    }
    HistoryItem switchTo(Object o,String path,long pos){
        String pathPrefix;

        if (!history.isEmpty()){
            getCurrent().listPosition = pos;
            pathPrefix = getCurrent().path + ".";
        }
        else
            pathPrefix = "";

        history.add(history(o,pathPrefix + path));

        while (history.size() > MAX_HISTORY)
            history.remove(0);

        return getCurrent();
    }
    HistoryItem move(Item i,long pos){
        try {
            return switchTo(i.get(),i.getPath(),pos);
        } catch (Exception e) {
            // FAIL more noisily
            e.printStackTrace();
            return switchTo(e,i.getPath(),pos);
        }
    }

    public void addSaved(Object object) {
        saved.add(object);
    }
}
