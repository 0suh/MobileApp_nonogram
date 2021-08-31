package edu.skku.map.pa2;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import edu.skku.map.pa2.R;

public class MyGridNum1 extends BaseAdapter {
    Context context;
    int layout;
    private LayoutInflater inflater;
    private ArrayList<Integer> integers;

    public MyGridNum1(Context con, int layout, ArrayList<Integer> ints){
        this.context = con;
        this.layout = layout;
        this.integers = ints;
        this.inflater = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    };
    @Override
    public int getCount() {
        return integers.size();
    }

    @Override
    public Object getItem(int position) {
        return integers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView text=null;
        if (convertView == null) {
            convertView = inflater.inflate(layout, parent,false);
        }
        text = (TextView) convertView.findViewById(R.id.textView);
        text.setText(integers.get(position).toString());
        return convertView;
    }

}
