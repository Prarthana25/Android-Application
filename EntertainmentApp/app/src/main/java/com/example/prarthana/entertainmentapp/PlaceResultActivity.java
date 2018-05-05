package com.example.prarthana.entertainmentapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PlaceResultActivity extends AppCompatActivity implements PlaceAdapter.ClickListener{
    private RecyclerView mRecyclerView;
    private PlaceAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<PlacesResult> data1;
    private List<PlacesResult> data2;
    private List<PlacesResult> data3;
    protected JSONObject placeJson1,placeJson2,placeJson3;
    protected Button previousButton;
    protected Button nextButton;
    protected int currentPage=1;
    protected String page2Token="";
    protected String page3Token="";
    protected RelativeLayout resultLayout,noResultLayout,resultButtonLayout,placeProgress;
    protected List<String> favList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.places_result);

        placeProgress=(RelativeLayout)findViewById(R.id.placeProgress);
        placeProgress.setVisibility(View.GONE);
        resultLayout=(RelativeLayout) findViewById(R.id.placeResult);
        resultButtonLayout=(RelativeLayout) findViewById(R.id.placeResultButton);
        noResultLayout=(RelativeLayout) findViewById(R.id.placeNoResult);
        noResultLayout.setVisibility(View.GONE);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        currentPage=1;

        Intent intent = getIntent();
        Bundle extraBundle = intent.getExtras();
        previousButton=(Button)findViewById(R.id.previous);
        nextButton=(Button)findViewById(R.id.next);
        try {
            placeJson1 = new JSONObject(extraBundle.getString("placeJson"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(placeJson1.has("next_page_token")) {
            try {
                page2Token=placeJson1.getString("next_page_token");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
            nextButton.setEnabled(false);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(currentPage==1){
                        currentPage=2;
                        if(data2!=null)
                            loadRecyclerView();
                        else{
                            getNextPlaceResult();
                        }
                    }
                    else{
                        currentPage=3;
                        if(data3!=null)
                            loadRecyclerView();
                        else{
                            getNextPlaceResult();
                        }
                    }

            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPage==2){
                    currentPage=1;
                    loadRecyclerView();
                }
                else {
                    currentPage = 2;
                    loadRecyclerView();
                }
            }
        });
    }
    @Override
    public void onResume(){
        super.onResume();
        getFavoriteList();
        JSONObject jsonObject;
        if(currentPage==1)
            jsonObject=placeJson1;
        else if(currentPage==2)
            jsonObject=placeJson2;
        else
            jsonObject=placeJson3;
        loadPlacesResultArray(jsonObject);
    }

    public void getNextPlaceResult(){
        placeProgress.setVisibility(View.VISIBLE);
            String token="";
            if(currentPage==2)
                token=page2Token;
            else
                token=page3Token;

            if(!token.equals("")){
                //Replace with your Node.js url
                String url="http://homework9-env.us-east-1.elasticbeanstalk.com/nextPage?next_page_token="+token;
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                        url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                            saveCurrentJson(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        placeProgress.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),"Results cannot be fetched",Toast.LENGTH_SHORT).show();
                    }
                });
                VolleyApplication.getInstance().addToRequestQueue(jsonObjReq, "placesRequest");
            }
            else{
                placeProgress.setVisibility(View.GONE);
            }
    }

    public void getFavoriteList(){
        SharedPreferences sharedPreferences=this.getSharedPreferences("Favorite", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int counter=sharedPreferences.getInt("FavCounter",0);
        favList=new ArrayList<>();
        for(int i=1;i<=counter;i++){
            String jsonString=sharedPreferences.getString("Fav"+i,null);
            if(jsonString!=null){
                Gson gson=new Gson();
                PlacesResult placesResult=gson.fromJson(jsonString,PlacesResult.class);
                favList.add(placesResult.place_id);
            }
        }
    }

    public void saveCurrentJson(JSONObject jsonObject){
        if(currentPage==2){
            placeJson2=jsonObject;
            if(jsonObject.has("next_page_token")) {
                try {
                    page3Token = jsonObject.getString("next_page_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
                loadPlacesResultArray(jsonObject);
        }
        else {
            placeJson3=jsonObject;
            loadPlacesResultArray(jsonObject);
        }
    }

    public void loadPlacesResultArray(JSONObject jsonObject){
        placeProgress.setVisibility(View.GONE);
        List<PlacesResult> data;
        if(currentPage==1) {
            data1 = new ArrayList<>();
            data=data1;
        }
        else if(currentPage==2){
            data2=new ArrayList<>();
            data=data2;
        }
        else {
            data3 = new ArrayList<>();
            data=data3;
        }
        try {
            if(jsonObject.has("results") && jsonObject.getJSONArray("results").length()>0 ) {
                JSONArray jsonArray = (JSONArray) jsonObject.getJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = (JSONObject) jsonArray.get(i);
                    String place_id=obj.has("place_id")?obj.getString("place_id"):"";
                    String place_name=obj.has("name")?obj.getString("name"):"";
                    String place_icon=obj.has("icon")?obj.getString("icon"):"";
                    String place_vicinity=obj.has("vicinity")?obj.getString("vicinity"):"";
                    boolean isFav=favList.contains(place_id) ?true :false;
                    PlacesResult placesResult = new PlacesResult(place_id,place_name,place_icon,place_vicinity,isFav);
                    data.add(placesResult);
                }
                loadRecyclerView();
            }
            else{
                noResultLayout.setVisibility(View.VISIBLE);
                resultButtonLayout.setVisibility(View.GONE);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void loadRecyclerView(){
        List<PlacesResult> data;
        if(currentPage==1) {
            data = data1;
            previousButton.setEnabled(false);
            if (page2Token.equals(""))
                nextButton.setEnabled(false);
            else
                nextButton.setEnabled(true);
        }
        else if(currentPage==2) {
            data = data2;
            previousButton.setEnabled(true);
            if (page3Token.equals(""))
                nextButton.setEnabled(false);
            else
                nextButton.setEnabled(true);
        }
        else {
            data = data3;
            previousButton.setEnabled(true);
            nextButton.setEnabled(false);
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.places_view1);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new PlaceAdapter(this,data);
        mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    public void itemClicked(View view, final PlacesResult placesResult, final String place_id) {
        placeProgress.setVisibility(View.VISIBLE);
        //Replace with your Node.js url
        String detailsUrl="http://homework9-env.us-east-1.elasticbeanstalk.com/details?place_id="+place_id;
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                detailsUrl, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                startDetailActivity(response,placesResult);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                placeProgress.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"Results cannot be fetched",Toast.LENGTH_SHORT).show();
            }
        });
        VolleyApplication.getInstance().addToRequestQueue(jsonObjReq, "placesRequest");

    }

    public void startDetailActivity(JSONObject jsonObject, PlacesResult placesResult){
        Gson gson=new Gson();
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle bundle=new Bundle();
        bundle.putString("detailsJson",jsonObject.toString());
        bundle.putString("currentPlace",gson.toJson(placesResult));
        intent.putExtras(bundle);
        placeProgress.setVisibility(View.GONE);
        startActivity(intent);
    }
}
