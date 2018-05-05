package com.example.prarthana.entertainmentapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment implements FavoriteAdapter.ClickListener {
    private static final String TAG="Favorite";
    protected List<PlacesResult> data=new ArrayList<>();
    protected RelativeLayout favNoResult,favProgress;
    private RecyclerView mRecyclerView;
    private FavoriteAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,@Nullable Bundle savedInstanceState){
        view=inflater.inflate(R.layout.favorite_activity,container,false);
        favProgress=(RelativeLayout)view.findViewById(R.id.favProgress);
        favProgress.setVisibility(View.GONE);
        updateView();
        return view;
    }
    @Override
    public void onResume(){
        super.onResume();
        updateView();
    }

    public void updateView(){
        favNoResult=(RelativeLayout) view.findViewById(R.id.favNoResult);
        favNoResult.setVisibility(View.GONE);
        SharedPreferences sharedPreferences=getActivity().getSharedPreferences("Favorite", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        data=new ArrayList<>();
        int counter=sharedPreferences.getInt("FavCounter",0);
        int count=sharedPreferences.getInt("FavCount",0);
        if(count>0) {
            for (int i = 1; i <= counter; i++) {
                String jsonString = sharedPreferences.getString("Fav" + i, null);
                if (jsonString != null) {
                    Gson gson = new Gson();
                    PlacesResult placesResult = gson.fromJson(jsonString, PlacesResult.class);
                    data.add(placesResult);
                }
            }
            favNoResult.setVisibility(View.GONE);
            loadRecyclerView();
        }
        else
            favNoResult.setVisibility(View.VISIBLE);
    }

    public void loadRecyclerView(){
        mRecyclerView = (RecyclerView) view.findViewById(R.id.favRecycler);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new FavoriteAdapter(getActivity(),data,view);
        mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }
    @Override
    public void itemClicked(View view, final PlacesResult placesResult,String place_id) {
        favProgress.setVisibility(View.VISIBLE);
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
                favProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(),"Results cannot be fetched",Toast.LENGTH_SHORT).show();
            }
        });
        VolleyApplication.getInstance().addToRequestQueue(jsonObjReq, "placesRequest");
    }
    public void startDetailActivity(JSONObject jsonObject, PlacesResult placesResult){
        Gson gson=new Gson();
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("detailsJson",jsonObject.toString());
        bundle.putString("currentPlace",gson.toJson(placesResult));
        intent.putExtras(bundle);
        favProgress.setVisibility(View.GONE);
        startActivity(intent);
    }
}
