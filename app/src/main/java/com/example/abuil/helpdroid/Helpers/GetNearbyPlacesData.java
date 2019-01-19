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


    //execute URL
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
    //after execute the URL we send it to DataParser
    @Override
    protected void onPostExecute(String s){

        List<HashMap<String, String>> nearbyPlaceList;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(s);
        Log.d("nearbyplacesdata","called parse method");

        showNearbyPlaces(nearbyPlaceList);
    }
    //Showing nearbyplaces Data using markers
    //Display the markes on the map according to each place details.
    /* To get the contact details of each place we had to use the place_id that we received
     * from the NearByPlaces URL BY GOOGLE and then created a new thread to execute the URL
     * and get the phone number by a new JSON link which is urlData
     */

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList)
    {
        // a loop to display each place that were recieved from google.
        for(int i = 0; i < nearbyPlaceList.size(); i++)
        {
            MarkerOptions markerOptions = new MarkerOptions(); // get the places details from the hash map
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);
            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            String place_id =googlePlace.get("place_id");
            // A SPECIAL URL TO GET THE CONTACT DETAILS FOR EVERY PLACE USING THE place_id
            urlData="https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyB6RAW3vDo7_MOCz3tiPnYEy0xM1NgSj6c&placeid="+place_id;
            /* a new thread to the execute the urlData and get the contact details
            * so we join this thread and wait until it finished
            */
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
                t1.join();
            } catch (Exception e) {
                Log.d("Thraedjoining", "showNearbyPlaces:enable to join The joinThread ");
            }
            DataParser parser = new DataParser(); //parse the executed JSON to get the phone number
            phone = parser.getPlaceDetails(Place);
            // finally place the marker on the map and its Details.
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