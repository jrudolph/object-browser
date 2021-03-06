/*
    This file is part of the object-browser.

    The object-browser is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    The object-browser is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the object-browser.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2009 Johannes Rudolph
*/

package net.virtualvoid.android.browser;

import java.util.ArrayList;

import net.virtualvoid.android.browser.ObjectBrowser.HistoryItem;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
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
    private boolean creatingShortCut;

    private ObjectBrowser state;

	private ObjectBrowser getApp(){
	    return state;
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        state = new ObjectBrowser(this);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())){
            String path = getIntent().getData().getSchemeSpecificPart();
            try{
                state.switchTo(ItemFactory.fromPath(state.getHome(), path),path, 0);
            } catch(Exception e){
                e.printStackTrace();
                Toast.makeText(this, "Can't access this element, probably the path is wrong: "+path, Toast.LENGTH_LONG)
                    .show();
                finish();
                return;
            }
        }
        else
            state.switchTo(state.mycastle,"Home", 0);

        creatingShortCut = Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction());

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
    	items.addAll(ItemFactory.itemsFor(getApp().getHome(),item));

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
    private final static int SHORTCUT = Integer.MIN_VALUE + 1;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,HOME,0,"Home").setIcon(android.R.drawable.ic_menu_myplaces);
        menu.add(0,HISTORY,1,"History").setIcon(android.R.drawable.ic_menu_recent_history);
        menu.add(0,SAVE,2,"Save").setIcon(android.R.drawable.ic_menu_save);

        if (creatingShortCut)
            menu.add(0,SHORTCUT,3,"Create shortcut").setIcon(android.R.drawable.star_big_on);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        HistoryItem current = getApp().getCurrent();
        switch(item.getItemId()){
        case HOME:
            setObject(getApp().switchTo(getApp().getHome(),getExpandableListView().getExpandableListPosition(getExpandableListView().getFirstVisiblePosition())));
            return true;
        case HISTORY:
            setObject(getApp().switchTo(getApp().history,getExpandableListView().getExpandableListPosition(getExpandableListView().getFirstVisiblePosition())));
            return true;
        case SAVE:
            getApp().addSaved(current.object);
            Toast.makeText(this, "The current object was saved in your favourites. See Home.getFavourites().", Toast.LENGTH_SHORT)
                 .show();
            return true;
        case SHORTCUT:
            ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(ObjectBrowserView.this, R.drawable.magnifier);

            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
                    new Intent(
                            Intent.ACTION_VIEW
                            ,new Uri.Builder().scheme("obrowse").opaquePart(current.path).build())
                        );
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, current.path);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

            setResult(RESULT_OK, intent);
            finish();

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
            ItemList itemList = (ItemList) myAdapter.getGroup(ExpandableListView.getPackedPositionGroup(pos));
            setObject(getApp().switchTo(itemList,itemList.getName().toString(),pos));
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

            Object value;
            try {
                value = item.get();
                convertView.setBackgroundDrawable(null);
            } catch (Exception e){
                value = e;
                convertView.setBackgroundColor(Color.rgb(113,0,0));
            }
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