package com.umbc.friend_finder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        BlankFragment.OnFragmentInteractionListener, android.location.LocationListener {

    public static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private Button logout;
    private String currentUser;
    private Marker marker = null;
    private LatLng latLng = null;
    private LocationManager locationManager;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    double currentLatitude, currentLongitude;
    private List<User> nearbyUsers = new ArrayList<User>();
    private List<Marker> markers = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(result==0) {

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


            EditText user = findViewById(R.id.editText);
            currentUser = getIntent().getStringExtra("User");
            user.setText(currentUser);

            logout = findViewById(R.id.logout);
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userRef.child(currentUser).removeValue();
                    currentUser = null;
                    Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                    intent.putExtra("User", currentUser);
                    startActivity(intent);
                }
            });
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            currentUser = getName(currentUser);
            database = FirebaseDatabase.getInstance();
            userRef = database.getReference("Users");

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        float[] dist = new float[1];
                        User user = snapshot.getValue(User.class);
                        if(user.userName.equals(currentUser)){
                           continue;
                        }
                        Location.distanceBetween(user.latitude, user.longitude, currentLatitude, currentLongitude, dist);
                        if(dist[0]<1000){
                            nearbyUsers.add(user);
                        }
                    }
                    plotNearbyUsers();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    public void plotNearbyUsers(){

        for(Marker m : markers){
            m.remove();
            m = null;
        }
        markers.clear();
        if(nearbyUsers.size()==0){
            return;
        }
        for(User user:nearbyUsers){
            LatLng ll = new LatLng(user.latitude, user.longitude);
            Marker m;
            MarkerOptions options = new MarkerOptions()
                    .position(ll)
                    .title(user.userName);
            m = mMap.addMarker(options);
            m.showInfoWindow();
            markers.add(m);
        }
        nearbyUsers.clear();

    }

    public String getName(String currentUser){
        int index = currentUser.indexOf("@");
        String userName = currentUser.substring(0, index);
        return userName;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        registerGPS();
        mMap = googleMap;
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

    }

    public void setMarker(){
        if (marker != null) {
            marker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("You");
        marker = mMap.addMarker(options);
        marker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void handleNewLocation(Location location) {

        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        latLng = new LatLng(currentLatitude, currentLongitude);
        setMarker();
        addUser();

    }

    public void addUser(){
        User user = new User(currentUser, currentLatitude, currentLongitude);
        userRef.child(currentUser).setValue(user);

    }


    // Read from the database


    @SuppressLint("MissingPermission")
    public void registerGPS() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, this);
    }

    public void deRegisterGPS(){
        locationManager.removeUpdates(this);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
