package com.example.prarthana.entertainmentapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.TextView;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SectionPageAdapter msectionPageAdapter;
    private ViewPager mViewPager;
    protected JSONObject fragmentData;
    protected TabLayout tabLayout;
    protected PlacesResult currentPlace;
    protected Intent intent;
    protected Bundle extraBundle;
    protected String tweetMessage="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_result);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        intent = getIntent();
        extraBundle = intent.getExtras();

        Gson gson=new Gson();
        currentPlace=gson.fromJson(extraBundle.getString("currentPlace"),PlacesResult.class);
        getSupportActionBar().setTitle(currentPlace.place_name);
        getTweetMessage();

        InfoFragment infoFragment = new InfoFragment();
        infoFragment.setArguments(extraBundle);

        PhotoFragment photoFragment=new PhotoFragment();
        photoFragment.setArguments(extraBundle);

        MapFragment mapFragment=new MapFragment();
        mapFragment.setArguments(extraBundle);

        ReviewFragment reviewFragment = new ReviewFragment();
        reviewFragment.setArguments(extraBundle);

        mViewPager=(ViewPager)findViewById(R.id.details_container);
        DetailsPageAdapter adapter=new DetailsPageAdapter(getSupportFragmentManager(),this);
        adapter.addFragment(infoFragment, "Info");
        adapter.addFragment(photoFragment, "Photos");
        adapter.addFragment(mapFragment, "Maps");
        adapter.addFragment(reviewFragment, "Reviews");
        mViewPager.setAdapter(adapter);

        tabLayout=(TabLayout) findViewById(R.id.details_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    public void getTweetMessage(){
        try {
            JSONObject jsonObject = new JSONObject(extraBundle.getString("detailsJson"));
            if(!jsonObject.has("result")) return;
            JSONObject detailObj = (JSONObject) jsonObject.getJSONObject("result");
            String name=detailObj.has("name")?detailObj.getString("name") :"";
            String address=detailObj.has("vicinity") ?detailObj.getString("vicinity"):"";
            String website=detailObj.has("website") ?detailObj.getString("website") :(detailObj.has("url")?detailObj.getString("url"):"");
            tweetMessage+="Check out " +name+" located at "+ address +". Website:"+website;
        }
        catch (Exception e){

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.details_menu,menu);
        if(currentPlace.isFav)
            menu.getItem(1).setIcon(R.drawable.heart_white_fill);
        else
            menu.getItem(1).setIcon(R.drawable.hear_white_blank);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.detail_menu_fav:
                if(currentPlace.isFav) {
                    currentPlace.isFav=false;
                    menuItem.setIcon(R.drawable.hear_white_blank);
                    removeFavorite();
                }
                else{
                    currentPlace.isFav=true;
                    menuItem.setIcon(R.drawable.heart_white_fill);
                    addFavorite();
                }
                break;

            case R.id.detail_menu_tweet:
                Intent tweet = new Intent(Intent.ACTION_VIEW);
                tweet.setData(Uri.parse("https://twitter.com/intent/tweet?text=" + Uri.encode(tweetMessage)+"&hashtags=TravelAndEntertainmentSearch"));
                startActivity(tweet);
                break;

            case android.R.id.home:
                this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    public void addFavorite(){
        SharedPreferences sharedPreferences=this.getSharedPreferences("Favorite", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int count=sharedPreferences.getInt("FavCount",0);
        int counter=sharedPreferences.getInt("FavCounter",0);
        count++;
        counter++;
        Gson gson=new Gson();
        editor.putString("Fav"+counter,gson.toJson(currentPlace));
        editor.putInt("FavCounter",counter);
        editor.putInt("FavCount",count);
        editor.putInt(currentPlace.place_id,counter);
        editor.commit();
        String message=currentPlace.place_name+" is added to favorite";
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    public void removeFavorite(){
        SharedPreferences sharedPreferences=this.getSharedPreferences("Favorite",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int count=sharedPreferences.getInt("FavCount",0);
        String key=currentPlace.place_id;
        int counter=sharedPreferences.getInt(key,0);
        count--;
        editor.remove("Fav"+counter);
        editor.remove(key);
        editor.putInt("FavCount",count);
        editor.commit();
        String message=currentPlace.place_name+" is removed from favorite";
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    public void temporary(){
        Bundle bundle = new Bundle();
        bundle.putString("detailsJson", fragmentData.toString());

        InfoFragment infoFragment = new InfoFragment();
        infoFragment.setArguments(bundle);

        ReviewFragment reviewFragment = new ReviewFragment();
        reviewFragment.setArguments(bundle);

        mViewPager=(ViewPager)findViewById(R.id.details_container);
        DetailsPageAdapter adapter=new DetailsPageAdapter(getSupportFragmentManager(),this);
        adapter.addFragment(infoFragment, "Info");
        adapter.addFragment(new PhotoFragment(), "Photos");
        adapter.addFragment(new MapFragment(), "Maps");
        adapter.addFragment(reviewFragment, "Reviews");
        mViewPager.setAdapter(adapter);

        tabLayout=(TabLayout) findViewById(R.id.details_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }
}
