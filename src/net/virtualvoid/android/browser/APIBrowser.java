package net.virtualvoid.android.browser;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class APIBrowser extends ListActivity {
	private Object current;
	private LayoutInflater inflater;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        setListAdapter(new Adapter());
        setContentView(R.layout.main);
        
        setObject(this);
    }
    private ArrayList<Method> methods = new ArrayList<Method>();
    private final static String TAG = "APIBrowser";

    private void setObject(Object o){
    	this.current = o;
    	Log.d(TAG,"setObject called with "+o.toString());
    	
    	((TextView)findViewById(R.id.object)).setText(current.toString());
    	
    	methods.clear();
    	for (Method m:current.getClass().getMethods()){
    		if (m.getParameterTypes().length == 0 
    				&& (m.getModifiers()&Modifier.STATIC)==0
    				&& !m.getReturnType().equals(Void.TYPE))
    			methods.add(m);
    	}
    	((Adapter)getListAdapter()).notifyDataSetInvalidated();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	Method m = methods.get(position);
    	try {
			setObject(m.invoke(current));
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	class Adapter extends BaseAdapter{
		@Override
		public int getCount() {
			return methods.size();
		}
		@Override
		public Object getItem(int position) {
			return methods.get(position);
		}
		@Override
		public long getItemId(int position) {
			return methods.get(position).hashCode();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = inflater.inflate(R.layout.method, null);
			
			Method m = methods.get(position);
			
			((TextView) convertView.findViewById(R.id.name)).setText(m.getName());
			((TextView) convertView.findViewById(R.id.result_type)).setText(m.getReturnType().getSimpleName());
			
			return convertView;
		}		
	}
}