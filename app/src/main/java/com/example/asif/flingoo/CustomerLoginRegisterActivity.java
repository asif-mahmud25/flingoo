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

public class CustomerLoginRegisterActivity extends AppCompatActivity {

    private Button CustomerLoginButton;
    private Button CustomerRegisterButton;
    private TextView CustomerRegisterLink;
    private EditText EmailCustomer;
    private EditText PasswordCustomer;

    //Loadingbar
    private ProgressDialog loadingBar;


    //Firebase

    private FirebaseAuth mAuth;
    private DatabaseReference CustomerDatabaseRef;
    private String OnlineCustomerID;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);


        CustomerLoginButton= (Button) findViewById(R.id.customer_login_btn);
        CustomerRegisterButton= (Button) findViewById(R.id.customer_register_btn);
        CustomerRegisterLink= (TextView) findViewById(R.id.register_customer_link);
        EmailCustomer = (EditText)findViewById(R.id.email_customer);
        PasswordCustomer = (EditText)findViewById(R.id.password_customer);

        //Loading bar

        loadingBar = new ProgressDialog(this);


        //Firebase

        mAuth = FirebaseAuth.getInstance();



        CustomerRegisterButton.setVisibility(View.INVISIBLE);
        CustomerRegisterButton.setEnabled(false);





        CustomerRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CustomerLoginButton.setVisibility(View.INVISIBLE);
                CustomerRegisterLink.setVisibility(View.INVISIBLE);

                CustomerRegisterButton.setVisibility(View.VISIBLE);
                CustomerRegisterButton.setEnabled(true);



            }
        });

        CustomerRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = EmailCustomer.getText().toString();
                String password = PasswordCustomer.getText().toString();

                RegisterCustomer(email, password);
            }
        });

        CustomerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = EmailCustomer.getText().toString();
                String password = PasswordCustomer.getText().toString();

                SignInCustomer(email, password);





            }
        });



    }

    private void SignInCustomer(String email, String password) {

        if(TextUtils.isEmpty(email)){

            Toast.makeText(CustomerLoginRegisterActivity.this,"Please enter Email", Toast.LENGTH_SHORT).show();


        }

        if(TextUtils.isEmpty(password)){

            Toast.makeText(CustomerLoginRegisterActivity.this,"Please enter Password", Toast.LENGTH_SHORT).show();


        }

        else{

            loadingBar.setTitle("Customer Login");
            loadingBar.setMessage("Logging in please wait....");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer login successful!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent customerIntent = new Intent(CustomerLoginRegisterActivity.this,CustomerHome.class);
                                startActivity(customerIntent);

                            }

                            else {

                                Toast.makeText(CustomerLoginRegisterActivity.this, "Incorrect email or password!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });

        }




    }

    private void RegisterCustomer(String email, String password) {

        if(TextUtils.isEmpty(email)){

            Toast.makeText(CustomerLoginRegisterActivity.this,"Please enter Email", Toast.LENGTH_SHORT).show();


        }

        if(TextUtils.isEmpty(password)){

            Toast.makeText(CustomerLoginRegisterActivity.this,"Please enter Password", Toast.LENGTH_SHORT).show();


        }

        else{

            loadingBar.setTitle("Customer Registration");
            loadingBar.setMessage("Registering please wait....");
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                OnlineCustomerID = mAuth.getCurrentUser().getUid();
                                CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child("Customers").child(OnlineCustomerID);


                                CustomerDatabaseRef.setValue(true);

                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer register successful!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent customerintent = new Intent(CustomerLoginRegisterActivity.this, CustomerHome.class);
                                startActivity(customerintent);
                            }

                            else {

                                Toast.makeText(CustomerLoginRegisterActivity.this, "Registration unsuccessful!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });

        }


    }
}
