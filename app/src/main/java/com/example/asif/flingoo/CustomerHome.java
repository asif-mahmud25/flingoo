package com.example.asif.flingoo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.places.Place;
//import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
//import com.google.android.gms.location.places.ui.PlaceSelectionListener;
//import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{


    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;

    private Button ServiceRequestButton,Settings;

    private LatLng customerServiceLocation;

    private int radius = 1;
    private boolean requestBol = false;

    private Boolean serviceProviderFound = false;
    private String serviceProviderFoundID;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String customerId;
    private String destination,requestservice;
    private RadioGroup radioGroup;

    private DatabaseReference customerDatabaseRef;
    private DatabaseReference serviceProviderRef;
    private DatabaseReference ServiceProviderRefL;
    private DatabaseReference ServiceProviderLcationRef;
    private LinearLayout spInfo;
    private ImageView spProfileImage;
    private TextView spName, spPhone,spDestination;



    //custom map marker
    Marker mCurrent;

    Marker ServiceProviderMarker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Firebase

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        customerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customers Request");
        serviceProviderRef = FirebaseDatabase.getInstance().getReference().child("Service Providers Available");
        ServiceProviderLcationRef = FirebaseDatabase.getInstance().getReference().child("Service Provider Working");


        ServiceRequestButton = (Button) findViewById(R.id.service_request_btn);



        //customer map

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        spInfo = (LinearLayout) findViewById(R.id.spInfo);
        spProfileImage = (ImageView) findViewById(R.id.spProfileImage);
        spName = (TextView) findViewById(R.id.spName);
        spPhone = (TextView) findViewById(R.id.spPhone);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        radioGroup.check(R.id.Electrician);



        ServiceRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requestBol){
                    requestBol = false;
                    geoQuery.removeAllListeners();
                    ServiceProviderLcationRef1.removeEventListener(ServiceProviderLocationListener);

                    if(serviceProviderFoundID!= null){
                        ServiceProviderRefL = FirebaseDatabase.getInstance().getReference()
                                .child("Users").child("Service Providers").child(serviceProviderFoundID).child("Customers Request");
                        ServiceProviderRefL.removeValue();
                        serviceProviderFoundID = null;
                    }
                    serviceProviderFound = false;
                    radius = 1;
                    GeoFire geoFire = new GeoFire(customerDatabaseRef);
                    geoFire.removeLocation(customerId);
                    if (ServiceProviderMarker!=null){
                        ServiceProviderMarker.remove();
                    }
                    ServiceRequestButton.setText("Request Service");
                    spInfo.setVisibility(View.GONE);
                    spName.setText("");
                    spPhone.setText("");
                    spProfileImage.setImageResource(R.mipmap.ic_default_user);
                }
                else {
                    int selectId = radioGroup.getCheckedRadioButtonId();
                    final RadioButton radioButton = (RadioButton) findViewById(selectId);
                    if(radioButton.getText() == null){
                        return;
                    }

                    requestservice = radioButton.getText().toString();
                    requestBol = true;
                    GeoFire geoFire = new GeoFire(customerDatabaseRef);
                    geoFire.setLocation(customerId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                    customerServiceLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

                    //custom map marker
                    if (mCurrent != null) {

                        mCurrent.remove();
                    }

                    mCurrent = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.customer_map_marker))
                            .position(customerServiceLocation)
                            .title("You"));


                    ServiceRequestButton.setText("Getting Service Provider");

                    getServiceProvider();


                }}
        });
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination = place.getName().toString();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.

            }
        });


    }
    GeoQuery geoQuery;
    private void getServiceProvider() {

        GeoFire geoFire = new GeoFire(serviceProviderRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(customerServiceLocation.latitude, customerServiceLocation.longitude), radius );
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(!serviceProviderFound && requestBol){
                    DatabaseReference customerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Service Providers").child(key);
                    customerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                                Map<String, Object> spmap = (Map<String, Object>) dataSnapshot.getValue();
                                if (serviceProviderFound) {
                                    return;
                                }


                                if (spmap.get("service").equals(requestservice)) {
                                        serviceProviderFound = true;
                                        serviceProviderFoundID = dataSnapshot.getKey();

                                        ServiceProviderRefL = FirebaseDatabase.getInstance().getReference()
                                                .child("Users").child("Service Providers").child(serviceProviderFoundID).child("Customers Request");

                                        HashMap ServiceProviderMap = new HashMap();
                                        ServiceProviderMap.put("CustomerServiceID", customerId);
                                        ServiceProviderMap.put("destination", destination);
                                        ServiceProviderRefL.updateChildren(ServiceProviderMap);

                                        gettingServiceProviderLocation();
                                        gettingServiceProviderInfo();
                                        ServiceRequestButton.setText("Looking for SP...");
                                    }

                                }
                            }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });


                }


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if(!serviceProviderFound){

                    radius = radius + 1;
                    getServiceProvider();

                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private DatabaseReference ServiceProviderLcationRef1;
    private ValueEventListener ServiceProviderLocationListener;
    private void gettingServiceProviderLocation() {


        ServiceProviderLcationRef1= FirebaseDatabase.getInstance().getReference().child("Service Provider Working").child(serviceProviderFoundID).child("l");
        ServiceProviderLocationListener = ServiceProviderLcationRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && requestBol){

                    List<Object> ServiceProviderLocationMap = (List<Object>) dataSnapshot.getValue();

                    double LocationLat = 0;
                    double LocationLng = 0;

                    ServiceRequestButton.setText("Service Provider Found");

                    if(ServiceProviderLocationMap.get(0) != null){

                        LocationLat = Double.parseDouble(ServiceProviderLocationMap.get(0).toString());

                    }

                    if(ServiceProviderLocationMap.get(1) != null){

                        LocationLng = Double.parseDouble(ServiceProviderLocationMap.get(1).toString());

                    }

                    LatLng ServiceProviderLatLng = new LatLng(LocationLat,LocationLng);
                    if(ServiceProviderMarker != null){

                        ServiceProviderMarker.remove();
                    }


                    Location locationc = new Location("");
                    locationc.setLatitude(customerServiceLocation.latitude);
                    locationc.setLongitude(customerServiceLocation.longitude);

                    Location locationi = new Location("");
                    locationi.setLatitude(ServiceProviderLatLng.latitude);
                    locationi.setLongitude(ServiceProviderLatLng.longitude);

                    float Distance = locationc.distanceTo(locationi);
                    if(Distance<100){
                        ServiceRequestButton.setText("Service Provider is near");
                    }
                    if (Distance<20){
                        ServiceRequestButton.setText("Service Ongoing! (End?)");
                    }

                    else {

                        ServiceRequestButton.setText("SP Distance: " + String.valueOf(Distance) + "m (Cancel?)");
                    }

                    ServiceProviderMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.spmapmarker))
                            .position(ServiceProviderLatLng)
                            .title("Your service provider"));




                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.customer_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_customer_settings) {
            Intent intent = new Intent(CustomerHome.this,CustomerSettings.class);
            startActivity(intent);


        } else if (id == R.id.nav_customer_logout) {

            FirebaseAuth.getInstance().signOut();
            LogoutCustomer();


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void gettingServiceProviderInfo(){
        spInfo.setVisibility(View.VISIBLE);
        DatabaseReference CustomerDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("Service Providers").child(serviceProviderFoundID);
        CustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!= null){
                        spName.setText(map.get("name").toString());
                    }
                    if(map.get("phone")!= null){
                        spPhone.setText( map.get("phone").toString());
                    }

                    if(map.get("profileImageUrl")!= null){

                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(spProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapsblue));

            if (!success) {
                Log.e("CustomerHome", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("CustomerHome", "Can't find style. Error: ", e);
        }

        buildGoogleApiClient();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));


        /*

        //custom map marker
        if(mCurrent != null){

            mCurrent.remove();
        }

        mCurrent = mMap.addMarker(new MarkerOptions()
                       .icon(BitmapDescriptorFactory.fromResource(R.mipmap.customer_map_marker))
                       .position(latLng)
                       .title("You"));


                       */



    }

    protected synchronized void buildGoogleApiClient(){

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();


    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    private void LogoutCustomer() {

        Intent logoutIntent = new Intent(CustomerHome.this, WelcomeActivity.class);

        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(logoutIntent);
        finish();

    }


}
