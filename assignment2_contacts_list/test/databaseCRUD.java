package com.example.zt.assignment2;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class databaseCRUD {

    private MyDatabaseHelper dbHelper;

    public databaseCRUD(Context context){
        //dbHelper= new MyDatabaseHelper(context);
    }

    public void insert_person(String name, String phone){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("name",name);
        values.put("phone",phone);
        //
        db.insert("contacts_list",null,values);
        db.close();
    }

    public void insert_relationship(String name1, String name2){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values1=new ContentValues();
        ContentValues values2=new ContentValues();
        values1.put("name1",name1);
        values1.put("name2",name2);

        values2.put("name1",name2);
        values2.put("name2",name1);
        //
        db.insert("relationship",null,values1);
        db.insert("relationship",null,values2);
        db.close();
    }

}
