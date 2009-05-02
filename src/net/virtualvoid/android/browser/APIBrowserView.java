package net.virtualvoid.android.browser;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class APIBrowserView extends ListActivity {
	private LayoutInflater inflater;

	private APIBrowser getApp(){
	    return (APIBrowser) getApplication();
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
                && m.getName().startsWith("get")
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

    private void setObject(Object current){
        Log.d(TAG,"setObject called with "+current);

        if (current == null)
            return;

    	((TextView)findViewById(R.id.object)).setText(current.toString());
    	((TextView)findViewById(R.id.clazz)).setText(current.getClass().getName());

    	subItems.clear();
    	subItems.addAll(fieldsOf(current));
    	subItems.addAll(propertiesOf(current));

    	((Adapter)getListAdapter()).notifyDataSetInvalidated();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	setObject(getApp().move(subItems.get(position)));
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            setObject(getApp().toPrevious());
            return true;
        }
        else
            return false;
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
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = inflater.inflate(R.layout.item, null);

			Item m = subItems.get(position);

			((TextView) convertView.findViewById(R.id.name)).setText(m.getName());
			((TextView) convertView.findViewById(R.id.result_type)).setText(m.getReturnType().getName());
			((TextView) convertView.findViewById(R.id.value)).setText(String.valueOf(m.get()));

			return convertView;
		}
	}
}