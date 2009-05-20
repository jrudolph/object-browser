package net.virtualvoid.android.browser;

import java.io.BufferedReader;
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
    public ItemList getSystemDrawables(){
        return new ItemFactory.MappedArrayItemList<Field>("System icons",android.R.drawable.class.getFields()){
            @Override
            protected Item map(final Field f) {
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
                    };
                } catch (Exception e) {
                    throw new RuntimeException(e);
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
