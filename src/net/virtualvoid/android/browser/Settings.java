package net.virtualvoid.android.browser;

public class Settings {
    static interface Setting<T>{
        T get();
        String getName();
    }
    public static <T> Setting<T> constant(final String name,final T value){
        return new Setting<T>(){
            @Override
            public String getName() {
                return name;
            }
            @Override
            public T get() {
                return value;
            }
        };
    }

    public static Setting<Boolean> quoteStrings = constant("Quote literal strings",true);
}
