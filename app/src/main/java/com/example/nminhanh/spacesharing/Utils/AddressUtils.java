package com.example.nminhanh.spacesharing.Utils;

import android.content.Context;

import com.example.nminhanh.spacesharing.Model.District;
import com.example.nminhanh.spacesharing.Model.Ward;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class AddressUtils {

    private Context context;

    public AddressUtils(Context context) {
        this.context = context;
    }

    public String getJSONfromAssets(String path) {
        String json = "";
        if (!path.contains("-1")) {
            try {
                InputStream is = context.getAssets().open(path);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    public ArrayList<District> getDistrictList(String src) {
        ArrayList<District> districtList = new ArrayList<>();
        districtList.add(new District("-1", "Hãy chọn quận/huyện..."));
        if (!src.isEmpty()) {
            try {
                JSONObject root = new JSONObject(src);
                Iterator<String> iterator = root.keys();

                while (iterator.hasNext()) {
                    String currentDistId = iterator.next();
                    JSONObject currentDist = root.getJSONObject(currentDistId);
                    String name = currentDist.getString("name_with_type");
                    String parentId = currentDist.getString("parent_code");
                    districtList.add(new District(currentDistId, name, parentId));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return districtList;
    }

    public ArrayList<String> getDistrictNameList(ArrayList<District> list) {
        ArrayList<String> nameList = new ArrayList<>();
        for (District d : list) {
            nameList.add(d.getName());
        }
        return nameList;
    }

    public String getDistrictName(String cityId, String districtId) {
        String JSONdistrictList = getJSONfromAssets("quan-huyen/" + cityId + ".json");
        ArrayList<District> districtList = getDistrictList(JSONdistrictList);

        for (District d : districtList) {
            if (d.getId().equalsIgnoreCase(districtId)) {
                return d.getName();
            }
        }
        return null;
    }

    public ArrayList<Ward> getWardList(String src) {
        ArrayList<Ward> wardList = new ArrayList<>();
        wardList.add(new Ward("-1", "Hãy chọn phường/xã..."));
        if (!src.isEmpty()) {
            try {
                JSONObject root = new JSONObject(src);
                Iterator<String> iterator = root.keys();

                while (iterator.hasNext()) {
                    String currentWardId = iterator.next();
                    JSONObject currentWard = root.getJSONObject(currentWardId);
                    String name = currentWard.getString("name_with_type");
                    String parentId = currentWard.getString("parent_code");
                    wardList.add(new Ward(currentWardId, name, parentId));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return wardList;
    }

    public ArrayList<String> getWardNameList(ArrayList<Ward> list) {
        ArrayList<String> nameList = new ArrayList<>();
        for (Ward d : list) {
            nameList.add(d.getName());
        }
        return nameList;
    }

    public String getWardName(String districtId, String wardId) {
        String JSONWardList = getJSONfromAssets("xa-phuong/" + districtId + ".json");
        ArrayList<Ward> wardList = getWardList(JSONWardList);

        for (Ward w : wardList) {
            if (w.getId().equalsIgnoreCase(wardId)) {
                return w.getName();
            }
        }
        return null;
    }
}
