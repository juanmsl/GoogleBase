package com.juanmsl.googlebase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.juanmsl.googlebase.constants.Maps;
import com.juanmsl.googlebase.constants.Permissions;
import com.juanmsl.googlebase.controllers.LocationController;
import com.juanmsl.googlebase.logic.FBAuth;
import com.juanmsl.googlebase.logic.Utils;

import java.io.File;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback, PlaceSelectionListener {

    private FBAuth firebaseAuthentication;
    private LocationController locationController;
    private GoogleMap googleMap;
    private Intent mainIntent;
    private FirebaseUser user;
    private Place place;

    private Toolbar toolbar;
    private ImageView image;
    private TextView userName;
    private TextView userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(this);

        firebaseAuthentication = new FBAuth(this) {
            @Override
            public void onSuccess() {
                user = firebaseAuthentication.getUser();
                userName.setText(user.getDisplayName());
                userEmail.setText(user.getEmail());
                firebaseAuthentication.downloadFile(user.getEmail());
            }

            @Override
            public void onDownloadSuccess(File file) {
                Uri imageUri = Uri.fromFile(file);
                if(imageUri != null) {
                    Bitmap selectedImage = Utils.getImageFormUri(HomeActivity.this, imageUri);
                    image.setImageBitmap(Utils.cropImage(selectedImage));
                }
            }
        };
        locationController =  new LocationController(this) {
            @Override
            public void onMyLocationRecieved(Location location) {
                googleMap.clear();
                double distance = Utils.distance(location.getLatitude(), location.getLongitude(), place.getLatLng().latitude, place.getLatLng().longitude);
                addMarker(location, "Tu estas aquí", null);
                addMarker(place.getLatLng(), place.getAddress().toString(), "Distancia: " + distance + " km");
                Utils.drawPathBetween(new LatLng(location.getLatitude(), location.getLongitude()), place.getLatLng(), googleMap);
                Snackbar.make(HomeActivity.this.getCurrentFocus(), "Distancia a la que te encuentras: " + distance + " km", Snackbar.LENGTH_LONG).show();

                CameraPosition.Builder cameraPosition = CameraPosition.builder();
                cameraPosition.target(place.getLatLng());
                cameraPosition.zoom(Maps.ZOOM_PATH);
                cameraPosition.bearing(0);

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition.build()), 1500, null);
            }
        };
        mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        image = (ImageView) findViewById(R.id.home_image);
        userName = (TextView) findViewById(R.id.home_username);
        userEmail = (TextView) findViewById(R.id.home_useremail);

        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setPadding(0, 150, 0, 0);

        LatLng bogota = new LatLng(4.711000, -74.072094);
        CameraPosition.Builder cameraPosition = CameraPosition.builder();
        cameraPosition.target(bogota);
        cameraPosition.zoom(Maps.ZOOM_CITY);
        cameraPosition.bearing(0);

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition.build()), 1500, null);

        this.googleMap.getUiSettings().setCompassEnabled(true);
        this.googleMap.getUiSettings().setZoomGesturesEnabled(true);
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.getUiSettings().setMapToolbarEnabled(true);

        if(Permissions.askPermission(this, Permissions.FINE_LOCATION, "Acepta los permisos para poder ubicarte")) {
            locationAction();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(Permissions.permissionGranted(requestCode, permissions, grantResults)) {
            locationAction();
        }
    }

    private void locationAction() {
        if(Permissions.checkSelfPermission(this, Permissions.FINE_LOCATION)) {
            googleMap.setMyLocationEnabled(true);
            //locationController.start();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuthentication.start();
        locationController.askForGPS();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuthentication.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //locationController.pause();
    }

    @Override
    public void onPlaceSelected(Place place) {
        this.place = place;
        locationController.getMyLocation();
    }

    @Override
    public void onError(Status status) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemClicked = item.getItemId();
        switch (itemClicked) {
            case R.id.menu_signout:
                firebaseAuthentication.signOut();
                startActivity(mainIntent);
                break;
            case R.id.menu_style_map:
                Utils.showMapStyleSelectorDialog(this, googleMap);
                break;
            case R.id.menu_type_map:
                Utils.showMapTypeSelectorDialog(this, googleMap);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addMarker(Location location, String title, String snippet) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        addMarker(latLng, title, snippet);
    }

    public void addMarker(LatLng latLng, String title, String snippet) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(title);
        if(snippet != null) {
            markerOptions.snippet(snippet);
        }
        this.googleMap.addMarker(markerOptions);
    }

    public void addMarkerAndMove(Location location, String title, String snippet, int zoom) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        addMarkerAndMove(latLng, title, snippet, zoom);
    }

    public void addMarkerAndMove(LatLng latLng, String title, String snippet, int zoom) {
        addMarker(latLng, title, snippet);
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onBackPressed() {}
}
