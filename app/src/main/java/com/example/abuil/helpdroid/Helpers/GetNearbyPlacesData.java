package com.example.abuil.helpdroid.Helpers;


import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
/**
 * @author Priyanka
 */

public  class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private GoogleMap mMap;
    String url;
    String urlData, Place,phone;
    private ProgressBar progressBar;



    @Override
    protected String doInBackground(Object... objects){
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];

        progressBar= (ProgressBar) objects[2];
        DownloadURL downloadURL = new DownloadURL();
        try {
            googlePlacesData = downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s){

        List<HashMap<String, String>> nearbyPlaceList;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(s);
        Log.d("nearbyplacesdata","called parse method");

        showNearbyPlaces(nearbyPlaceList);
    }

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList)
    {

        for(int i = 0; i < nearbyPlaceList.size(); i++)
        {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);
            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            String place_id =googlePlace.get("place_id");

            urlData="https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyB6RAW3vDo7_MOCz3tiPnYEy0xM1NgSj6c&placeid="+place_id;
         //   if (placeType=="hospital") {
         //   progressBar.setVisibility(View.VISIBLE);
                class ThreadJoining extends Thread {
                    @Override
                    public void run() {
                        DownloadURL downloadURL = new DownloadURL();
                        try {

                            Place = downloadURL.readUrl(urlData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                ThreadJoining t1 = new ThreadJoining();
                t1.start();
                try {
                    Log.d("join", "showNearbyPlaces: ");
                    t1.join();
                } catch (Exception e) {
                    Log.d("joine", "showNearbyPlaces: ");

                }
                DataParser parser = new DataParser();
                phone = parser.getPlaceDetails(Place);
            markerOptions.snippet(phone);

            double lat = Double.parseDouble( googlePlace.get("lat"));
            double lng = Double.parseDouble( googlePlace.get("lng"));

            LatLng latLng = new LatLng( lat, lng);
            markerOptions.position(latLng);
            markerOptions.title( placeName + " : "+ vicinity );



            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        }
      progressBar.setVisibility(View.INVISIBLE);

    }



}