package com.example.zt.assignment1;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    private Button btn;
    private EditText input_title;
    private EditText input_description;
    private ListView listView;
    private TextView title;
    private List<Map<String,Object>> dataList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main);
        listView=(ListView)findViewById(R.id.listview);
        initDataList();

        input_title = (EditText) findViewById(R.id.input_title);
        input_description = (EditText) findViewById(R.id.input_description);

        final MyAdapter adapter = new MyAdapter(this, dataList, R.layout.list_view);
        listView.setAdapter(adapter);

        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String serch_textip1=input_title.getText().toString().trim();
                String serch_textip2=input_description.getText().toString().trim();
                if(serch_textip1.length()==0 && serch_textip2.length()==0) {
                    Toast.makeText(MainActivity.this,"Title or description cannot be empty.", Toast.LENGTH_LONG).show();
                }
                else{
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("title", input_title.getText().toString());
                    map.put("content",input_description.getText().toString());
                    dataList.add(map);
                    adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                dataList.remove(position);
                adapter.notifyDataSetChanged();
                listView.setAdapter(adapter);
                return false;
            }
        });
    }

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
