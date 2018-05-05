package com.example.prarthana.entertainmentapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.util.ArrayList;

public class PhotoFragment extends Fragment {
    protected GeoDataClient mGeoDataClient;
    protected ArrayList<PhotoList> arrayList;
    protected ListView listView;
    protected String place_id;
    protected RelativeLayout photoResult,photoNoResult;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photo_fragment, container, false);
        photoResult=(RelativeLayout)view.findViewById(R.id.photoResult);
        photoNoResult=(RelativeLayout)view.findViewById(R.id.photoNoResult);
        arrayList=new ArrayList<>();

        mGeoDataClient = Places.getGeoDataClient(getActivity());
        listView=view.findViewById(R.id.photoListView);

        Gson gson=new Gson();
        PlacesResult placesResult=gson.fromJson(getArguments().getString("currentPlace"),PlacesResult.class);
        place_id=placesResult.place_id;

        mGeoDataClient.getPlacePhotos(place_id).addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                PlacePhotoMetadataResponse photos = task.getResult();
                final PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                if(photoMetadataBuffer.getCount()==0){
                    photoResult.setVisibility(View.GONE);
                    photoNoResult.setVisibility(View.VISIBLE);
                    return;
                }
                for(int i=0;i<photoMetadataBuffer.getCount();i++) {
                    PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(i);
                    Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata)
                            .addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                                @Override
                                public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                                    PlacePhotoResponse photo = task.getResult();
                                    Bitmap bitmap = photo.getBitmap();
                                    PhotoList photoList = new PhotoList(bitmap);
                                    arrayList.add(photoList);
                                    if(arrayList.size()==photoMetadataBuffer.getCount()) {
                                        PhotoListAdapter photoListAdapter = new PhotoListAdapter(getContext(), R.layout.photo_fragment, arrayList);
                                        listView.setAdapter(photoListAdapter);
                                    }
                                }
                            });
                }
            }
        });
        return view;
    }
}
