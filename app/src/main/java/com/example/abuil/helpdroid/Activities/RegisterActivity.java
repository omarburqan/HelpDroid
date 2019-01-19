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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.abuil.helpdroid.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    ImageView ImgUserPhoto;
    static int PReqCode = 1 ;
    static int REQUESCODE = 1 ;
    Uri pickedImgUri ;
    private EditText userEmail,userPassword,userPAssword2,userName,userNumber,familymember1,familymember2,familymember3;
    private ProgressBar loadingProgress;
    private Button regBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference myDataBase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //init the Edittext
        userEmail = findViewById(R.id.regMail);
        userPassword = findViewById(R.id.regPassword);
        userPAssword2 = findViewById(R.id.regPassword2);
        userName = findViewById(R.id.regName);
        userNumber = findViewById(R.id.UserNumber);
        loadingProgress = findViewById(R.id.regProgressBar);
        familymember1=findViewById(R.id.familymember1);
        familymember2=findViewById(R.id.familymember2);
        familymember3=findViewById(R.id.familymember3);
        // init register button
        regBtn = findViewById(R.id.regBtn);
        //init loadingprogress
        loadingProgress.setVisibility(View.INVISIBLE);
        //init database
        myDataBase=FirebaseDatabase.getInstance().getReference();
        //init auth
        mAuth = FirebaseAuth.getInstance();

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                regBtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);
                final String email = userEmail.getText().toString();
                final String password = userPassword.getText().toString();
                final String password2 = userPAssword2.getText().toString();
                final String name = userName.getText().toString();
                final String number = userNumber.getText().toString();
                final String familyMember1=familymember1.getText().toString();
                final String familyMember2=familymember2.getText().toString();
                final String familyMember3=familymember3.getText().toString();

                if( email.isEmpty() || name.isEmpty() || password.isEmpty()  || !password.equals(password2) || number.isEmpty()) {
                    // something goes wrong : all fields must be filled
                    // we need to display an error message
                    showMessage("Please Verify all fields") ;
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                }
                else {
                    // everything is ok and all fields are filled now we can start creating user account
                    // CreateUserAccount method will try to create the user if the email is valid
                    CreateUserAccount(email,name,password,familyMember1,familyMember2,familyMember3,number,"true");
                }
            }
        });
        //init image user as a button and if clicked opengallery after take the permission
        ImgUserPhoto = findViewById(R.id.regUserPhoto) ;
        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
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
    // adding the new user details to the database.
    private void CreateUserAccount(final String email, final String name, String password, final String familyMember1, final String familyMember2, final String familyMember3,final String number,final String isOnline) {
        // this method create user account with specific email and password
        mAuth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // user account created successfully
                        showMessage("Account created");
                        // after we created user account we need to update his profile picture and name
                        updateUserInfo( name ,pickedImgUri,mAuth.getCurrentUser());
                        final String userID = mAuth.getCurrentUser().getUid();
                        myDataBase.child("Users").child(userID).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    myDataBase.child("Users").child(userID).child("email").setValue(email);
                                    myDataBase.child("Users").child(userID).child("name").setValue(name);
                                    myDataBase.child("Users").child(userID).child("number").setValue(number);
                                    myDataBase.child("Users").child(userID).child("familyMember1").setValue(familyMember1);
                                    myDataBase.child("Users").child(userID).child("familyMember2").setValue(familyMember2);
                                    myDataBase.child("Users").child(userID).child("familyMember3").setValue(familyMember3);
                                    myDataBase.child("Users").child(userID).child("isOnline").setValue(isOnline);
                                }
                                else{
                                    Toast.makeText(RegisterActivity.this, "Error to store in database", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else
                    {
                        // account creation failed
                        showMessage("account creation failed" + task.getException().getMessage());
                        regBtn.setVisibility(View.VISIBLE);
                        loadingProgress.setVisibility(View.INVISIBLE);
                    }
                }
            });
    }


    // update user photo and name
    private void updateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {
        if(pickedImgUri==null){
            pickedImgUri=Uri.parse("android.resource://com.example.abuil.helpdroid/mipmap/userphoto");
        }
        // first we need to upload user photo to firebase storage and get url
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
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();
                        currentUser.updateProfile(profleUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            // user info updated successfully
                                            showMessage("Register Complete");
                                            updateUI();
                                        }
                                    }
                                });
                    }
                });
            }
        });
    }
    // Go to HomeActivity if the register is succesful
    private void updateUI() {

        Intent homeActivity = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(homeActivity);
        finish();
    }

    // simple method to show toast message
    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }

    private void openGallery() {
        //TODO: open gallery intent and wait for user to pick an image !
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESCODE);
    }
    //permission for storage
    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(RegisterActivity.this,"Please accept for required permission",Toast.LENGTH_SHORT).show();
            }
            else
            {
                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }
        }
        else
            openGallery();
    }

    // after taking the photo saving the imgUri
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null ) {
            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            pickedImgUri = data.getData() ;
            ImgUserPhoto.setImageURI(pickedImgUri);
        }
    }
    //override back button
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent loginActivity = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(loginActivity);
            finish();
        }
        return true;
    }

}