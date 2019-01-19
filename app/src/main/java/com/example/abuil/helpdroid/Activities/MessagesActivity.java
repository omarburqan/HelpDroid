package com.example.abuil.helpdroid.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ListView;

import com.example.abuil.helpdroid.Adapters.messageAdapter;
import com.example.abuil.helpdroid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    List<String> messeges;
    List<String> name;
    ListView messagesListView;
    messageAdapter mesAdapter;
    private DatabaseReference myDataBase;
    NotificationManagerCompat notificationManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        messagesListView=findViewById(R.id.MessageList);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        myDataBase = FirebaseDatabase.getInstance().getReference();
        messeges=new ArrayList<>();
        name=new ArrayList<>();
        notificationManager = NotificationManagerCompat.from(this);
        // Displaying the messages of the user according to the dataplace.
        myDataBase.child("Users").child(currentUser.getUid()).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()  ) {
                    name.add(0,data.getKey().toString());
                    myDataBase.child("Users").child(currentUser.getUid()).child("Messages").child(data.getKey().toString()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot data: dataSnapshot.getChildren()  ) {
                                messeges.add(0,data.getValue().toString());
                                mesAdapter=new messageAdapter(MessagesActivity.this,R.layout.messagerow,messeges,name);
                                messagesListView.setAdapter(mesAdapter);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


// override the back button pressed
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent HomeActivity = new Intent(getApplicationContext(), com.example.abuil.helpdroid.Activities.HomeActivity.class);
            startActivity(HomeActivity);
            finish();
        }
        return true;
    }
}
