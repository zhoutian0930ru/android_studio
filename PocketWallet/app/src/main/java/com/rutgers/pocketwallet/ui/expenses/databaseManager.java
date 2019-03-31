package com.rutgers.pocketwallet.ui.expenses;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class databaseManager {

    private MyDatabaseHelper dbHelper = null;
    private static SQLiteDatabase mDb = null;
    private Context mContext = null;

    public databaseManager(Context context){
        mContext = context;
        dbHelper = MyDatabaseHelper.getInstance(mContext);
        mDb = dbHelper.getReadableDatabase();
    }

    public boolean executeSql(String sql){
        try {
            mDb.execSQL(sql);
            return true;
        }
        catch(SQLiteException e){
            return false;
        }
    }


    public Cursor executeSql(String sql, String[]args){
        try {
            return mDb.rawQuery(sql, args);
        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        return null;
    }


    public void closeDBhelper() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
