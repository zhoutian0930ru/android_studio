package com.rutgers.tz164.assignment3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class MyDatabaseHelper extends SQLiteOpenHelper {
    private Context mContext;

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) //first time create table
    {
        db.execSQL("create table location(_id integer primary key autoincrement, address varchar, time integer, longitude varchar, latitude varchar, name varchar, marker varchar, associated varchar, named varchar)");//create database when first create
       // db.execSQL("create table relation(_id integer primary key autoincrement, address varchar, parentaddress varchar, parent name)");//create database when first create

     //   db.execSQL("create table fence(_id integer primary key autoincrement, fence varchar, enterflag int, exitflag int)");//create database when first create

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //when upgrade
    }

}