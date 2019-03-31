package com.example.zt.assignment2;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;


public class contact_list_fragment extends Fragment {
    private Button add,delete;
    private ListView listView;
    private TextView title;
    private databaseManager dbManager;
    private CheckBoxCursorAdapter cbAdapter;
    private Cursor cursor;


    public contact_list_fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact_list, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("onCreate******************************");
    }


   public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //bind widgets in fragment

        add = (Button) view.findViewById(R.id.add);
        delete = (Button) view.findViewById(R.id.delete);
        title=(TextView) view.findViewById(R.id.general_title);
        listView=(ListView)view.findViewById(R.id.listview);
        dbManager = new databaseManager(this.getActivity());



        //checkbox adapter
        String sql = "SELECT _id,name,phone FROM contacts_list";
        cursor = dbManager.executeSql(sql,null);
        cbAdapter = new CheckBoxCursorAdapter(this.getActivity(),R.layout.list_view,cursor,
                new String[] {"name"},new int[]{R.id.item_name}, R.id.checkbox,"name",0);
        listView.setAdapter(cbAdapter);



        //add function button
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),Contact_details.class);
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
                cbAdapter = new CheckBoxCursorAdapter(getActivity(),R.layout.list_view,cursor,
                        new String[] {"name"},new int[]{R.id.item_name}, R.id.checkbox,"name",0);

                listView.setAdapter(cbAdapter);

            }
        });

        //list item click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                cursor.moveToPosition(position);
                String name_from=cursor.getString(cursor.getColumnIndex("name"));
                Intent i=new Intent(getActivity(),Contact_profile.class);
                i.putExtra("name_from_main",name_from);
                startActivity(i);
            }
        });
    }


}
