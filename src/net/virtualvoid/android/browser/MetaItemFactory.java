package net.virtualvoid.android.browser;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class MetaItemFactory {
    static abstract class MappedListMetaItemList<T> implements MetaItemList{
        private List<T> list;
        private CharSequence name;

        public MappedListMetaItemList(CharSequence name,List<T> list) {
            this.name = name;
            this.list = list;
        }

        protected abstract MetaItem map(T ele);

        @Override
        public MetaItem get(int position) {
            return map(list.get(position));
        }
        @Override
        public CharSequence getName() {
            return name;
        }
        @Override
        public int size() {
            return list.size();
        }
    }
    private static MetaItemList fromList(final String name,final List<MetaItem> items){
        return new MetaItemList(){
            @Override
            public MetaItem get(int position) {
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

    private static boolean isProperty(Method m){
        return !isStatic(m) // not static
                && (m.getName().startsWith("get") || m.getName().startsWith("is"))
                && m.getParameterTypes().length == 0;
    }
    private static boolean isStatic(Member m){
        return (m.getModifiers()&Modifier.STATIC) != 0;
    }
    private static ArrayList<Method> propertyMethodsOf(final Class<?> clazz){
        Method[] ms = clazz.getMethods();
        ArrayList<Method> res = new ArrayList<Method>(ms.length);
        for(Method m:ms)
            if(isProperty(m))
                res.add(m);
        return res;
    }
    private static MetaItemList propertiesOf(final Class<?> clazz){
        return new MappedListMetaItemList<Method>("Properties",propertyMethodsOf(clazz)){
            @Override
            protected MetaItem map(final Method m) {
                return new MetaItem(){
                    {
                        m.setAccessible(true);
                    }
                    @Override
                    public Object get(Object parent){
                        try {
                            return m.invoke(parent);
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
                };
            }
        };
    }
    private static MetaItemList fieldsOf(Class<?> cur){
        ArrayList<MetaItem> res = new ArrayList<MetaItem>();

        while(cur != null){
            for (final Field f:cur.getDeclaredFields())
                if (!isStatic(f))
                    res.add(new MetaItem(){
                        {
                            f.setAccessible(true);
                        }
                        @Override
                        public Object get(Object o){
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
    public static ArrayList<MetaItemList> metaItemsFor(Class<?> clazz){
        ArrayList<MetaItemList> res = new ArrayList<MetaItemList>(){
            private static final long serialVersionUID = 1L;

            @Override
            public boolean add(MetaItemList list) {
                if (list.size() > 0)
                    return super.add(list);
                else
                    return false;
            }
        };

        res.add(fieldsOf(clazz));
        res.add(propertiesOf(clazz));

        return res;
    }
}