package kr.co.arteum.www.arteum;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by ParkYD on 2015-10-12.
 */

public class SettingSQL {
    DBManager dbmanager;
    SQLiteDatabase sqlitedb;
    public SettingSQL(Context context){
        getSetting(context);
    }

    public void getSetting(Context context){ //세팅데이터 가져오기
        try{
            String selectQuery = "SELECT * FROM MySetting order by sys_id";

            dbmanager = new DBManager(context);
            sqlitedb = dbmanager.getReadableDatabase();
            Cursor cursor =sqlitedb.rawQuery(selectQuery, null);
            if(cursor.moveToFirst()){
                do{
                    if(cursor.getString(cursor.getColumnIndex("settingName")).equals("pushAll")) {
                        Util.SETTING_PUSH_ALL = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("status")));
                    }
                    /*if(cursor.getString(cursor.getColumnIndex("settingName")).equals("setting2")) {
                        Util.SETTING_BOOL[1] = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("status")));
                    }
                    if(cursor.getString(cursor.getColumnIndex("settingName")).equals("setting3")) {
                        Util.SETTING_BOOL[2] = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("status")));
                    }
                    if(cursor.getString(cursor.getColumnIndex("settingName")).equals("myStation")) {
                        Util.SETTING_BOOL[3] = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("status")));
                        Util.MYSTATION = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndex("alramStartTime"))),Double.parseDouble(cursor.getString(cursor.getColumnIndex("alramEndTime"))));
                    }*/
                } while (cursor.moveToNext());
            }
            Log.e("MySetting table :", "\n----------------------\n" + dbmanager.PrintDataSetting());
            cursor.close();
            sqlitedb.close();
            dbmanager.close();
        } catch(SQLiteException e) {
            Log.e("SettingSQL_error", e.getMessage());
        }

    }
    public Boolean getPushAllSetting(Context context) {
        Boolean rValue = false;
        try{
            String selectQuery = "SELECT * FROM MySetting where settingName = 'pushAll'";

            dbmanager = new DBManager(context);
            sqlitedb = dbmanager.getReadableDatabase();
            Cursor cursor =sqlitedb.rawQuery(selectQuery, null);
            if(cursor.moveToFirst()){
                do{
                    if(cursor.getString(cursor.getColumnIndex("settingName")).equals("pushAll")) {
                        rValue = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("status")));
                    }
                } while (cursor.moveToNext());
            }
            Log.e("MySetting table :", "\n----------------------\n" + dbmanager.PrintDataSetting());
            cursor.close();
            sqlitedb.close();
            dbmanager.close();
        } catch(SQLiteException e) {
            Log.e("SettingSQL_error", e.getMessage());
        }
        return rValue;
    }

    public void settingPushAllUpdate(Context context, String statusValue){
        try {
            dbmanager = new DBManager(context);
            sqlitedb = dbmanager.getReadableDatabase();

            ContentValues values = new ContentValues();
            values.put("settingName", "pushAll");
            values.put("status", statusValue);

            ContentValues values2 = new ContentValues();
            values2.put("status", "true");
            //long newRowId = sqlitedb.update("MySetting", values, "settingName='"+busHP+"'", null);
            long newRowId2 = sqlitedb.update("MySetting", values, "settingName='pushAll'", null);
            sqlitedb.close();
            dbmanager.close();

            if (newRowId2 !=-1) {
                //Toast.makeText(this, "변경완료", Toast.LENGTH_LONG).show();
                //Log.e("MySetting table :", "\n----------------------\n" + dbmanager.PrintDataSetting());
            } else {
                Toast.makeText(context, "업데이트 실패", Toast.LENGTH_LONG).show();
            }
        }catch (SQLiteException e2) {
            Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



}
