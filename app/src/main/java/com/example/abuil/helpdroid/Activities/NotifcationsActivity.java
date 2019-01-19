package com.example.abuil.helpdroid.Activities;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import com.example.abuil.helpdroid.Helpers.NotificationService;

public class NotifcationsActivity extends Application {
    public static final String CHANNEL_1_ID = "channel1";
    @Override
    public void onCreate(){
        super.onCreate();
        // starting the service which will run in the background and detect a message to notify the user.
        startService(new Intent(this, NotificationService.class));
        createNotificationChannels();
    }
    //Build the notification channel
    private void createNotificationChannels(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1= new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");

            NotificationManager manager = getSystemService((NotificationManager.class));
            manager.createNotificationChannel(channel1);

        }

    }
}
