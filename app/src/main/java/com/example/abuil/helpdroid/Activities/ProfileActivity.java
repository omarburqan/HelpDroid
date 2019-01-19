package com.example.abuil.helpdroid.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.abuil.helpdroid.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileActivity extends AppCompatActivity {
    private EditText familymember1,familymember2,familymember3,userNumber;
    String userName;
    private Button update;
    private ProgressBar upProgressbar;
    private DatabaseReference myDataBase;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser ;
    static int PReqCode = 1 ;
    static int REQUESCODE = 1 ;
    private ImageView userPhoto;
    Uri pickedImgUri ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        userName="";
        // init the edit text
        userNumber=findViewById(R.id.edit_yournumber);
        familymember1=findViewById(R.id.edit_familymember1);
        familymember2=findViewById(R.id.edit_familymember2);
        familymember3=findViewById(R.id.edit_familymember3);
        userPhoto=findViewById(R.id.profilephoto);
        update=findViewById(R.id.save_changes);
        //progress-bar when updating
        upProgressbar=findViewById(R.id.upProgressBar);
        upProgressbar.setVisibility(View.INVISIBLE);
        myDataBase= FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Glide.with(this).load(currentUser.getPhotoUrl()).into(userPhoto);
        //filling the edit texts according the user details which is stored in the database.
        myDataBase.child("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName = dataSnapshot.child("name").getValue().toString();
                userNumber.setText(dataSnapshot.child("number").getValue().toString());
                familymember1.setText(dataSnapshot.child("familyMember1").getValue().toString());
                familymember2.setText(dataSnapshot.child("familyMember2").getValue().toString());
                familymember3.setText(dataSnapshot.child("familyMember3").getValue().toString());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        //update button for updating the values for user details.
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userID = mAuth.getCurrentUser().getUid();
                if(TextUtils.isEmpty(userNumber.getText().toString())){
                    Toast.makeText(ProfileActivity.this, "phone number required", Toast.LENGTH_SHORT).show();
                }else {
                    myDataBase.child("Users").child(userID).child("number").setValue(userNumber.getText().toString());
                    myDataBase.child("Users").child(userID).child("familyMember1").setValue(familymember1.getText().toString());
                    myDataBase.child("Users").child(userID).child("familyMember2").setValue(familymember2.getText().toString());
                    myDataBase.child("Users").child(userID).child("familyMember3").setValue(familymember3.getText().toString());
                    if(pickedImgUri==null){
                        Toast.makeText(ProfileActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                        return; // so not to change the picture.
                    }
                    StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
                    final StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());
                    imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // image uploaded succesfully
                            // now we can get our image url
                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // uri contain user image url
                                    UserProfileChangeRequest profleUpdate = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(userName)
                                            .setPhotoUri(uri)
                                            .build();
                                    currentUser.updateProfile(profleUpdate);
                                }
                            });
                        }
                    });
                    Toast.makeText(ProfileActivity.this, "Updated With A New Picture", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // when pressing user photo checkpermission and open gallery.
        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 22) {
                    checkAndRequestForPermission();
                }
                else
                {
                    openGallery();
                }
            }
        });
    }

    private void openGallery() {
        //TODO: open gallery intent and wait for user to pick an image !
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null ) {
            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            pickedImgUri = data.getData() ;
            userPhoto.setImageURI(pickedImgUri);
        }
    }
    // Storage permission
    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ProfileActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PReqCode);
        }
        else
            openGallery();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Changes 'back' button action
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            //Include the code here
            Intent homeActivity = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(homeActivity);
            finish();
        }
        return true;
    }
}
