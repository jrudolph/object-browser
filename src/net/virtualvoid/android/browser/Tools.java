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
