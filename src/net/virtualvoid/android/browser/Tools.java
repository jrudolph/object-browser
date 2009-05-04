package net.virtualvoid.android.browser;

import java.lang.reflect.Method;

import android.widget.ListView;

public class Tools {
    private static Method getMethod(Class<?> cl,String name){
        try {
            Method m = cl.getDeclaredMethod(name);
            m.setAccessible(true);
            return m;
        } catch (Exception e) {
            return null;
        }
    }
    private static final Method layoutChildren = getMethod(ListView.class,"layoutChildren");

    public static void layoutChildren(ListView view){
        if (layoutChildren != null)
            try {
                layoutChildren.invoke(view);
            } catch (Exception e) {
                throw new Error(e);
            }
    }
}
