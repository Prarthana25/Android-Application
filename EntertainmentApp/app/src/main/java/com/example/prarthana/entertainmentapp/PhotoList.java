package com.example.prarthana.entertainmentapp;

import android.graphics.Bitmap;

public class PhotoList {
    private Bitmap imageUrl;
    public PhotoList(Bitmap imageUrl){
        this.imageUrl=imageUrl;
    }
    public Bitmap getImageUrl(){
        return imageUrl;
    }
}
