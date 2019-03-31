package com.example.zt.assignment2;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

public class Contact_details extends AppCompatActivity {
    private Button addperson;
    private EditText input_name;
    private EditText input_phone;
    private ListView listView;
    private Bitmap mBitmap;
    private ImageView image;
    private MyDatabaseHelper db;
    private CheckBoxCursorAdapter cbAdapter;
    private Cursor cursor;
    private SQLiteDatabase dbWrite,dbRead;
    private String name,phone;
    public contact_list_fragment cl_fragment;
    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    protected static Uri tempUri;
    protected static final int CROP_SMALL_PICTURE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_contact_details);

        //landscape  fragment
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)//when under landscape
        {
            cl_fragment = new contact_list_fragment();

            if (!cl_fragment.isAdded()) {
                getFragmentManager().beginTransaction().add(R.id.land_list,cl_fragment).commitAllowingStateLoss();//show mainactivity fragment
            }
        }


        listView=(ListView)findViewById(R.id.add_re);
        addperson=(Button)findViewById(R.id.addperson);
        image=(ImageView)findViewById(R.id.image1);
        db = new MyDatabaseHelper(Contact_details.this,"contact.db",null,1);
        mBitmap=null;


        //checkbox adapter
        dbRead = db.getReadableDatabase();
        String sql = "SELECT _id,name,phone FROM contacts_list";
        cursor = dbRead.rawQuery(sql,null);
        cbAdapter = new CheckBoxCursorAdapter(this,R.layout.list_view,cursor,
                new String[] {"name"},new int[]{R.id.item_name}, R.id.checkbox,"name",0);
        listView.setAdapter(cbAdapter);
        dbRead.close();
        image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showChoosePicDialog();
            }
        });

        //addperson button
        addperson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_name = (EditText) findViewById(R.id.input_name);
                input_phone = (EditText) findViewById(R.id.input_phone);
                name = input_name.getText().toString();
                phone = input_phone.getText().toString();

                dbWrite = db.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("name",name);
                cv.put("phone",phone);
                dbWrite.insert("contacts_list",null,cv);

                List<String> bn = cbAdapter.getSelectedItems();
                ContentValues cv_1 = new ContentValues();
                ContentValues cv_2 = new ContentValues();

                for(String id : bn){
                    cv_1.put("name1",name);
                    cv_1.put("name2",id);
                    cv_2.put("name1",id);
                    cv_2.put("name2",name);
                    dbWrite.insert("relationship",null,cv_1);
                    dbWrite.insert("relationship",null,cv_2);
                }

                dbWrite.close();

                dbRead = db.getReadableDatabase();
                String sql = "SELECT _id,name,phone FROM contacts_list";
                cursor = dbRead.rawQuery(sql,null);
                cbAdapter = new CheckBoxCursorAdapter(Contact_details.this,R.layout.list_view,cursor,
                        new String[] {"name"},new int[]{R.id.item_name}, R.id.checkbox,"name",0);
                listView.setAdapter(cbAdapter);
                dbRead.close();

                if(mBitmap!=null){
                    insert_picture(Contact_details.this,mBitmap);
                }

                Toast toast = Toast.makeText(Contact_details.this, "add success", Toast.LENGTH_SHORT);
                toast.show();

            }
        });


    }

    protected void showChoosePicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Contact_details.this);
        builder.setTitle("Add :");
        String[] items = { "choose a photo", "take a photo" };
        builder.setNegativeButton("cancel", null);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CHOOSE_PICTURE:
                        Intent openAlbumIntent = new Intent(
                                Intent.ACTION_GET_CONTENT);
                        openAlbumIntent.setType("image/*");
                        //用startActivityForResult方法，待会儿重写onActivityResult()方法，拿到图片做裁剪操作
                        startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
                        break;
                    case TAKE_PICTURE:
                        Intent openCameraIntent = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);
                        tempUri = Uri.fromFile(new File(Environment
                                .getExternalStorageDirectory(), "temp_image.jpg"));

                        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                        startActivityForResult(openCameraIntent, TAKE_PICTURE);
                        break;
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MainActivity.RESULT_OK) {
            switch (requestCode) {
                case TAKE_PICTURE:
                    cutImage(tempUri);
                    break;
                case CHOOSE_PICTURE:
                    cutImage(data.getData());
                    break;
                case CROP_SMALL_PICTURE:
                    if (data != null) {
                        setImageToView(data);
                    }
                    break;
            }
        }
    }

    protected void cutImage(Uri uri) {
        if (uri == null) {
            Log.i("alanjet", "The uri is not exist.");
        }
        tempUri = uri;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_SMALL_PICTURE);
    }

    protected void setImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            mBitmap = extras.getParcelable("data");

            image.setImageBitmap(mBitmap);//display picture
        }
    }



    private void insert_picture( Context context,Bitmap bitmap_in){
        dbWrite = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap_in.compress(Bitmap.CompressFormat.PNG,100,os);
        cv.put("name",name);
        cv.put("picture",os.toByteArray());
        dbWrite.insert("picture",null,cv);
        dbWrite.close();
    }



}
