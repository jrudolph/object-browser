package net.virtualvoid.android.browser;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;

public class DBTools {
    public static Map<String,String> rowAsMap(Cursor cursor){
        HashMap<String, String> res = new HashMap<String, String>();
        String[] colNames = cursor.getColumnNames();
        for (int i=0;i<cursor.getColumnCount();i++)
            res.put(colNames[i], cursor.getString(i));
        return res;
    }
}
