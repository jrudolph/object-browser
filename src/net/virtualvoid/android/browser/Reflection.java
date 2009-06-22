package net.virtualvoid.android.browser;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class Reflection {
    public static boolean isSupertypeOf(Class<?> c1,Class<?> c2,Map<Class<?>,Map<Class<?>,Boolean>> cache){
        if (c1 == c2)
            return false;

        Map<Class<?>,Boolean> superMap = cache.get(c1);
        if (superMap == null){
            superMap = new HashMap<Class<?>, Boolean>();
            cache.put(c1, superMap);
        }

        Boolean res = superMap.get(c2);
        if (res == null){
            Class<?> curSuper = c2.getSuperclass();
            while(curSuper != null){
                if (curSuper == c1){
                    superMap.put(c2, true);
                    return true;
                }
                curSuper = curSuper.getSuperclass();
            }
            superMap.put(c2, false);
            res = false;
        }

        return res;
    }
    /**
     * Copy elements to an array which has the most specific type possible
     * @param oa
     * @return
     */
    public static Object[] narrow(Object []oa){
        if (oa.length == 0)
            return oa;

        Map<Class<?>,Map<Class<?>,Boolean>> cache = new HashMap<Class<?>, Map<Class<?>,Boolean>>();

        Class<?> cl = null;
        for(Object o:oa){
            if (o == null)
                continue;

            Class<?> cand = o.getClass();

            if (cl == null)
                cl = cand;
            else if (cl == cand)
                continue;
            else if (isSupertypeOf(cand,cl,cache))
                cl = cand;
            else if (!isSupertypeOf(cl,cand,cache)) // no common hierarchy
                cl = Object.class;
        }

        if (cl != null && isSupertypeOf(oa.getClass().getComponentType(),cl,cache)){
            Object[] res = (Object[]) Array.newInstance(cl, oa.length);
            System.arraycopy(oa, 0, res, 0, oa.length);
            return res;
        }
        else
            return oa;
    }
    public static Class<?> boxed(Class<?> cl){
        if (!cl.isPrimitive())
            return cl;
        else
            if (cl == Integer.TYPE)
                return Integer.class;
            else if (cl == Float.TYPE)
                return Float.class;
            else if (cl == Boolean.TYPE)
                return Boolean.class;
            else if (cl == Short.TYPE)
                return Short.class;
            else if (cl == Double.TYPE)
                return Double.class;
            else if (cl == Character.TYPE)
                return Character.class;
            else if (cl == Byte.TYPE)
                return Byte.class;
            else
                throw new RuntimeException("Missing boxed type for primitive type "+cl);
    }
}
