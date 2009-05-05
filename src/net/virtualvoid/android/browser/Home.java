package net.virtualvoid.android.browser;

import java.lang.reflect.Field;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

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

    private Spanned colorize(CharSequence seq){
        Spannable sp = Spannable.Factory.getInstance().newSpannable(seq);
        int len = seq.length();
        for (int i=0;i<len;i++)
            sp.setSpan(new ForegroundColorSpan(Color.HSVToColor(new float[]{i*360/len,.8f,.8f})), i, i+1,0);
        return sp;
    }
    public Spanned getAuthor(){
        return colorize("Johannes Rudolph");
    }
    public String getSourceURL(){
        return "<a href=\"http://github.com/jrudolph/object-browser\">http://github.com/jrudolph/object-browser</a>";
    }
}
