package com.example.prarthana.entertainmentapp;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback,OnItemSelectedListener {
    protected GeoDataClient mGeoDataClient;
    protected MapView mMapView;
    protected GoogleMap mMap;
    protected View view;
    protected Spinner spinner;
    protected Marker originMarker;
    protected Marker destinationMarker;
    protected List<Polyline> polylinePath;
    protected String startAddress,endAddress;
    protected LatLng startLocation,endLocation;
    protected PlacesResult placesResult;
    protected AutoCompleteTextView searchPlace;
    protected String travelMode;
    protected String endCoordinates;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.map_fragment, container, false);
        spinner = (Spinner) view.findViewById(R.id.travelModeSpinner);

        Gson gson=new Gson();
        placesResult=gson.fromJson(getArguments().getString("currentPlace"),PlacesResult.class);
        endAddress=placesResult.place_name;

        try {
            JSONObject jsonObject= new JSONObject(getArguments().getString("detailsJson"));
            JSONObject locationObject=jsonObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");
            endLocation=new LatLng(locationObject.getDouble("lat"),locationObject.getDouble("lng"));
            endCoordinates=locationObject.getDouble("lat")+","+locationObject.getDouble("lng");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.mode, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        searchPlace = view.findViewById(R.id.mapFromLocation);

        CustomAutoCompleteAdapter autoAdapter =  new CustomAutoCompleteAdapter(getActivity().getApplicationContext());
        searchPlace.setAdapter(autoAdapter);
        searchPlace.setOnItemClickListener(onItemClickListener);
        return view;
    }
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);
        SupportMapFragment mapFragment=(SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap=googleMap;
        Marker initialMarker=mMap.addMarker(new MarkerOptions().position(endLocation).title(placesResult.place_name));
        initialMarker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endLocation,16));
    }
    private AdapterView.OnItemClickListener onItemClickListener =
            new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    getLocation();
                }
            };


    public void getLocation(){
        startAddress=searchPlace.getText().toString();
        if(startAddress==null || startAddress.length()==0)
            return;
        travelMode=spinner.getSelectedItem().toString();
        mMap.clear();

        try {
            //Replace with your Node.js url
            String directionUrl="http://homework9-env.us-east-1.elasticbeanstalk.com/mapDirection?origin=" + URLEncoder.encode(startAddress,"utf-8") + "&destination=" + endCoordinates + "&mode="+travelMode;
            getDirectionJson(directionUrl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void getDirectionJson(String directionUrl) {
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    directionUrl, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    loadDirectionResult(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Error.Response", error.toString());
                }
            });
            VolleyApplication.getInstance().addToRequestQueue(jsonObjReq, "placesRequest");
    }
    public void loadDirectionResult(JSONObject jsonObject){
        try {
            if (jsonObject.has("routes")) {
                JSONArray jsonRoute = jsonObject.getJSONArray("routes");
                if (jsonRoute.length() > 0) {
                    JSONArray jsonLegs = jsonRoute.getJSONObject(0).getJSONArray("legs");
                    JSONObject jsonLeg = jsonLegs.getJSONObject(0);
                    JSONArray jsonSteps = jsonLeg.getJSONArray("steps");
                    JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");
                    startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));

                    String[] polylines = getPaths(jsonSteps);
                    displayRoute(polylines);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public String[] getPaths(JSONArray jsonSteps){
        int count=jsonSteps.length();
        String[] polylines=new String[count];

        for(int i=0;i<count;i++){
            try {
                polylines[i] = getPath(jsonSteps.getJSONObject(i));
            }
            catch(Exception e){}
        }
        return polylines;
    }
    public String getPath(JSONObject googlePath){
        String polyline="";
        try{
            polyline=googlePath.getJSONObject("polyline").getString("points");
        }
        catch(Exception e){}
        return polyline;
    }

    public void displayRoute(String[] polylines){
        polylinePath=new ArrayList<>();
        Marker startMarker=mMap.addMarker(new MarkerOptions().position(startLocation).title(startAddress));
        startMarker.showInfoWindow();
        Marker endMarker=mMap.addMarker(new MarkerOptions().position(endLocation).title(endAddress));
        endMarker.showInfoWindow();
        int count=polylines.length;
        for(int i=0;i<count;i++){
            PolylineOptions polylineOptions=new PolylineOptions();
            polylineOptions.color(Color.BLUE);
            polylineOptions.width(10);
            polylineOptions.addAll(PolyUtil.decode(polylines[i]));
            polylinePath.add(mMap.addPolyline(polylineOptions));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endLocation,12));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        getLocation();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
