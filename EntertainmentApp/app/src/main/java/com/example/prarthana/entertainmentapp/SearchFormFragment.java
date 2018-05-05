package com.example.prarthana.entertainmentapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import android.widget.AdapterView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static android.content.Context.LOCATION_SERVICE;


public class SearchFormFragment extends Fragment {
    private static final String TAG = "Search";
    protected JSONObject placeJson;
    protected TextInputLayout keywordLayout, autoCompleteLayout;
    protected EditText keywordText, distanceText;
    protected Button searchButton, clearButton;
    protected RadioGroup locationGroup;
    protected AutoCompleteTextView searchPlace;
    protected Spinner categorySpinner;
    protected String placesUrl = "";
    protected RelativeLayout searchProgress;
    protected double latitude, longitude;
    protected String[] categoryValue;

    private static final int PERMISSION_REQUEST_CODE = 11;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 10;
    protected Location deviceLocation;
    protected LocationManager locationManager;
    protected LocationListener locationListener;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.searchform, container, false);
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                deviceLocation=location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            requestRuntimePermission();
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationListener);
        }
        searchProgress = (RelativeLayout) view.findViewById(R.id.searchProgress);
        searchProgress.setVisibility(View.GONE);
        searchButton = view.findViewById(R.id.search);
        keywordText = (EditText) view.findViewById(R.id.keyword);
        keywordLayout = (TextInputLayout) view.findViewById(R.id.keywordLayout);
        keywordLayout.setHintAnimationEnabled(false);
        keywordText.setHint("Enter a keyword");
        autoCompleteLayout = (TextInputLayout) view.findViewById(R.id.autoCompleteLayout);
        autoCompleteLayout.setHintAnimationEnabled(false);
        searchPlace = view.findViewById(R.id.otherLocation);
        searchPlace.setHint("Type in a location");
        distanceText = (EditText) view.findViewById(R.id.distance);
        locationGroup = (RadioGroup) view.findViewById(R.id.locationRadio);
        locationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio1) {
                    searchPlace.setEnabled(false);
                    autoCompleteLayout.setErrorEnabled(false);
                } else
                    searchPlace.setEnabled(true);

            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performValidation();
            }
        });

        categorySpinner = (Spinner) view.findViewById(R.id.categorySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.category, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        Resources res = getResources();
        categoryValue=res.getStringArray(R.array.categoryvalue);

        CustomAutoCompleteAdapter autoAdapter = new CustomAutoCompleteAdapter(getActivity().getApplicationContext());
        searchPlace.setAdapter(autoAdapter);

        clearButton=view.findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keywordText.setText("");
                distanceText.setText("");
                searchPlace.setText("");
                categorySpinner.setSelection(0);
                locationGroup.check(R.id.radio1);
                searchPlace.setEnabled(false);
                keywordLayout.setErrorEnabled(false);
                autoCompleteLayout.setErrorEnabled(false);
            }
        });
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationListener);
            }
        }
    }

    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(getActivity(),new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        },1);
    }
    public void performValidation(){
        boolean check=true;
        if(keywordText.getText().toString().isEmpty() || keywordText.getText().toString().trim().length()==0){
            keywordLayout.setError("Please enter mandatory field");
            check=false;
        }
        else
            keywordLayout.setErrorEnabled(false);

        if(searchPlace.isEnabled()){
            if(searchPlace.getText().toString().isEmpty() || searchPlace.getText().toString().trim().length()==0) {
                autoCompleteLayout.setError("Please enter mandatory field");
                check=false;
            }
            else
                autoCompleteLayout.setErrorEnabled(false);
        }
        if(!check)
            Toast.makeText(getContext(),"Please fix all fields with error",Toast.LENGTH_SHORT).show();
        else
            getFormData();
    }
    public void getFormData() {
        searchProgress.setVisibility(View.VISIBLE);
        try {
            String keyword = URLEncoder.encode(keywordText.getText().toString(),"utf-8");
            int position=categorySpinner.getSelectedItemPosition();
            String category = URLEncoder.encode(categoryValue[position],"utf-8");
            String distance = URLEncoder.encode(distanceText.getText().toString(),"utf-8");
            String ipLocation="34.0266,-118.2873";

            if(deviceLocation!=null)
                ipLocation=deviceLocation.getLatitude()+","+deviceLocation.getLongitude();
            //Replace with your Node.js url
            placesUrl = "http://homework9-env.us-east-1.elasticbeanstalk.com/places?keyword="+keyword+"&category="+category+"&distance="+distance;
            int radioButtonID = locationGroup.getCheckedRadioButtonId();
            String location = ((RadioButton) locationGroup.findViewById(radioButtonID)).getText().toString();
            if (location.equals("Other")) {
                String otherLocation = searchPlace.getText().toString();

                //Replace with your Node.js url
                String geocodeUrl = "http://homework9-env.us-east-1.elasticbeanstalk.com/geocode?otherLocation=" + URLEncoder.encode(otherLocation, "utf-8");
                final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                        geocodeUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.has("results")) {
                            try {
                                JSONArray jsonArray = response.getJSONArray("results");
                                if (jsonArray.length() > 0) {
                                    JSONObject geocodeObj = jsonArray.getJSONObject(0);
                                    String ipOther = geocodeObj.getJSONObject("geometry").getJSONObject("location").getString("lat") + "," + geocodeObj.getJSONObject("geometry").getJSONObject("location").getString("lng");
                                    placesUrl += "&location=Other&ipOther=" + ipOther;
                                    getPlaceJson(placesUrl);
                                } else {
                                    Toast.makeText(getContext(),"Results cannot be fetched",Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        else{
                            searchProgress.setVisibility(View.GONE);
                            Toast.makeText(getContext(),"Location cannot be fetched",Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        searchProgress.setVisibility(View.GONE);
                        Toast.makeText(getContext(),"Location cannot be fetched",Toast.LENGTH_SHORT).show();
                    }
                });
                VolleyApplication.getInstance().addToRequestQueue(jsonObjReq, "placesRequest");
            }
            else {
                placesUrl+="&location=Here&ipLocation="+ipLocation;
                getPlaceJson(placesUrl);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getPlaceJson(String url){
        //Replace with your Node.js url
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                placeJson=response;
                startPlaceActivity();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                searchProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(),"Results cannot be fetched",Toast.LENGTH_SHORT).show();
            }
        });
        VolleyApplication.getInstance().addToRequestQueue(jsonObjReq, "placesRequest");
    }
    public void startPlaceActivity(){
        Intent intent = new Intent(getActivity(), PlaceResultActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("placeJson",placeJson.toString());
        intent.putExtras(bundle);
        searchProgress.setVisibility(View.GONE);
        startActivity(intent);
    }
}
