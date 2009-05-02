package net.virtualvoid.android.browser;

public interface Item {
    String getName();
    Class<?> getReturnType();
    Object get();
}
