package com.example.zt.assignment2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {


    public static final String CREATE_LIST = "create table contacts_list(" +
            "_id integer primary key autoincrement," +
            "name text," +
            "phone text)";

    public static final String CREATE_RELATIONSHIP = "create table relationship(" +
            "_id integer primary key autoincrement," +
            "name1 text," +
            "name2 text)";
    public static final String CREATE_PICTURE = "create table picture(" +
            "_id integer primary key autoincrement," +
            "name text," +
            "picture blob not null)";


    private Context mContext;
    private static MyDatabaseHelper mInstance = null;

    public  MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory , int version){
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
        db.execSQL(CREATE_RELATIONSHIP);
        db.execSQL(CREATE_PICTURE);

        Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {



    }
}
