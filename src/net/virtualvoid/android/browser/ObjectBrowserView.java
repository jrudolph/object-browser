package net.virtualvoid.android.browser;

import java.util.ArrayList;

import net.virtualvoid.android.browser.ObjectBrowser.HistoryItem;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ImageView.ScaleType;

public class ObjectBrowserView extends ExpandableListActivity {
	private LayoutInflater inflater;
    private Adapter myAdapter;

	private ObjectBrowser getApp(){
	    return (ObjectBrowser) getApplication();
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        myAdapter = new Adapter();
        setListAdapter(myAdapter);
        setContentView(R.layout.main);
        registerForContextMenu(getExpandableListView());

        setObject(getApp().getCurrent());
    }
    private ArrayList<ItemList> items = new ArrayList<ItemList>();
    private final static String TAG = "APIBrowser";

    private void setObject(HistoryItem item){
        Object current = item.object;

        Log.d(TAG,"setObject called with "+current);

        if (current == null)
            return;

        setTitle("Object Browser: "+(current instanceof Formattable ? ((Formattable) current).formatted() : current.toString()));

        ExpandableListView list = getExpandableListView();

    	items.clear();
    	items.addAll(ItemFactory.itemsFor(current));

    	myAdapter.notifyDataSetInvalidated();

        // HACK: call layoutChildren before setting the selection, since
        // setSelection will not work otherwise
        Tools.layoutChildren(list);

    	for (int i=0;i<items.size();i++)
    	    list.expandGroup(i);

    	if (item.listPosition == 0)
    	    list.setSelection(0);
    	else
            list.setSelectionFromTop(list.getFlatListPosition(item.listPosition),5);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
            int groupPosition, int childPosition, long id) {
        long pos = ExpandableListView.getPackedPositionForChild(groupPosition, childPosition);
        Log.d(TAG,"saving last pos "+pos);
        setObject(getApp().move(items.get(groupPosition).get(childPosition),pos));
        return true;
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
    // You say, this generates a lookupswitch instead of a tableswitch?
    // You're right! I say: Removing the fun from programming in the same way
    // as redundancy, is premature optimization.
    private final static int HOME = 42;
    private final static int HISTORY = -753;
    private final static int SAVE = 123456;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,HOME,0,"Home").setIcon(android.R.drawable.ic_menu_myplaces);
        menu.add(0,HISTORY,1,"History").setIcon(android.R.drawable.ic_menu_recent_history);
        menu.add(0,SAVE,2,"Save").setIcon(android.R.drawable.ic_menu_save);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
        case HOME:
            setObject(getApp().switchTo(getApp().getHome(),getExpandableListView().getExpandableListPosition(getExpandableListView().getFirstVisiblePosition())));
            return true;
        case HISTORY:
            setObject(getApp().switchTo(getApp().history,getExpandableListView().getExpandableListPosition(getExpandableListView().getFirstVisiblePosition())));
            return true;
        case SAVE:
            getApp().addSaved(getApp().getCurrent().object);
            Toast.makeText(this, "The current object was saved in your favourites. See Home.getFavourites().", Toast.LENGTH_SHORT)
                 .show();
            return true;
        }
        return false;
    }
    private final static int ITEM_LIST = 1000;
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP){
            menu.add(0,ITEM_LIST,0,"Go into ItemList");
        }
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        long pos = ((ExpandableListContextMenuInfo)item.getMenuInfo()).packedPosition;
        switch(item.getItemId()){
        case ITEM_LIST:
            setObject(getApp().switchTo(myAdapter.getGroup(ExpandableListView.getPackedPositionGroup(pos)),pos));
            return true;
        }
        return false;
    }
	class Adapter extends BaseExpandableListAdapter{
	    private int visibility(boolean visible){
            return visible ? View.VISIBLE : View.GONE;
        }
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return items.get(groupPosition).get(childPosition);
        }
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return getChild(groupPosition, childPosition).hashCode();
        }
        @Override
        public View getChildView(int groupPosition, int childPosition,
                boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = inflater.inflate(R.layout.item, null);

            Item item = items.get(groupPosition).get(childPosition);

            ((TextView) convertView.findViewById(R.id.name)).setText(item.getName());
            ((TextView) convertView.findViewById(R.id.result_type)).setText(item.getReturnType().getCanonicalName());
            TextView textView = (TextView) convertView.findViewById(R.id.value);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.valueDrawable);

            Object value = item.get();
            if (value instanceof Drawable){
                imageView.setImageDrawable((Drawable) value);
                imageView.setScaleType(value instanceof NinePatchDrawable ? ScaleType.FIT_XY : ScaleType.CENTER_INSIDE);
                if (value instanceof AnimationDrawable){
                    AnimationDrawable anim = (AnimationDrawable) value;
                    anim.setCallback(imageView);
                    anim.start();
                    anim.setOneShot(false);
                }
            }
            else if (value instanceof Spanned)
                textView.setText((Spanned)value);
            else if (value instanceof Formattable)
                textView.setText(((Formattable) value).formatted());
            else
                textView.setText(Html.fromHtml(ItemFactory.toString(value)));

            textView.setVisibility(visibility(!(value instanceof Drawable)));
            imageView.setVisibility(visibility(value instanceof Drawable));

            textView.setTextSize(textView.getText().length() > 20 ? 12 : 18);

            return convertView;
        }
        @Override
        public int getChildrenCount(int groupPosition) {
            return items.get(groupPosition).size();
        }
        @Override
        public Object getGroup(int groupPosition) {
            return items.get(groupPosition);
        }
        @Override
        public int getGroupCount() {
            return items.size();
        }
        @Override
        public long getGroupId(int groupPosition) {
            return getGroup(groupPosition).hashCode();
        }
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = inflater.inflate(R.layout.group, null);

            ItemList is = items.get(groupPosition);

            ((TextView) convertView.findViewById(R.id.name)).setText(is.getName());

            return convertView;
        }
        @Override
        public boolean hasStableIds() {
            return false;
        }
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
	}
}