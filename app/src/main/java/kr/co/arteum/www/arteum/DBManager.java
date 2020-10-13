package kr.co.arteum.www.arteum;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2014-12-24.
 */
public class DBManager extends SQLiteOpenHelper {

    public static final String DB_NAME = "MySetting";
    private static final int DB_VER = 1;
    public DBManager(Context context){
        super(context, DB_NAME, null, DB_VER);
    }

    private SQLiteDatabase db; // DB controller

    @Override
    public void onCreate(SQLiteDatabase db){
        this.db = db;
        db.execSQL("create table MySetting(" +
                "sys_id integer primary key autoincrement, settingName text, status text);");
        // insert default values
        this.addSetting(db, "pushAll", "true");
        this.addSetting(db, "setting2", "false");
        this.addSetting(db, "setting3", "false");

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        String sql_droptable0 = "DROP TABLE IF EXISTS " + "MySetting;";
        db.execSQL(sql_droptable0);
    }

    public void addSetting(SQLiteDatabase db, String key, String bool) {
        ContentValues values = new ContentValues();
        values.put("settingName", key);
        values.put("status", bool);
        db.insert("MySetting", null, values);
    }


    public String PrintDataSetting() {
        SQLiteDatabase db = getReadableDatabase();
        String str = "";

        Cursor cursor = db.rawQuery("select * from mySetting", null);
        while(cursor.moveToNext()) {
            str += cursor.getInt(0)
                    + " : settingName "
                    + cursor.getString(1)
                    + ", status = "
                    + cursor.getString(2)
                    + "\n";
        }
        return str;
    }

}
