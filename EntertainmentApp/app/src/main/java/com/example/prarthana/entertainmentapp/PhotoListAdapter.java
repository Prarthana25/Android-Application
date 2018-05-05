package com.example.prarthana.entertainmentapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PhotoListAdapter extends ArrayAdapter<PhotoList> {
    ArrayList<PhotoList> photos;
    Context context;
    int resources;

    public PhotoListAdapter(Context context,int resources,ArrayList<PhotoList> photos){
        super(context,resources,photos);
        this.context=context;
        this.resources=resources;
        this.photos=photos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView==null){
            LayoutInflater layoutInflater=(LayoutInflater) getContext()
            .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView=layoutInflater.inflate(R.layout.photo_view,null,true);
        }
        PhotoList photoList=getItem(position);
        ImageView imageView=(ImageView) convertView.findViewById(R.id.photoView);
        imageView.setImageBitmap(photoList.getImageUrl());
        return convertView;
    }
}
