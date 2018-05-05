package com.example.prarthana.entertainmentapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ReviewFragment extends Fragment implements ReviewListAdapter.ClickListener,AdapterView.OnItemSelectedListener {
    protected View view;
    protected List<Review> googleReview;
    protected List<Review> yelpReview;
    private RecyclerView mRecyclerView;
    private ReviewListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    protected Spinner reviewSpinner;
    protected Spinner reviewOrderSpinner;
    protected JSONObject detailsJson;
    protected int check=0;
    protected boolean yelpCheck=false;
    protected RelativeLayout reviewResult,reviewNoResult,yelpProgress;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        check=0;

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.review_fragment, container, false);

        yelpProgress = (RelativeLayout) view.findViewById(R.id.yelpProgress);
        yelpProgress.setVisibility(View.GONE);

        reviewResult=(RelativeLayout)view.findViewById(R.id.reviewResult);
        reviewNoResult=(RelativeLayout)view.findViewById(R.id.reviewNoResult);

        reviewSpinner=view.findViewById(R.id.reviewSpinner);
        reviewOrderSpinner=view.findViewById(R.id.reviewOrderSpinner);

        ArrayAdapter<CharSequence> reviewAdapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.reviewName, android.R.layout.simple_spinner_item);
        reviewAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reviewSpinner.setAdapter(reviewAdapter);
        reviewSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> reviewOrderAdapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.reviewOrder, android.R.layout.simple_spinner_item);
        reviewOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reviewOrderSpinner.setAdapter(reviewOrderAdapter);
        reviewOrderSpinner.setOnItemSelectedListener(this);

        try {
            detailsJson = new JSONObject(getArguments().getString("detailsJson"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        loadGoogleReviews(detailsJson);
        return view;
    }
    public void loadGoogleReviews(JSONObject jsonObject) {
        googleReview=new ArrayList<>();
        try {
            if (jsonObject.has("result")) {
                JSONObject resultObj = jsonObject.getJSONObject("result");
                if (resultObj.has("reviews") && resultObj.getJSONArray("reviews").length() > 0) {
                    JSONArray jsonArray = resultObj.getJSONArray("reviews");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonReview = jsonArray.getJSONObject(i);
                        String author_name = jsonReview.has("author_name") ? jsonReview.getString("author_name") : "";
                        String author_url = jsonReview.has("author_url") ? jsonReview.getString("author_url") : "";
                        String profile_photo_url = jsonReview.has("profile_photo_url") ? jsonReview.getString("profile_photo_url") : "";
                        float rating = jsonReview.has("rating") ? Float.parseFloat(jsonReview.getString("rating")) : 0;
                        String text = jsonReview.has("text") ? jsonReview.getString("text") : "";
                        String time = jsonReview.has("time") ? jsonReview.getString("time") : "";
                        Date date = new Date(TimeUnit.MILLISECONDS.convert(Long.parseLong(time), TimeUnit.SECONDS));
                        Review review = new Review(i, author_name, author_url, profile_photo_url, rating, text, date);
                        googleReview.add(review);
                    }
                }
            }
            else{

            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        loadReviewRecyclerView(googleReview);
    }
    public void loadYelpReviews(JSONObject jsonObject){
        yelpReview=new ArrayList<>();
        if(jsonObject!=null && jsonObject.has("reviews")){
            try {
                JSONArray jsonArray=jsonObject.getJSONArray("reviews");
                for(int i=0;i<jsonArray.length();i++){
                    String name="",image_url="";
                    String url,time,text;
                    float rating;
                    Date date=new Date();
                    JSONObject reviewObj=jsonArray.getJSONObject(i);
                    if(reviewObj.has("user")){
                        JSONObject userObj=reviewObj.getJSONObject("user");
                        name=userObj.has("name") ?userObj.getString("name"):"";
                        image_url=userObj.has("image_url")?userObj.getString("image_url"):"";
                    }
                    url=reviewObj.has("url") ?reviewObj.getString("url") :"";
                    rating=reviewObj.has("rating") ?Float.parseFloat(reviewObj.getString("rating")) :-1;
                    time=reviewObj.has("time_created") ?reviewObj.getString("time_created") :"";
                    text=reviewObj.has("text") ?reviewObj.getString("text") :"";
                    if(!time.equals("")){
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        date=format.parse(time);
                    }
                    Review review=new Review(i,name,url,image_url,rating,text,date);
                    yelpReview.add(review);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        yelpProgress.setVisibility(View.GONE);
        displayReviews();
    }
    public void getYelpMatch(){
            yelpCheck=true;
            String yelpMatchUrl=getYelpMatchUrl();
            if(!yelpMatchUrl.isEmpty()){
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                        yelpMatchUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                            getYelpReviews(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(),"Results cannot be fetched",Toast.LENGTH_SHORT).show();
                    }
                });
                VolleyApplication.getInstance().addToRequestQueue(jsonObjReq, "placesRequest");
            }
            else{
                getYelpReviews(null);
            }
    }
    public String getYelpMatchUrl(){
        String yelpMatchUrl="";
        if(detailsJson.has("result")) {
            try{
                JSONObject detailsResult=detailsJson.getJSONObject("result");
                //Replace with your Node.js url
                yelpMatchUrl="http://homework9-env.us-east-1.elasticbeanstalk.com/yelpMatch?";
                if(detailsResult.has("name"))
                    yelpMatchUrl+="name="+detailsResult.getString("name");
                if(detailsResult.has("address_components")){

                    JSONArray addressArray=detailsResult.getJSONArray("address_components");
                    for(int i=0;i<addressArray.length();i++) {
                        yelpMatchUrl+="&";
                        JSONObject addressObj = addressArray.getJSONObject(i);
                        yelpMatchUrl+=addressObj.getJSONArray("types").get(0)+"="+ URLEncoder.encode(addressObj.getString("short_name"),"utf-8");
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return yelpMatchUrl;
    }
    public void getYelpReviews(JSONObject jsonObject){
        if(jsonObject!=null && jsonObject.has("businesses")) {
            try {
                JSONArray yelpBusiness = jsonObject.getJSONArray("businesses");
                JSONObject bestMatch;
                if(yelpBusiness.length()>0) {
                    bestMatch = yelpBusiness.getJSONObject(0);
                    String businessId=bestMatch.getString("id");
                    //Replace with your Node.js url
                    String yelpReviewUrl="http://homework9-env.us-east-1.elasticbeanstalk.com/yelpReviews?id="+URLEncoder.encode(businessId,"utf-8");
                    JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                            yelpReviewUrl, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            loadYelpReviews(response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            yelpProgress.setVisibility(View.GONE);
                            Toast.makeText(getContext(),"Results cannot be fetched",Toast.LENGTH_SHORT).show();
                        }
                    });
                    VolleyApplication.getInstance().addToRequestQueue(jsonObjReq, "placesRequest");
                }
                else {
                    loadYelpReviews(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
            loadYelpReviews(null);
    }
    public void loadReviewRecyclerView(List<Review> data){
        if(data==null || data.size()==0){
            reviewResult.setVisibility(View.GONE);
            reviewNoResult.setVisibility(View.VISIBLE);
            return;
        }
        reviewResult.setVisibility(View.VISIBLE);
        reviewNoResult.setVisibility(View.GONE);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.reviewRecycler);
        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());


        mAdapter = new ReviewListAdapter(getActivity().getApplicationContext(),data);
        mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    public void displayReviews(){
        String reviewName = ((Spinner) view.findViewById(R.id.reviewSpinner)).getSelectedItem().toString();
        String reviewOrder = ((Spinner) view.findViewById(R.id.reviewOrderSpinner)).getSelectedItem().toString();

        List<Review> currentObj;
        if(reviewName.equals("Google Reviews"))
            currentObj=googleReview;
        else
            currentObj=yelpReview;

        switch (reviewOrder){
            case "Lowest Rating":
                Collections.sort(currentObj,Review.lowRatingComparator());
                break;
            case "Highest Rating":
                Collections.sort(currentObj,Review.highRatingComparator());
                break;
            case "Most Recent":
                Collections.sort(currentObj,Review.mostRecentComparator());
                break;
            case "Least Recent":
                Collections.sort(currentObj,Review.leastRecentComparator());
                break;
            default:
                Collections.sort(currentObj,Review.defaultComparator());
                break;
        }
        loadReviewRecyclerView(currentObj);
    }
    @Override
    public void itemClicked(View view,String url) {
            if(!url.isEmpty()){
                Uri uri=Uri.parse(url);
                Intent intent=new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(check++<=1)
            return;
        AppCompatSpinner spinner = (AppCompatSpinner) parent;
        if(spinner.getId()==R.id.reviewSpinner){
            String reviewName=spinner.getSelectedItem().toString();
            if(reviewName.equals("Google Reviews")){
                displayReviews();
            }
            else{
                if(yelpCheck)
                    displayReviews();
                else{
                    yelpProgress.setVisibility(View.VISIBLE);
                    getYelpMatch();
                }
            }
        }
        else{
            displayReviews();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
