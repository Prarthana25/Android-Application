package com.example.prarthana.entertainmentapp;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.app.Application;

public class VolleyApplication extends Application {

    //Declare a private  RequestQueue variable
    private  RequestQueue requestQueue;

    private static VolleyApplication mInstance;

    public void onCreate()
    {
        super.onCreate();
        mInstance=this;

    }

    public static synchronized VolleyApplication getInstance()
    {
        return mInstance;
    }

    /**
     Create a getRequestQueue() method to return the instance of
     RequestQueue.This kind of implementation ensures that
     the variable is instantiated only once and the same
     instance is used throughout the application
     **/
    public RequestQueue getRequestQueue()
    {
        if (requestQueue==null)
            requestQueue= Volley.newRequestQueue(getApplicationContext());

        return requestQueue;
    }

    /*
         Method to add the Request to the the single
    instance of RequestQueue created above.Setting a tag to every
    request helps in grouping them. Tags act as identifier
    for requests and can be used while cancelling them
    */
    public void addToRequestQueue(Request request, String tag)
    {
        request.setTag(tag);
        getRequestQueue().add(request);

    }

    /**
     Cancel all the requests matching with the given tag
     **/

    public void cancelAllRequests(String tag)
    {
        getRequestQueue().cancelAll(tag);
    }
}
