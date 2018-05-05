package com.example.prarthana.entertainmentapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.places.GeoDataClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InfoFragment extends Fragment {
    protected GeoDataClient mGeoDataClient;
    protected JSONObject detailsJson;
    protected TextView infoAddress1;
    protected TextView infoAddress2;
    protected TextView infoPhone1;
    protected TextView infoPhone2;
    protected TextView infoPrice1;
    protected TextView infoPrice2;
    protected TextView infoRating1;
    protected RatingBar infoRating2;
    protected TextView infoGoogle1;
    protected TextView infoGoogle2;
    protected TextView infoWebsite1;
    protected TextView infoWebsite2;
    protected LinearLayout addrLayout,priceLayout,phoneLayout,ratingLayout,googleLayout,websiteLayout;
    protected RelativeLayout infoResult,infoNoResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.info_fragment, container, false);

        infoResult=(RelativeLayout)view.findViewById(R.id.infoResult);
        infoNoResult=(RelativeLayout)view.findViewById(R.id.infoNoResult);

        infoAddress1=(TextView)view.findViewById(R.id.infoAddress1);
        infoAddress2=(TextView)view.findViewById(R.id.infoAddress2);

        infoPhone1=(TextView)view.findViewById(R.id.infoPhone1);
        infoPhone2=(TextView)view.findViewById(R.id.infoPhone2);

        infoPrice1=(TextView)view.findViewById(R.id.infoPrice1);
        infoPrice2=(TextView)view.findViewById(R.id.infoPrice2);

        infoRating1=(TextView)view.findViewById(R.id.infoRating1);
        infoRating2=(RatingBar)view.findViewById(R.id.infoRating2);

        infoGoogle1=(TextView)view.findViewById(R.id.infoGoogle1);
        infoGoogle2=(TextView)view.findViewById(R.id.infoGoogle2);

        infoWebsite1=(TextView)view.findViewById(R.id.infoWebsite1);
        infoWebsite2=(TextView)view.findViewById(R.id.infoWebsite2);

        addrLayout=(LinearLayout)view.findViewById(R.id.addrContainer);
        priceLayout=(LinearLayout)view.findViewById(R.id.priceContainer);
        phoneLayout=(LinearLayout)view.findViewById(R.id.phoneContainer);
        ratingLayout=(LinearLayout)view.findViewById(R.id.ratingContainer);
        googleLayout=(LinearLayout)view.findViewById(R.id.googleContainer);
        websiteLayout=(LinearLayout)view.findViewById(R.id.websiteContainer);

        try {
             detailsJson = new JSONObject(getArguments().getString("detailsJson"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        loadInfoDetails(detailsJson);
        return view;
    }
    public void loadInfoDetails(JSONObject jsonObject) {
        try {
            if(!jsonObject.has("result")){
                infoResult.setVisibility(View.GONE);
                infoNoResult.setVisibility(View.VISIBLE);
                return;
            }
            JSONObject detailObj = (JSONObject) jsonObject.getJSONObject("result");

            if(detailObj.has("vicinity")) {
                infoAddress1.setText("Address");
                infoAddress2.setText(detailObj.getString("vicinity"));
            }
            else {
                addrLayout.setVisibility(View.GONE);
            }
            if(detailObj.has("international_phone_number")) {
                infoPhone1.setText("Phone Number");
                infoPhone2.setText(detailObj.getString("international_phone_number"));
            }
            else{
                phoneLayout.setVisibility(View.GONE);
            }
            if(detailObj.has("price_level")) {
                int count=Integer.parseInt(detailObj.getString("price_level"));
                String pricing="";
                for(int i=0;i<count;i++)
                    pricing+="$";
                infoPrice1.setText("Pricing Level");
                infoPrice2.setText(pricing);
            }
            else{
                priceLayout.setVisibility(View.GONE);
            }
            if(detailObj.has("rating")) {
                infoRating1.setText("Rating");
                float rate= Float.parseFloat(detailObj.getString("rating"));
                infoRating2.setRating(rate);
            }
            else{
                ratingLayout.setVisibility(View.GONE);
            }
            if(detailObj.has("url")) {
                infoGoogle1.setText("Google Page");
                infoGoogle2.setText(detailObj.getString("url"));
                infoGoogle2.setMovementMethod(LinkMovementMethod.getInstance());
            }
            else{
                googleLayout.setVisibility(View.GONE);
            }
            if(detailObj.has("website")) {
                infoWebsite1.setText("Website");
                infoWebsite2.setText(detailObj.getString("website"));
                infoWebsite2.setMovementMethod(LinkMovementMethod.getInstance());
            }
            else{
                websiteLayout.setVisibility(View.GONE);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}