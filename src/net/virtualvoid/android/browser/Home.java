package net.virtualvoid.android.browser;

import java.lang.reflect.Field;

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

}
