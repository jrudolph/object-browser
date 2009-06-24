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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

@Textual("Home")
public class Home {
    private ObjectBrowser application;

    public Home(ObjectBrowser application) {
        this.application = application;
    }
    public ObjectBrowser getApplication(){
        return application;
    }

    public ArrayList<Object> getFavourites(){
        return application.saved;
    }
    public File getFileSystemRoot() throws IOException{
        return File.listRoots()[0].getCanonicalFile();
    }
    public ItemList getSystemDrawables(){
        return new ItemFactory.MappedArrayItemList<Field>("System icons",android.R.drawable.class.getFields()){
            @Override
            protected Item map(final Field f,int pos) {
                try {
                    final int id = f.getInt(null);
                    final String name = f.getName();
                    return new Item(){
                        @Override
                        public Object get() {
                            return application.getResources().getDrawable(id);
                        }
                        @Override
                        public CharSequence getName() {
                            return name;
                        }
                        @Override
                        public Class<?> getReturnType() {
                            return Drawable.class;
                        }
                        @Override
                        public String getPath() {
                            return name;
                        }
                    };
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public Item byPathSegment(String path) {
                try {
                    return map(android.R.drawable.class.getField(path),-1);
                } catch (Exception e) {
                    return null;
                }
            }
        };
    }
    public PackageManager getPackageManager(){
        return application.getPackageManager();
    }
    public Author getAuthor(){
        return new Author(this);
    }
    public void getExceptionalCase(){
        throw new RuntimeException("This is an exception from rule 23: Avoid exceptions.");
    }
    private String[]linesOf(InputStream is) throws IOException{
        ArrayList<String> res = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while(br.ready()){
            String line = br.readLine().trim();
            if (line.length()>0)
                res.add(line);
        }
        is.close();
        return res.toArray(new String[res.size()]);
    }
    private final static String[]versions = {"v0.2.0"};
    public Map<String,String[]> getChanges() throws IOException{
        Map<String,String[]> res = new HashMap<String, String[]>();
        for (String v:versions)
            res.put(v, linesOf(application.getAssets().open("v0.2.0")));
        return res;
    }
}
