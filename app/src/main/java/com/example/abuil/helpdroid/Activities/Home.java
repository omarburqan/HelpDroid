package com.example.abuil.helpdroid.Activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.abuil.helpdroid.R;
import com.example.abuil.helpdroid.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.example.abuil.helpdroid.Activities.NotifcationsActivity.CHANNEL_1_ID;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private static final String TAG ="home>>" ;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private Button sendSMS;
    private DatabaseReference myDataBase;
    Notification notification;
    String familymember1;
    String familymember2;
    String familymember3;
    String username, email;
    String lat = "", lon = "";
    LocationManager locationManager;
    String locationlink;
    String currentUserID;
    private int MessagesIndex=0;
    Intent MessagesActivity;
    private SpeechRecognizer mySpeech;
    NotificationManagerCompat notificationManager;
    boolean enter = false;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;

    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL =  100;  /* 10 secs */
    private long FASTEST_INTERVAL = 1000; /* 2 sec */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationManager = NotificationManagerCompat.from(this);
        setContentView(R.layout.activity_home2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MessagesActivity = new Intent(getApplicationContext(),com.example.abuil.helpdroid.Activities.MessagesActivity.class);
        sendSMS = findViewById(R.id.panicButton);
        myDataBase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid().toString();
        currentUser = mAuth.getCurrentUser();
        final MediaPlayer mp = MediaPlayer.create(Home.this, R.raw.danger_alarm);
        bulidGoogleApiClient();

        mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        checkLocation(); //check whether location service is enable or not in your  phone


        myDataBase.child("Users").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                username = dataSnapshot.child("name").getValue().toString();
                email = dataSnapshot.child("email").getValue().toString();
                familymember1 = dataSnapshot.child("familyMember1").getValue().toString();
                familymember2 = dataSnapshot.child("familyMember2").getValue().toString();
                familymember3 = dataSnapshot.child("familyMember3").getValue().toString();

                sendSMS.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (checkPermission()) {

                            if(!checkLocation()){
                                Toast.makeText(Home.this, "Location Services is not enabled", Toast.LENGTH_LONG).show();
                                return;
                            }
                            locationlink="http://www.google.com/maps/place/"+lat+","+lon;


                            //Send the SMS//
                            Toast.makeText(Home.this, "sending", Toast.LENGTH_SHORT).show();
                            if(!familymember1.isEmpty() ){
                                CheckFamilyMemberIsActive(familymember1);
                             }
                            if(!familymember2.isEmpty()) {
                                CheckFamilyMemberIsActive(familymember2);
                            }
                            if(!familymember3.isEmpty()) {
                                CheckFamilyMemberIsActive(familymember3);
                            }
                          //  mp.start();
                        } else {
                            Toast.makeText(Home.this, "Permission denied", Toast.LENGTH_LONG).show();
                        }
                    }

                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // ini


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(MessagesActivity);
                finish();
            }
        });
        FloatingActionButton mute = findViewById(R.id.mute);
        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp.pause();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        updateNavHeader();

        myDataBase.child("Users").child(currentUser.getUid()).child("Messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Intent notificationIntent = new Intent(Home.this, MessagesActivity.class);

                PendingIntent pendingIntent = PendingIntent.getActivity(Home.this, 0,
                        notificationIntent, 0);
                notification = new NotificationCompat.Builder(Home.this, CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(dataSnapshot.getKey().toString())
                        .setContentText("Please Help Me , Emergency situation")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                         .setContentIntent(pendingIntent)
                        .build();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Intent notificationIntent = new Intent(Home.this, MessagesActivity.class);

                PendingIntent pendingIntent = PendingIntent.getActivity(Home.this, 0,
                        notificationIntent, 0);

                notification = new NotificationCompat.Builder(Home.this, CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(dataSnapshot.getKey().toString())
                        .setContentText("Please Help Me , Emergency situation")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setContentIntent(pendingIntent)
                        .build();

                notificationManager.notify(1, notification);


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                enter=false;
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        myDataBase.child("Users").child(currentUser.getUid()).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(enter )
                    notificationManager.notify(1, notification);
                else {
                    enter = true;

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    private void CheckFamilyMemberIsActive(final String Number){
        final SmsManager smsManager = SmsManager.getDefault();

        myDataBase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final boolean[] messageSent = {false};
                for (final DataSnapshot data:dataSnapshot.getChildren()) {
                    if(Number.equals(data.child("number").getValue().toString())){ // number is relavent for
                       if(data.child("isOnline").getValue().toString().equals("true"))     {
                           myDataBase.child("Users").child(data.getKey()).child("Messages").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                   MessagesIndex=0;
                                   myDataBase.child("Users").child(data.getKey()).child("Messages").child(username).child(MessagesIndex+"").setValue(locationlink.toString());


                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError databaseError) {

                               }
                           });
                           messageSent[0]=true;
                       }else if(data.child("isOnline").getValue().toString().equals("false")){
                           String mess = "I need Your Assistance at this location,Please help me \n";
                           mess +=locationlink;
                           smsManager.sendTextMessage(Number, null, mess
                                   , null, null);
                           messageSent[0]=true;
                       }
                    }
                }
                if(!messageSent[0]){
                    String mess = "I need Your Assistance this location,Please. \n";
                    mess +=locationlink;
                    smsManager.sendTextMessage(Number, null, mess
                                        , null, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Profile) {
            Intent profileActivity = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(profileActivity);
            finish();
        } else if (id == R.id.nav_Maps) {
            Intent mapsActivity = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(mapsActivity);
            finish();
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            myDataBase.child("Users").child(currentUserID).child("isOnline").setValue("false");
            Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginActivity);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void updateNavHeader() {

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        final TextView navUsername = headerView.findViewById(R.id.nav_username);
        final TextView navUserMail = headerView.findViewById(R.id.nav_user_mail);
        final ImageView navUserPhot = headerView.findViewById(R.id.nav_user_photo);
        myDataBase.child("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                username = dataSnapshot.child("name").getValue().toString();
                email = dataSnapshot.child("email").getValue().toString();
                navUserMail.setText(email);
                navUsername.setText(username);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        // now we will use Glide to load user image
        // first we need to import the library

        Glide.with(this).load(currentUser.getPhotoUrl()).into(navUserPhot);


    }

    private boolean checkPermission() {

        ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION},1);
        int result = ContextCompat.checkSelfPermission(Home.this, Manifest.permission.SEND_SMS);

        int result2 = ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_PHONE_STATE);
        boolean result3 = checkLocationPermission();
        if (result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED && result3==true ) {

            return true;
        } else {
            return false;
        }
    }
    public boolean checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {


                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d(TAG, "onConnected:  connect sucssefull");

        startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLocation == null){
            startLocationUpdates();
        }
        if (mLocation != null) {
                lat=String.valueOf(mLocation.getLatitude());
                lon=String.valueOf(mLocation.getLongitude());

        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());

    }

    @Override
    public void onLocationChanged(Location location) {

        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        lat=String.valueOf(location.getLatitude());
       lon=String.valueOf(location.getLongitude());


        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }
    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            Log.d(TAG, "checkLocation: ");
        return isLocationEnabled();
    }
    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    protected synchronized void bulidGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();

    }
}