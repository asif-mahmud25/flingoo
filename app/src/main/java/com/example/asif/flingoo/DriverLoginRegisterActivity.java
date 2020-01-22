package com.example.asif.flingoo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginRegisterActivity extends AppCompatActivity {


    private Button DriverLoginButton;
    private Button DriverRegisterButton;
    private TextView DriverRegisterLink;
    private EditText EmailDriver;
    private EditText PasswordDriver;


    //Loadingbar
    private ProgressDialog loadingBar;


    //Firebase

    private FirebaseAuth mAuth;

    private DatabaseReference ServiceProviderDatabaseRef;
    private String OnlineServiceProviderID;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);

        //Initialize

        DriverLoginButton = (Button) findViewById(R.id.driver_login_btn);
        DriverRegisterButton = (Button) findViewById(R.id.driver_register_btn);
        DriverRegisterLink = (TextView) findViewById(R.id.driver_register_link);
        EmailDriver = (EditText)findViewById(R.id.email_driver);
        PasswordDriver = (EditText)findViewById(R.id.password_driver);

        //Loading bar

        loadingBar = new ProgressDialog(this);


        //Firebase

        mAuth = FirebaseAuth.getInstance();




        DriverRegisterButton.setVisibility(View.INVISIBLE);
        DriverRegisterButton.setEnabled(false);


        DriverRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DriverLoginButton.setVisibility(View.INVISIBLE);
                DriverRegisterLink.setVisibility(View.INVISIBLE);

                DriverRegisterButton.setVisibility(View.VISIBLE);
                DriverRegisterButton.setEnabled(true);


            }

        });


        DriverRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = EmailDriver.getText().toString();
                String password = PasswordDriver.getText().toString();

                RegisterDriver(email, password);
            }
        });

        DriverLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = EmailDriver.getText().toString();
                String password = PasswordDriver.getText().toString();

                SignInDriver(email,password);




            }
        });

    }

    private void SignInDriver(String email, String password) {

        if(TextUtils.isEmpty(email)){

            Toast.makeText(DriverLoginRegisterActivity.this,"Please enter Email", Toast.LENGTH_SHORT).show();


        }

        if(TextUtils.isEmpty(password)){

            Toast.makeText(DriverLoginRegisterActivity.this,"Please enter Password", Toast.LENGTH_SHORT).show();


        }

        else{

            loadingBar.setTitle("Service Provider Login");
            loadingBar.setMessage("Logging in please wait....");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                Toast.makeText(DriverLoginRegisterActivity.this, "Service provider login successful!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent driverIntent = new Intent(DriverLoginRegisterActivity.this,ServiceProviderHome.class);
                                startActivity(driverIntent);
                            }

                            else {

                                Toast.makeText(DriverLoginRegisterActivity.this, "Incorrect email or password!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });

        }




    }


    private void RegisterDriver(String email, String password) {

        if(TextUtils.isEmpty(email)){

            Toast.makeText(DriverLoginRegisterActivity.this,"Please enter Email", Toast.LENGTH_SHORT).show();


        }

        if(TextUtils.isEmpty(password)){

            Toast.makeText(DriverLoginRegisterActivity.this,"Please enter Password", Toast.LENGTH_SHORT).show();


        }

        else{

            loadingBar.setTitle("Service Provider Registration");
            loadingBar.setMessage("Registering please wait....");
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                OnlineServiceProviderID = mAuth.getCurrentUser().getUid();
                                ServiceProviderDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child("Service Providers").child(OnlineServiceProviderID);


                                ServiceProviderDatabaseRef.setValue(true);

                                Toast.makeText(DriverLoginRegisterActivity.this, "Service provider register successful!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent driverIntent = new Intent(DriverLoginRegisterActivity.this,ServiceProviderHome.class);
                                startActivity(driverIntent);
                            }

                            else {

                                Toast.makeText(DriverLoginRegisterActivity.this, "Registration unsuccessful!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });

        }

    }
}