package net.virtualvoid.android.browser;

public interface ItemList {
    Item get(int position);
    int size();
    CharSequence getName();
}
