package com.example.abuil.helpdroid.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.abuil.helpdroid.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener ,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener{

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastlocation;
    private Marker currentLocationmMarker;
    //public static final int REQUEST_LOCATION_CODE = 99;
    int PROXIMITY_RADIUS = 5000;
    double latitude,longitude;
    double end_latitude, end_longitude;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
   public  ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();

        }

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        }
        else {
            Log.d("onCreate","Google Play Services available.");
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        progressBar=findViewById(R.id.mapsProgressBar);
        progressBar.setVisibility(View.INVISIBLE);



    }
    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (client== null) {
                            bulidGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                bulidGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            bulidGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);


    }
    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();

    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent homeActivity = new Intent(getApplicationContext(),Home.class);
            startActivity(homeActivity);
            finish();
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location==null)
            return;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        lastlocation = location;
        if(currentLocationmMarker != null)
        {
            currentLocationmMarker.remove();

        }
        LatLng latLng = new LatLng(location.getLatitude() , location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if(client != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        }
    }

    public boolean checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    public void onClick(View v)
    {
        Object dataTransfer[] = new Object[3];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        String url="";
        switch(v.getId())
        {
            case R.id.B_hopistals:
                ShowNearbyPlaces(url,dataTransfer,getNearbyPlacesData,"hospital");
               /* end_longitude=0.0;
                end_latitude=0.0;
                mMap.clear();
                onLocationChanged(lastlocation);
                String hospital = "hospital";
                url = getUrl(latitude, longitude, hospital);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2]=progressBar;
                progressBar.setVisibility(View.VISIBLE);
                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing Nearby Hospitals", Toast.LENGTH_SHORT).show();*/
                break;
            case R.id.B_policeStations:
                ShowNearbyPlaces(url,dataTransfer,getNearbyPlacesData,"police");

                /*end_longitude=0.0;
                end_latitude=0.0;
                mMap.clear();
                onLocationChanged(lastlocation);
                String policeStation = "police";
                url = getUrl(latitude, longitude, policeStation);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2]=progressBar;
                progressBar.setVisibility(View.VISIBLE);
                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, " Showing Nearby PolceStations" , Toast.LENGTH_SHORT).show();*/
                break;
            case R.id.B_fireStations:
                ShowNearbyPlaces(url,dataTransfer,getNearbyPlacesData,"fire_station");

                /*end_longitude=0.0;
                end_latitude=0.0;
                mMap.clear();
                onLocationChanged(lastlocation);
                String fireStations = "fire_station";
                url = getUrl(latitude, longitude, fireStations);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2]=progressBar;
                progressBar.setVisibility(View.VISIBLE);
                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing Nearby FireStations" , Toast.LENGTH_SHORT).show();*/
                break;
            case R.id.B_to:
                if(end_latitude==0.0 && end_longitude==0.0){
                    Toast.makeText(MapsActivity.this, "Please chose a marker" , Toast.LENGTH_SHORT).show();
                    break;
                }
                mMap.clear();

                dataTransfer = new Object[3];
                url = getDirectionsUrl();
                GetDirectionsData getDirectionsData = new GetDirectionsData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = new LatLng(end_latitude, end_longitude);
                getDirectionsData.execute(dataTransfer);
                break;
        }
    }

    private void ShowNearbyPlaces(String url,Object[] dataTransfer,GetNearbyPlacesData getNearbyPlacesData,String place) {

        end_longitude=0.0;
        end_latitude=0.0;
        mMap.clear();
        onLocationChanged(lastlocation);
        String hospital = place;
        url = getUrl(latitude, longitude, hospital);
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;
        dataTransfer[2]=progressBar;
        progressBar.setVisibility(View.VISIBLE);
        getNearbyPlacesData.execute(dataTransfer);
        Toast.makeText(MapsActivity.this, "Showing Nearby " + place, Toast.LENGTH_SHORT).show();
    }

    private String getDirectionsUrl()
    {
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+latitude+","+longitude);
        googleDirectionsUrl.append("&destination="+end_latitude+","+end_longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyB6RAW3vDo7_MOCz3tiPnYEy0xM1NgSj6c");

        return googleDirectionsUrl.toString();
    }
    private String getUrl(double latitude , double longitude , String nearbyPlace)
    {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+"AIzaSyB6RAW3vDo7_MOCz3tiPnYEy0xM1NgSj6c");
        Log.d("near", "getUrl: " + googlePlaceUrl);
        return googlePlaceUrl.toString();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        end_latitude=marker.getPosition().latitude;
        end_longitude=marker.getPosition().longitude;
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
}
