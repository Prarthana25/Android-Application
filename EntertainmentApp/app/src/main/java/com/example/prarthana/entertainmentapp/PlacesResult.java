package com.example.prarthana.entertainmentapp;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.app.Activity;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;
import org.json.JSONException;


public class PlacesResult {
    String place_id;
    String place_name;
    String place_category;
    String place_address;
    boolean isFav;

    PlacesResult(String place_id,String place_name,String place_category,String place_address,boolean isFav)
    {
        this.place_id=place_id;
        this.place_name=place_name;
        this.place_address=place_address;
        this.place_category=place_category;
        this.isFav=isFav;
    }
}
