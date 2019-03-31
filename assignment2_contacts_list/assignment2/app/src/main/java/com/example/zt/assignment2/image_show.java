package com.example.zt.assignment2;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.ImageView;


public class image_show extends Activity {

    private Bitmap mBitmap;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_image);

        image=(ImageView)findViewById(R.id.full_image);

        Intent i = getIntent();

        byte[] full_image=i.getByteArrayExtra("image");
        mBitmap= BitmapFactory.decodeByteArray(full_image,0,full_image.length,null);
        image.setImageBitmap(mBitmap);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        finish();
        return true;
    }

}
