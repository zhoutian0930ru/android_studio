package com.example.zt.assignment2;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

public class Contact_profile extends AppCompatActivity {

    private Button add,delete;
    //private CheckBox checkbox;
    private ListView listView;
    private TextView p_name,p_phone;
    private ImageView image;
    private Bitmap mBitmap;
    private List<Map<String,Object>> dataList;
    private MyDatabaseHelper db;
    private SimpleCursorAdapter scAdapter;
    private databaseManager dbManager;
    private CheckBoxCursorAdapter cbAdapter;
    private Cursor cursor,cursor_temp,cursor_image;
    private SQLiteDatabase dbWrite,dbRead;
    public contact_list_fragment cl_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_contact_profile);

        //landscape  fragment
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)//when under landscape
        {
            cl_fragment = new contact_list_fragment();

            if (!cl_fragment.isAdded()) {
                getFragmentManager().beginTransaction().add(R.id.land_list,cl_fragment).commitAllowingStateLoss();//show mainactivity fragment
            }
        }

        p_name=(TextView)findViewById(R.id.output_name);
        p_phone=(TextView)findViewById(R.id.output_phone);
        listView=(ListView)findViewById(R.id.relationship);
        image=(ImageView)findViewById(R.id.image1);
        mBitmap=null;

        Intent i = getIntent();

        String name_profile=i.getStringExtra("name_from_main");
        p_name.setText(name_profile);

        db = new MyDatabaseHelper(Contact_profile.this,"contact.db",null,1);


        //listview adapter
        dbRead = db.getReadableDatabase();

        //get phone
        String sql_phone = "SELECT phone FROM contacts_list where name='"+name_profile+"'";
        cursor_temp = dbRead.rawQuery(sql_phone,null);
        cursor_temp.moveToPosition(0);
        String phone_profile =cursor_temp.getString(cursor_temp.getColumnIndex("phone"));
        p_phone.setText(phone_profile);


        //get image
        String sql_picture = "SELECT picture FROM picture where name='"+name_profile+"'";
        cursor_image = dbRead.rawQuery(sql_picture,null);
        if(cursor_image.getCount()!=0){
            cursor_image.moveToPosition(0);
            byte[] b =cursor_image.getBlob(cursor_image.getColumnIndex("picture"));
            mBitmap= BitmapFactory.decodeByteArray(b,0,b.length,null);
        }
        else {
            mBitmap = BitmapFactory.decodeResource(Contact_profile.this.getResources(), R.drawable.test);
        }
        image.setImageBitmap(mBitmap);

        //get relationship
        String sql = "select _id,name2 from relationship where name1='"+name_profile+"'";
        cursor = dbRead.rawQuery(sql,null);
        scAdapter = new SimpleCursorAdapter(this,R.layout.list_view_without_checkbox,cursor,
                new String[] {"name2"},new int[]{R.id.item_name},0);
        listView.setAdapter(scAdapter);

        dbRead.close();

        //list item click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                cursor.moveToPosition(position);
                String name_from_relationship=cursor.getString(cursor.getColumnIndex("name2"));

                //listview adapter
                dbRead = db.getReadableDatabase();

                p_name.setText(name_from_relationship);

                String sql_phone = "SELECT phone FROM contacts_list where name='"+name_from_relationship+"'";
                cursor_temp = dbRead.rawQuery(sql_phone,null);
                cursor_temp.moveToPosition(0);
                String phone_profile =cursor_temp.getString(cursor_temp.getColumnIndex("phone"));
                p_phone.setText(phone_profile);


                //get image
                String sql_picture = "SELECT picture FROM picture where name='"+name_from_relationship+"'";
                cursor_image = dbRead.rawQuery(sql_picture,null);
                if(cursor_image.getCount()!=0){
                    cursor_image.moveToPosition(0);
                    byte[] b =cursor_image.getBlob(cursor_image.getColumnIndex("picture"));
                    mBitmap= BitmapFactory.decodeByteArray(b,0,b.length,null);
                }
                else {
                    mBitmap = BitmapFactory.decodeResource(Contact_profile.this.getResources(), R.drawable.test);
                }
                image.setImageBitmap(mBitmap);


                String sql = "select _id,name2 from relationship where name1='"+name_from_relationship+"'";
                cursor = dbRead.rawQuery(sql,null);
                scAdapter = new SimpleCursorAdapter(Contact_profile.this,R.layout.list_view_without_checkbox,cursor,
                        new String[] {"name2"},new int[]{R.id.item_name},0);
                listView.setAdapter(scAdapter);

                dbRead.close();

            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i=new Intent(Contact_profile.this,image_show.class);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.PNG,100,os);
                i.putExtra("image",os.toByteArray());
                startActivity(i);
            }
        });

    }
}
