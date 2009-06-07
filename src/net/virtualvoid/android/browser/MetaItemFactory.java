package net.virtualvoid.android.browser;

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

        res.add(propertiesOf(clazz));

        return res;
    }
}