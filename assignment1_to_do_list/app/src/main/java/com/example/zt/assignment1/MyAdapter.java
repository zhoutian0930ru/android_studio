package com.example.zt.assignment1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

public class MyAdapter extends BaseAdapter {
    private List<Map<String, Object>> dataList;
    private Context context;
    private int resource;
    //private CheckBox checkbox;
    public boolean[] flag;

    public MyAdapter(Context context, List<Map<String, Object>> dataList,
                     int resource) {
        this.context = context;
        this.dataList = dataList;
        this.resource = resource;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int index) {

        return dataList.get(index);
    }

    @Override
    public long getItemId(int index) {

        return index;
    }

    @Override
    public View getView(int index, View view, ViewGroup arg2) {
        Util util = null;
        final int flag = index;
        if (view == null) {
            util = new Util();
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(resource, null);
            util.contentTextView = (TextView) view
                    .findViewById(R.id.item_content);
            util.titleTextView = (TextView) view.findViewById(R.id.item_title);
            util.checkbox = (CheckBox) view.findViewById(R.id.checkbox);
            view.setTag(util);
        } else {
            util = (Util) view.getTag();
        }
        Map<String, Object> map = dataList.get(index);
        util.contentTextView.setText((String) map.get("content"));
        util.titleTextView.setText((String) map.get("title"));


        util.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataList.remove(flag);
                notifyDataSetChanged();
            }
        });
        return view;
    }

}


class Util {
    ImageView imageView;
    TextView contentTextView, dateTextView, titleTextView;
    Button infoButton, deleteButton;
    CheckBox checkbox;
}
