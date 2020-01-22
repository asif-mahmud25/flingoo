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
import android.widget.ImageView;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout;
import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class ServiceProviderHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,

        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    //from driver map
    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    private  boolean currentLogoutStatus = false;
    private DatabaseReference AssingedCustomerRef;
    private DatabaseReference AssingedCustomerLocationRef;
    private String ServiceProviderID;
    private String CustomerID = "";
    private LinearLayout customerInfo;
    private ImageView customerProfileImage;
    private TextView customerName, customerPhone,customerDestination;


    //custom map marker
    Marker mCurrent;
    Marker sMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_provider_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //service provider map


        ServiceProviderID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        customerInfo = (LinearLayout) findViewById(R.id.customerInfo);
        customerProfileImage = (ImageView) findViewById(R.id.customerProfileImage);
        customerName = (TextView) findViewById(R.id.customerName);
        customerPhone = (TextView) findViewById(R.id.customerPhone);
        customerDestination = (TextView) findViewById(R.id.customerdestination);



        GetAssingedCustomerRequest();


    }



    private void GetAssingedCustomerRequest() {

        AssingedCustomerRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Service Providers").child(ServiceProviderID).child("Customers Request").child("CustomerServiceID");
//                .child("customer request")



        AssingedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    CustomerID = dataSnapshot.getValue().toString();

                    GetAssingedCustomerLocation();
                    GetAssingedCustomerDestination();
                    GetAssingedcustomerInfo();

                }
                else{
                    CustomerID = "";
                    if(sMarker!=null){
                        sMarker.remove();
                    }
                    if(AssingedCustomerLocationRefL != null){
                        AssingedCustomerLocationRef.removeEventListener(AssingedCustomerLocationRefL);}

                    customerInfo.setVisibility(View.GONE);
                    customerName.setText("");
                    customerPhone.setText("");
                    customerDestination.setText("Destination:  --" );
                    customerProfileImage.setImageResource(R.mipmap.ic_default_user);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private ValueEventListener AssingedCustomerLocationRefL;
    private void GetAssingedCustomerLocation() {

        AssingedCustomerLocationRef = FirebaseDatabase.getInstance().getReference()
                .child("Customers Request").child(CustomerID).child("l");

        AssingedCustomerLocationRefL = AssingedCustomerLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && !CustomerID.equals("")){

                    List<Object> CustomerLocationMap = (List<Object>) dataSnapshot.getValue();

                    double LocationLat = 0;
                    double LocationLng = 0;



                    if(CustomerLocationMap.get(0) != null){

                        LocationLat = Double.parseDouble(CustomerLocationMap.get(0).toString());

                    }

                    if(CustomerLocationMap.get(1) != null){

                        LocationLng = Double.parseDouble(CustomerLocationMap.get(1).toString());

                    }

                    LatLng ServiceProviderLatLng = new LatLng(LocationLat,LocationLng);
                    sMarker = mMap.addMarker( new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.customer_map_marker))
                            .position(ServiceProviderLatLng).title("Customer Location"));


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
    private void GetAssingedCustomerDestination() {

        AssingedCustomerRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Service Providers").child(ServiceProviderID).child("Customers Request").child("destination");


        AssingedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    String Destination  = dataSnapshot.getValue().toString();
                    customerDestination.setText("Destination:" + Destination);

                }
                else{
                    customerDestination.setText("Destination:  --" );
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private void GetAssingedcustomerInfo(){
        customerInfo.setVisibility(View.VISIBLE);
        DatabaseReference CustomerDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(CustomerID);
        CustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!= null){
                        customerName.setText(map.get("name").toString());
                    }
                    if(map.get("phone")!= null){
                        customerPhone.setText( map.get("phone").toString());
                    }

                    if(map.get("profileImageUrl")!= null){

                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(customerProfileImage);
                    }
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
        getMenuInflater().inflate(R.menu.service_provider_home, menu);
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

        if (id == R.id.nav_settings) {
            Intent intent = new Intent(ServiceProviderHome.this,ServiceProviderSettings.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {

            currentLogoutStatus=true;
            DisconnectServiceProvider();

            FirebaseAuth.getInstance().signOut();
            LogoutServiceProvider();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    //from driver map

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
                Log.e("ServiceProviderHome", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("ServiceProviderHome", "Can't find style. Error: ", e);
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

        if(getApplicationContext() != null){

            lastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));


            //custom map marker
            if(mCurrent != null){

                mCurrent.remove();
            }

            mCurrent = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.spmapmarker))
                    .position(latLng)
                    .title("You"));






            //Location save in Database

            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ServiceProviderAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Service Providers Available");

            GeoFire geoFire = new GeoFire(ServiceProviderAvailabilityRef);


            DatabaseReference ServiceProviderWorkingRef = FirebaseDatabase.getInstance().getReference().child("Service Provider Working");

            GeoFire geoFireWorking = new GeoFire(ServiceProviderWorkingRef);


           /*geoFire.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
           geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));*/




            switch (CustomerID){

                case "":
                    geoFireWorking.removeLocation(userID);

                    geoFire.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

                default:
                    geoFire.removeLocation(userID);
                    geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

            }


        }



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

        if(!currentLogoutStatus){

            DisconnectServiceProvider();
        }


    }

    private void DisconnectServiceProvider() {

        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ServiceProviderAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Service Providers Available");

        GeoFire geoFire = new GeoFire(ServiceProviderAvailabilityRef);
        geoFire.removeLocation(userID);

    }

    private void LogoutServiceProvider() {

        Intent logoutIntent = new Intent(ServiceProviderHome.this, WelcomeActivity.class);

        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(logoutIntent);
        finish();

    }


}
