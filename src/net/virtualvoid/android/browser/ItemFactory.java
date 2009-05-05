package net.virtualvoid.android.browser;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class ItemFactory {
    static abstract class MappedItemList<T> implements ItemList{
        private String name;

        public MappedItemList(String name) {
            this.name = name;
        }

        protected abstract T getOriginal(int position);
        protected abstract Item map(T object);
        @Override
        public Item get(int position) {
            return map(getOriginal(position));
        }
        @Override
        public String getName() {
            return name;
        }

    }
    static abstract class MappedListItemList<T> extends MappedItemList<T>{
        private List<T> items;

        public MappedListItemList(String name, List<T> items) {
            super(name);
            this.items = items;
        }

        @Override
        public int size() {
            return items.size();
        }
        @Override
        protected T getOriginal(int index) {
            return items.get(index);
        }
    }
    static abstract class MappedArrayItemList<T> extends MappedItemList<T>{
        private T[] items;

        public MappedArrayItemList(String name, T[] items) {
            super(name);
            this.items = items;
        }
        @Override
        public int size() {
            return items.length;
        }
        @Override
        protected T getOriginal(int arg0) {
            return items[arg0];
        }
    }

    private static ItemList fromList(final String name,final List<Item> items){
        return new ItemList(){
            @Override
            public Item get(int position) {
                return items.get(position);
            }
            @Override
            public String getName() {
                return name;
            }
            @Override
            public int size() {
                return items.size();
            }
        };
    }
    private static ItemList join(final String name,final ItemList list1,final ItemList list2){
        return new ItemList(){
            @Override
            public Item get(int position) {
                int len1 = list1.size();
                if (position < len1)
                    return list1.get(position);
                else
                    return list2.get(position-len1);
            }
            @Override
            public String getName() {
                return name;
            }
            @Override
            public int size() {
                return list1.size()+list2.size();
            }
        };
    }

    private static ItemList fieldsOf(final Object o){
        ArrayList<Item> res = new ArrayList<Item>();

        Class<?> cur = o.getClass();

        while(cur != null){
            for (final Field f:cur.getDeclaredFields())
                if (!isStatic(f))
                    res.add(new Item(){
                        {
                            f.setAccessible(true);
                        }
                        @Override
                        public Object get(){
                            try {
                                return f.get(o);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        @Override
                        public String getName() {
                            return f.getName();
                        }
                        @Override
                        public Class<?> getReturnType() {
                            return f.getType();
                        }
                    });
            cur = cur.getSuperclass();
        }

        return fromList("Fields",res);
    }
    private static boolean isProperty(Method m){
        return !isStatic(m) // not static
                && (m.getName().startsWith("get") || m.getName().startsWith("is"))
                && m.getParameterTypes().length == 0;
    }
    private static boolean isStatic(Member m){
        return (m.getModifiers()&Modifier.STATIC) != 0;
    }
    private static ItemList propertiesOf(final Object o){
        ArrayList<Item> res = new ArrayList<Item>();

        for (final Method m:o.getClass().getMethods())
            if (isProperty(m))
                res.add(new Item(){
                    {
                        m.setAccessible(true);
                    }
                    @Override
                    public Object get(){
                        try {
                            return m.invoke(o);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    @Override
                    public String getName() {
                        return m.getName();
                    }
                    @Override
                    public Class<?> getReturnType() {
                        return m.getReturnType();
                    }
                });

        return fromList("Properties",res);
    }
    private final static ItemList emptyList = new ItemList(){
        @Override
        public String getName() {
            return "<empty>";
        }
        @Override
        public int size() {
            return 0;
        }
        @Override
        public Item get(int position) {
            throw new NoSuchElementException("no such position "+position);
        }
    };
    private static ItemList singleton(final String name,final Object o){
        final Item item = single(name,o);
        return o == null ? emptyList : new ItemList(){
            @Override
            public Item get(int position) {
                if (position == 0)
                    return item;
                else
                    throw new NoSuchElementException("At position "+position);
            }
            @Override
            public String getName() {
                return name;
            }
            @Override
            public int size() {
                return 1;
            }
        };
    }
    private static Item single(final String name,final Object o){
        return new Item(){
            @Override
            public Object get() {
                return o;
            }
            @Override
            public String getName() {
                return name;
            }
            @Override
            public Class<?> getReturnType() {
                return o.getClass();
            }
        };
    }
    private static ItemList elementsOfArray(final Object array){
        final int len = Array.getLength(array);
        final Item[] items = new Item[len];
        return new ItemList(){
            @Override
            public Item get(final int index) {
                Item item = items[index];
                if (item == null){
                    item = new Item(){
                        @Override
                        public Object get() {
                            return Array.get(array, index);
                        }
                        @Override
                        public String getName() {
                            return Integer.toString(index);
                        }
                        @Override
                        public Class<?> getReturnType() {
                            Object val = get();
                            return val!=null ? val.getClass() : array.getClass().getComponentType();
                        }
                    };
                    items[index] = item;
                }
                return item;
            }
            @Override
            public String getName() {
                return "Elements";
            }
            @Override
            public int size() {
                return len;
            }
        };
    }
    private static ItemList elementsOfMap(final Map<?,?> map){
        ArrayList<Object> keys = new ArrayList<Object>();
        for (Object o:map.keySet())
            keys.add(o);
        return new MappedListItemList<Object>("Values",keys){
            @Override
            protected Item map(final Object key) {
                return new Item(){
                    @Override
                    public Object get() {
                        return map.get(key);
                    }
                    @Override
                    public String getName() {
                        return key.toString();
                    }
                    @Override
                    public Class<?> getReturnType() {
                        Object val = get();
                        return val!=null ? val.getClass() : Object.class;
                    }
                };
            }
        };
    }
    private static final int MAX_ELEMENTS = 100;
    private static ItemList elementsOfIterable(final Iterable<?> i){
        final ArrayList<Object> res = new ArrayList<Object>(MAX_ELEMENTS/2);

        int num = 0;
        for (Object o:i)
            if (num < MAX_ELEMENTS){
                res.add(o);
                num++;
            }
            else
                break;
        final int numElements = num;
        return join("Elements"
                   ,singleton("Size",num >= MAX_ELEMENTS?">= "+MAX_ELEMENTS:num)
                   ,new ItemList(){
                        @Override
                        public Item get(final int position) {
                            return new Item(){
                                @Override
                                public Object get() {
                                    return res.get(position);
                                }
                                @Override
                                public String getName() {
                                    return Integer.toString(position);
                                }
                                @Override
                                public Class<?> getReturnType() {
                                    Object val = get();
                                    return val!=null ? val.getClass() : Object.class;
                                }
                            };
                        }
                        @Override
                        public String getName() {
                            return "Elements";
                        }
                        @Override
                        public int size() {
                            return numElements;
                        }
                   });
    }
    private static ItemList contentsOfDirectory(final File dir){
        return join(
                "Contents",
                singleton("..",dir.getParentFile())
                ,new MappedArrayItemList<File>("Contents",dir.listFiles()){
                    @Override
                    protected Item map(final File f) {
                        return new Item(){
                            @Override
                            public Object get() {
                                return f;
                            }
                            @Override
                            public String getName() {
                                return f.getName();
                            }
                            @Override
                            public Class<?> getReturnType() {
                                return File.class;
                            }
                        };
                    }
                });
    }
    private static MappedListItemList<PackageInfo> packagesFromPM(final PackageManager pm) {
        return new MappedListItemList<PackageInfo>("Installed Packages",pm.getInstalledPackages(0xffffffff)){
            @Override
            protected Item map(final PackageInfo info) {
                return new Item(){
                    @Override
                    public Object get() {
                        return info;
                    }
                    @Override
                    public CharSequence getName() {
                        return pm.getApplicationLabel(info.applicationInfo);
                    }
                    @Override
                    public Class<?> getReturnType() {
                        return ApplicationInfo.class;
                    }
                };
            }
        };
    }

    private static void add(ArrayList<ItemList> list,ItemList il){
        if (il.size() > 0)
            list.add(il);
    }
    public static ArrayList<ItemList> itemsFor(Object o){
        ArrayList<ItemList> res = new ArrayList<ItemList>();

        if (o.getClass().isArray())
            add(res,elementsOfArray(o));
        else if (o instanceof Map)
            add(res,elementsOfMap((Map<?, ?>) o));
        else if (o instanceof File && ((File) o).isDirectory())
            add(res,contentsOfDirectory((File) o));
        else if (o instanceof Iterable)
            add(res,elementsOfIterable((Iterable<?>) o));
        else if (o instanceof PackageManager)
            add(res,packagesFromPM((PackageManager) o));
        else if (o instanceof ItemList)
            add(res,(ItemList) o);

        add(res,fieldsOf(o));
        add(res,propertiesOf(o));

        return res;
    }

    private static String capitalized(String str){
        return str.substring(0,1).toUpperCase()+str.substring(1);
    }
    private static Method getMethod(Class<?> clazz,String name){
        try {
            return clazz.getMethod(name);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
    private static Object getFieldOf(String field,Object o){
        try {
            Class<? extends Object> clazz = o.getClass();
            Method m = getMethod(clazz,"get"+capitalized(field));
            if (m == null)
                m = getMethod(clazz,field);

            if (m != null)
                return m.invoke(o);
            else{
                Field f = clazz.getDeclaredField(field);
                f.setAccessible(true);
                return f.get(o);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static String eval(String path,Object o){
        String[] parts = path.split("\\.");

        for(String part:parts)
            o = getFieldOf(part,o);
        return toString(o);
    }
    private static Pattern splish = Pattern.compile("#(\\w+(?:\\.\\w+)*)");
    private static String format(String format,Object o){
        Matcher m = splish.matcher(format);
        StringBuffer res = new StringBuffer();
        while(m.find())
            m.appendReplacement(res, eval(m.group(1),o));

        m.appendTail(res);
        return res.toString();
    }
    private static final int MAX_ARRAY_ELEMENTS = 5;
    private static String arrayToString(Object o){
        StringBuffer buffer = new StringBuffer();
        buffer.append('[');

        int len = Array.getLength(o);
        for (int i=0;i<Math.min(len,MAX_ARRAY_ELEMENTS);i++){
            if (i != 0)
                buffer.append(", ");
            buffer.append(toString(Array.get(o,i)));
        }
        int remaining = len - MAX_ARRAY_ELEMENTS;
        if (remaining > 0)
            buffer.append(", ... (<i>").append(remaining).append(" more</i>)");

        buffer.append(']');
        return buffer.toString();
    }
    public static String toString(Object o){
        if (o == null)
            return "null";
        else {
            Class<? extends Object> clazz = o.getClass();
            Textual text = clazz.getAnnotation(Textual.class);
            if (text != null)
                return format(text.value(),o);
            else if (clazz.isArray())
                return arrayToString(o);
            else
                return String.valueOf(o);
        }
    }
}
