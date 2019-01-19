package com.example.abuil.helpdroid.Activities;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.abuil.helpdroid.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.abuil.helpdroid.Activities.NotifcationsActivity.CHANNEL_1_ID;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private static final String TAG ="home>>" ;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private Button panic_button;
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
    private long UPDATE_INTERVAL =  100;  /* 1secs */
    private long FASTEST_INTERVAL = 1000; /* 10 sec */
    private MediaPlayer mp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        bulidGoogleApiClient();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        notificationManager = NotificationManagerCompat.from(this);
        MessagesActivity = new Intent(getApplicationContext(),com.example.abuil.helpdroid.Activities.MessagesActivity.class);
        panic_button = findViewById(R.id.panicButton);
        myDataBase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid().toString();
        currentUser = mAuth.getCurrentUser();
        mp = MediaPlayer.create(HomeActivity.this, R.raw.danger_alarm);
        mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        checkLocation(); //check whether location service is enable or not in your  phone
        /*get user details from the database*/
        myDataBase.child("Users").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                username = dataSnapshot.child("name").getValue().toString();
                email = dataSnapshot.child("email").getValue().toString();
                familymember1 = dataSnapshot.child("familyMember1").getValue().toString();
                familymember2 = dataSnapshot.child("familyMember2").getValue().toString();
                familymember3 = dataSnapshot.child("familyMember3").getValue().toString();
                panic_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (checkPermission()) {
                            if(!checkLocation()){
                                Toast.makeText(HomeActivity.this, "Location Services is not enabled", Toast.LENGTH_LONG).show();
                                return;
                            }
                            /*Check if the location is already taken*/
                            if(lat.isEmpty() || lon.isEmpty()){
                                Toast.makeText(HomeActivity.this, "Location not available yet,try again", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // get the locataion for the user and build it into the location link.
                            locationlink="http://www.google.com/maps/place/"+lat+","+lon;


                            //Send the SMS//
                            Toast.makeText(HomeActivity.this, "sending", Toast.LENGTH_SHORT).show();
                            if(!familymember1.isEmpty() ){
                                CheckFamilyMemberIsActive(familymember1);
                             }
                            if(!familymember2.isEmpty()) {
                                CheckFamilyMemberIsActive(familymember2);
                            }
                            if(!familymember3.isEmpty()) {
                                CheckFamilyMemberIsActive(familymember3);
                            }
                           mp.start();
                        } else {
                            Toast.makeText(HomeActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
                        }
                    }

                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        // init the messages button
        FloatingActionButton MessagesButton = findViewById(R.id.fab);
        MessagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(MessagesActivity);
                finish();
            }
        });
        // init the mute the alaram sound
        FloatingActionButton mute = findViewById(R.id.mute);
        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp.pause();
            }
        });
        // open the navigation menu
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        updateNavHeader();
        // keep checking if this  user has recieved a new message and update the messages with notification.
        myDataBase.child("Users").child(currentUser.getUid()).child("Messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Intent notificationIntent = new Intent(HomeActivity.this, MessagesActivity.class);

                PendingIntent pendingIntent = PendingIntent.getActivity(HomeActivity.this, 0,
                        notificationIntent, 0);
                notification = new NotificationCompat.Builder(HomeActivity.this, CHANNEL_1_ID)
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

                Intent notificationIntent = new Intent(HomeActivity.this, MessagesActivity.class);

                PendingIntent pendingIntent = PendingIntent.getActivity(HomeActivity.this, 0,
                        notificationIntent, 0);

                notification = new NotificationCompat.Builder(HomeActivity.this, CHANNEL_1_ID)
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


        // we did this to prevent the app from pop up the notification every time it starts
        myDataBase.child("Users").child(currentUser.getUid()).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(enter)
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
    //checking if family member has account and if he is online or not to send normalSMS or in app built messages
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
                    String mess = "I need Your Assistance at this location,Please help me \n";
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
    // override the backbutton pressing.
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.stop();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    // Listener for navigation menu buttons(items)
    // each button will start a new different activity
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
        // Displaying the user details such as name ,email and photo in the navigation menu
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
        // checking SMS,and Phone,
        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION},1);
        int result = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.SEND_SMS);

        int result2 = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_PHONE_STATE);
        boolean result3 = checkLocationPermission();
        if (result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED && result3==true ) {

            return true;
        } else {
            return false;
        }
    }
    // checking the location permission
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
            bulidGoogleApiClient();
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
    // first we check the location if not we try one more time and after that if not located we toast a message.
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
    //update the current location every 1 sec.
    @Override
    public void onLocationChanged(Location location) {

        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        lat=String.valueOf(location.getLatitude());
       lon=String.valueOf(location.getLongitude());


        // create a LatLng Object for use with maps
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
    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            Log.d(TAG, "checkLocation: Location is not enabled");
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