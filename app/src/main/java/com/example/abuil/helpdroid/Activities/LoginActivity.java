package com.example.abuil.helpdroid.Activities;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.abuil.helpdroid.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class LoginActivity extends AppCompatActivity {


    private EditText userMail,userPassword;
    private Button btnLogin;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth;
    private Intent HomeActivity;
    private Button btnReg;
    private FirebaseUser currentUser;
    private DatabaseReference myDataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        HomeActivity = new Intent(this, com.example.abuil.helpdroid.Activities.HomeActivity.class);
        myDataBase = FirebaseDatabase.getInstance().getReference();
        userMail = findViewById(R.id.login_mail);
        userPassword = findViewById(R.id.login_password);
        loginProgress = findViewById(R.id.login_progress);
        mAuth = FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        loginProgress.setVisibility(View.INVISIBLE);
        if(currentUser!=null){ // check if the user had already logged when he opens the application

            updateUI();
        }
        // init the register button
        btnReg=findViewById(R.id.rgsButton);
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerActivity = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(registerActivity);
                finish();

            }
        });
        // init the login button
        btnLogin = findViewById(R.id.loginBtn);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginProgress.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.INVISIBLE);

                final String mail = userMail.getText().toString();
                final String password = userPassword.getText().toString();

                if (mail.isEmpty() || password.isEmpty()) {
                    showMessage("Please Verify All Field");
                    btnLogin.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.INVISIBLE);
                }
                else
                {
                    signIn(mail,password);
                }
            }
        });
    }
    // Sign in by using firebase-database Authentication
    private void signIn(String mail, String password) {
        mAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loginProgress.setVisibility(View.INVISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                    myDataBase.child("Users").child(mAuth.getCurrentUser().getUid()).child("isOnline").setValue("true");
                    updateUI();
                }
                else {
                    showMessage(task.getException().getMessage());
                    btnLogin.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    // updating userinterface by starting a Home activity
    private void updateUI() { // Start home Activity
        startActivity(HomeActivity);
        finish();
    }

    private void showMessage(String text) {
        // a private method to make soem toasts
        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
    }

    //user is already connected  so we need to redirect him to home page
    //extra
    // making sure so we can all the sitution to check if user has already logged in
    //so we can make sure there is no bug
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            updateUI();
        }
    }
    //user is already connected  so we need to redirect him to home page
   //Importatnt not extra
    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            updateUI();
        }
    }
}