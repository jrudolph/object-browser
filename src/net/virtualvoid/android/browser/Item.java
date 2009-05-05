package net.virtualvoid.android.browser;

public interface Item {
    CharSequence getName();
    Class<?> getReturnType();
    Object get();
}
