package net.virtualvoid.android.browser;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import net.virtualvoid.android.browser.ObjectBrowser.HistoryItem;
import android.app.ListActivity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ObjectBrowserView extends ListActivity {
	private LayoutInflater inflater;

	private ObjectBrowser getApp(){
	    return (ObjectBrowser) getApplication();
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setListAdapter(new Adapter());
        setContentView(R.layout.main);

        setObject(getApp().getCurrent());
    }
    private ArrayList<Item> subItems = new ArrayList<Item>();
    private final static String TAG = "APIBrowser";

    private ArrayList<Item> fieldsOf(final Object o){
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

        return res;
    }
    private boolean isProperty(Method m){
        return !isStatic(m) // not static
                && (m.getName().startsWith("get") || m.getName().startsWith("is"))
                && m.getParameterTypes().length == 0;
    }
    private boolean isStatic(Member m){
        return (m.getModifiers()&Modifier.STATIC) != 0;
    }
    private ArrayList<Item> propertiesOf(final Object o){
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

        return res;
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
    private ArrayList<Item> elementsOfArray(final Object array){
        int len = Array.getLength(array);
        ArrayList<Item> res = new ArrayList<Item>(len+1);

        res.add(single("length",len));

        for (int i=0;i<len;i++){
            final int index = i;
            res.add(new Item(){
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
            });
        }
        return res;
    }
    private ArrayList<Item> elementsOfMap(final Map<?,?> map){
        int len = map.size();
        ArrayList<Item> res = new ArrayList<Item>(len+1);

        res.add(single("size",len));

        for (final Entry<?,?> e:map.entrySet()){
            res.add(new Item(){
                @Override
                public Object get() {
                    return e.getValue();
                }
                @Override
                public String getName() {
                    return e.getKey().toString();
                }
                @Override
                public Class<?> getReturnType() {
                    Object val = get();
                    return val!=null ? val.getClass() : Object.class;
                }
            });
        }
        return res;
    }

    private void setObject(HistoryItem item){
        Object current = item.object;

        Log.d(TAG,"setObject called with "+current);

        if (current == null)
            return;

    	((TextView)findViewById(R.id.object)).setText(current.toString());
    	((TextView)findViewById(R.id.clazz)).setText(current.getClass().getName());

    	subItems.clear();

    	if (current.getClass().isArray())
    	    subItems.addAll(elementsOfArray(current));
    	else if (current instanceof Map)
    	    subItems.addAll(elementsOfMap((Map<?, ?>) current));

    	subItems.addAll(fieldsOf(current));
       	subItems.addAll(propertiesOf(current));

       	((Adapter)getListView().getAdapter()).notifyDataSetInvalidated();
       	// HACK: call layoutChildren before setting the selection, since
       	// setSelection will not work otherwise
       	Tools.layoutChildren(getListView());

    	getListView().setSelection(item.listPosition);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	setObject(getApp().move(subItems.get(position),position));
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && getApp().hasPrevious()){
            setObject(getApp().toPrevious());
            return true;
        }
        else
            return super.onKeyDown(keyCode, event);
    }

	class Adapter extends BaseAdapter{
		@Override
		public int getCount() {
			return subItems.size();
		}
		@Override
		public Object getItem(int position) {
			return subItems.get(position);
		}
		@Override
		public long getItemId(int position) {
			return subItems.get(position).hashCode();
		}
		private int visibility(boolean visible){
		    return visible ? View.VISIBLE : View.GONE;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = inflater.inflate(R.layout.item, null);

			Item m = subItems.get(position);

			((TextView) convertView.findViewById(R.id.name)).setText(m.getName());
			((TextView) convertView.findViewById(R.id.result_type)).setText(m.getReturnType().getName());
	         TextView textView = (TextView) convertView.findViewById(R.id.value);
	         ImageView imageView = (ImageView) convertView.findViewById(R.id.valueDrawable);

			Object value = m.get();
            if (value instanceof Drawable)
                imageView.setImageDrawable((Drawable) value);
            else
                textView.setText(String.valueOf(value));

            textView.setVisibility(visibility(!(value instanceof Drawable)));
            imageView.setVisibility(visibility(value instanceof Drawable));

			return convertView;
		}
	}
}