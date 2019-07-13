package com.example.nminhanh.spacesharing.Utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchAddressTask extends AsyncTask<Location, Void, String> {

    private Context mContext;
    private onGetAddressCompletedListener mListenter;
    private Location currentLocation;

    public FetchAddressTask(Context mContext) {
        this.mContext = mContext;
        mListenter = (onGetAddressCompletedListener) mContext;
    }

    @Override
    protected String doInBackground(Location... locations) {
        Geocoder mGeoCoder = new Geocoder(mContext, Locale.getDefault());
        currentLocation = locations[0];

        List<Address> addresses = null;
        String result = "";

        try {
            addresses = mGeoCoder.getFromLocation(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude(),
                    1);
            if (addresses == null || addresses.size() == 0) {
                result = "Không xác định được vị trí hiện tại của bạn";
            } else {
                Address mCurrentAddress = addresses.get(0);
                ArrayList<String> mCurrentAddressParts = new ArrayList<>();

                for (int i = 0; i <= mCurrentAddress.getMaxAddressLineIndex(); i++) {
                    mCurrentAddressParts.add(mCurrentAddress.getAddressLine(i));
                }

                result = TextUtils.join("\n", mCurrentAddressParts);
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = "Dịch vụ hiện tại không khả dụng";
        } catch (IllegalArgumentException e) {
            result = "kinh độ/ vĩ độ không hợp lệ";
        }
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        mListenter.onGetAddressCompleted(s, currentLocation);
        super.onPostExecute(s);
    }

    public interface onGetAddressCompletedListener {
        void onGetAddressCompleted(String result, Location location);
    }
}
