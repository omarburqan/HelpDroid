package com.example.abuil.helpdroid.Helpers;
import java.io.IOException;
import java.util.HashMap;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException


        ;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Priyanka
 */

public class DataParser {
    private HashMap<String, String> getPlace(JSONObject googlePlaceJson) {
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String placeName = "--NA--";
        String vicinity = "--NA--";
        String latitude = "";
        String longitude = "";
        String reference = "";
        String placeID = "";


        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
            }
            if (!googlePlaceJson.isNull("vicinity")) {
                vicinity = googlePlaceJson.getString("vicinity");
            }
            if (!googlePlaceJson.isNull("place_id")) {
                placeID = googlePlaceJson.getString("place_id");
            }
            // String place_id =placeID;

            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = googlePlaceJson.getString("reference");
            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);
            googlePlaceMap.put("place_id", placeID);
            //  googlePlaceMap.put("phone", phoneNum);
            Log.d("my_id", "getPlace: " + placeID);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceMap;

    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {
        int count = jsonArray.length();
        List<HashMap<String, String>> placelist = new ArrayList<>();
        HashMap<String, String> placeMap = null;

        for (int i = 0; i < count; i++) {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placelist.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placelist;
    }

    public List<HashMap<String, String>> parse(String jsonData) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;


        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }

    public String[] parseDirections(String jsonData) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
            Log.d("jason array", "parseDirections: " + jsonArray);

        } catch (JSONException e) {
            Log.d("jason", "parseDirections: " + jsonArray);
            e.printStackTrace();
        }
        Log.d("taag", "parseDirections: " + jsonArray);
        return getPaths(jsonArray);
    }

    public String[] getPaths(JSONArray googleStepsJson) {

        int count=0;
      try {
          count= googleStepsJson.length();
      }
      catch (Exception e) {
          Log.d(">>", "getPaths: ");
      }
        Log.d("count", "getPaths: " + count);
        String[] polylines = new String[count];

        for (int i = 0; i < count; i++) {
            try {
                polylines[i] = getPath(googleStepsJson.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return polylines;
    }

    public String getPath(JSONObject googlePathJson) {
        String polyline = "";
        try {
            polyline = googlePathJson.getJSONObject("polyline").getString("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polyline;
    }

    public String getPlaceDetails(String jsonData) {
        String phone_number = "";
        JSONArray arr = null;
        if (!TextUtils.isEmpty(jsonData)) {

                if(jsonData.indexOf("international_phone_number") != -1) {

                    phone_number = jsonData.substring(jsonData.indexOf("international_phone_number"));
                    phone_number = phone_number.substring(phone_number.indexOf(":") + 1, phone_number.indexOf(","));
                    phone_number = phone_number.substring(phone_number.indexOf("\"") + 1, phone_number.length() - 1);
                }
            Log.d("phone1sgsda11", "getPlaceDetails: " + phone_number);
        }
        return phone_number;

    }
}