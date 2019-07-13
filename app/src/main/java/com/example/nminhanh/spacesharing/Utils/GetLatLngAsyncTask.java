package com.example.nminhanh.spacesharing.Utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetLatLngAsyncTask extends AsyncTask<String, Void, LatLng> {

    private static final String TAG = "GetLatLngAsyncTask";
    Context context;
    TranslateCompleteListener listener;

    public GetLatLngAsyncTask(Context context) {
        this.context = context;
        listener = (TranslateCompleteListener) context;
    }

    @Override
    protected LatLng doInBackground(String... strings) {
        LatLng currentLatLng = null;
        Geocoder mGeoCoder = new Geocoder(context);
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = mGeoCoder.getFromLocationName(strings[0], 1);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        } finally {
            if (addressList.size() > 0) {
                currentLatLng = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());
            }
        }
        if(addressList.size() > 0){
            return currentLatLng;
        }
        return null;
    }

    @Override
    protected void onPostExecute(LatLng latLng) {
        listener.onTranslateComplete(latLng);
    }

public interface TranslateCompleteListener {
    void onTranslateComplete(LatLng latLng);
}
}
