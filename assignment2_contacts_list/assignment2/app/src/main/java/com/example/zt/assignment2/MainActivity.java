package com.example.zt.assignment2;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    private Button add,delete;
    //private CheckBox checkbox;
    private ListView listView;
    private TextView title;
    private List<Map<String,Object>> dataList;
    private MyDatabaseHelper dbHelper;
    private SimpleCursorAdapter scAdapter;
    private databaseManager dbManager;
    private CheckBoxCursorAdapter cbAdapter;
    private Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main);

        initDataList();
        add = (Button) findViewById(R.id.add);
        delete = (Button) findViewById(R.id.delete);
        title=(TextView) findViewById(R.id.general_title);
        listView=(ListView)findViewById(R.id.listview);
        dbManager = new databaseManager(this);


        //checkbox adapter
        String sql = "SELECT _id,name,phone FROM contacts_list";
        cursor = dbManager.executeSql(sql,null);
        cbAdapter = new CheckBoxCursorAdapter(this,R.layout.list_view,cursor,
                new String[] {"name"},new int[]{R.id.item_name}, R.id.checkbox,"name",0);
        listView.setAdapter(cbAdapter);



        //add function button
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,Contact_details.class);
                startActivity(intent);
            }
        });



        //delete function button
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbAdapter.notifyDataSetChanged();
                List<String> bn = cbAdapter.getSelectedItems();
                String sql_delete="";
                String sql_delete_re="";
                for(String id : bn){
                    sql_delete="delete from contacts_list where name='" + id + "'";
                    dbManager.executeSql(sql_delete);
                    sql_delete_re="delete from relationship where name1='" + id + "' OR name2='" + id + "'";
                    dbManager.executeSql(sql_delete_re);
                }

                String sql = "SELECT _id,name,phone FROM contacts_list";
                cursor = dbManager.executeSql(sql,null);
                cbAdapter = new CheckBoxCursorAdapter(MainActivity.this,R.layout.list_view,cursor,
                        new String[] {"name"},new int[]{R.id.item_name}, R.id.checkbox,"name",0);

                listView.setAdapter(cbAdapter);

                //title.setText(id_s);

            }
        });

        //list item click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                cursor.moveToPosition(position);
                String name_from=cursor.getString(cursor.getColumnIndex("name"));
                Intent i=new Intent(MainActivity.this,Contact_profile.class);
                i.putExtra("name_from_main",name_from);
                startActivity(i);
            }
        });
    }




    // initialize data (useless)
    private void initDataList() {

        dataList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < 0; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("title", "android" + i);
            map.put("content", "SimpleAdapter" + i);
            dataList.add(map);
        }

    }
}
