package edu.skku.map.pa2;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MyGridViewAdapter extends BaseAdapter {
    private Context context;
    static public ArrayList<Bitmap> gridImages;

    public MyGridViewAdapter(Context con,ArrayList<Bitmap> img){
        context=con;
        gridImages=img;
    };

    @Override
    public int getCount() {
        return gridImages.size();
    }

    @Override
    public Object getItem(int position) {
        return gridImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if(convertView==null){
            imageView=new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(50,50));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(6,6,6,6);
        }
        else{
            imageView=(ImageView)convertView;
        }
        imageView.setImageBitmap(gridImages.get(position));
        return imageView;

    }

}
