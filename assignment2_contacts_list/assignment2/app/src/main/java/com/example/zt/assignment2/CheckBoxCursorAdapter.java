package com.example.zt.assignment2;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class CheckBoxCursorAdapter extends SimpleCursorAdapter
{
    private ArrayList<String> selection = new ArrayList<String>();
    private int mCheckBoxId = 0;
    private String mIdColumn;
    public CheckBoxCursorAdapter(Context context, int layout, Cursor c,
                                    String[] from, int[] to, int checkBoxId, String idColumn,
                                    int flags)
    {
        super(context, layout, c, from, to, flags);
        mCheckBoxId = checkBoxId;
        mIdColumn = idColumn;
    }


    @Override
    public int getCount()
    {
        return super.getCount();
    }


    @Override
    public Object getItem(int position)
    {
        return super.getItem(position);
    }


    @Override
    public long getItemId(int position)
    {
        return super.getItemId(position);
    }


    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent)
    {
        View view = super.getView(position, convertView, parent);
        final CheckBox checkbox = (CheckBox) view.findViewById(mCheckBoxId);
        checkbox.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Cursor cursor = getCursor();
                cursor.moveToPosition(position);

                checkbox.setChecked(checkbox.isChecked());
                if(checkbox.isChecked())
                {
                    selection.add(cursor.getString(cursor.getColumnIndex(mIdColumn)));
                }
                else//否则移除
                {
                    selection.remove(new String(cursor.getString(cursor.getColumnIndex(mIdColumn))));
                    //Toast.makeText(context, "has removed " + cursor.getInt(cursor.getColumnIndex(mIdColumn)), 0).show();
                }
            }
        });

        return view;
    }

    public ArrayList<String> getSelectedItems()
    {
        return selection;
    }
}
