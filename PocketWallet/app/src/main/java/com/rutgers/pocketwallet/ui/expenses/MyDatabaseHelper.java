package com.rutgers.pocketwallet.ui.expenses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {


    public static final String CREATE_LIST = "create table position(" +
            "_id integer primary key autoincrement," +
            "latitude text," +
            "altitude text)";

    private Context mContext;
    private static MyDatabaseHelper mInstance = null;

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory , int version){
        super(context,name ,factory,version);
        mContext = context;
    }

    public static synchronized MyDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyDatabaseHelper(context,"contact.db",null,1);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LIST);

        Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {



    }
}
