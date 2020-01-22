package com.example.asif.flingoo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServiceProviderSettings extends AppCompatActivity {

    private EditText NameField, PhoneField;
    private Button Back,Confirm;

    private FirebaseAuth mAuth;
    private DatabaseReference ServiceProviderDatabase;
    private String userID;
    private String cname;
    private String cphone;
    private String service;
    private RadioGroup radioGroup;
    private ImageView profileImage;
    private Uri resultUri;
    private String profileImageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_provider_settings);
        NameField = (EditText)findViewById(R.id.name);
        PhoneField = (EditText)findViewById(R.id.phone);

        profileImage = (ImageView) findViewById(R.id.cprofileImage);
        Confirm = (Button) findViewById(R.id.confirm_btn);
        Back = (Button) findViewById(R.id.back_btn);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        ServiceProviderDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Service Providers").child(userID);
        getUserInfo();
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });
        Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }
    private void getUserInfo(){
        ServiceProviderDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!= null){
                        cname = map.get("name").toString();
                        NameField.setText(cname);
                    }
                    if(map.get("phone")!= null){
                        cphone = map.get("phone").toString();
                        PhoneField.setText(cphone);
                    }
                    if(map.get("service")!= null){
                        service = map.get("service").toString();
                        switch(service){
                            case "Electrician":
                                radioGroup.check(R.id.Electrician);
                                break;
                            case "Automechanic":
                                radioGroup.check(R.id.Automechanic);
                                break;
                            case "Plumber":
                                radioGroup.check(R.id.Plumber);
                                break;
                        }
                    }

                    if(map.get("profileImageUrl")!= null){
                        profileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(profileImageUrl).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInformation() {
        cname = NameField.getText().toString();
        cphone = PhoneField.getText().toString();

        int selectId = radioGroup.getCheckedRadioButtonId();
        final RadioButton radioButton = (RadioButton) findViewById(selectId);

        if(radioButton.getText() == null){
            return;
        }

        service = radioButton.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name",cname);
        userInfo.put("phone",cphone);
        userInfo.put("service",service);


        ServiceProviderDatabase.updateChildren(userInfo);
        if(resultUri != null){
            final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resultUri);
            }catch(IOException e){
                e.printStackTrace(); }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,20, baos);
            byte[] data  = baos.toByteArray();
            UploadTask uploadTask = filepath.putBytes(data);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map newImage = new HashMap();
                            newImage.put("profileImageUrl", uri.toString());
                            ServiceProviderDatabase.updateChildren(newImage);

                            finish();
                            return;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            finish();
                            return;
                        }
                    });
                }
            });

//            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                }
//            });
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            profileImage.setImageURI(resultUri);
        }
    }
}

