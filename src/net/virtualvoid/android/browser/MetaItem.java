package net.virtualvoid.android.browser;

public interface MetaItem {
    CharSequence getName();
    Class<?> getReturnType();
    Object get(Object o);
}
