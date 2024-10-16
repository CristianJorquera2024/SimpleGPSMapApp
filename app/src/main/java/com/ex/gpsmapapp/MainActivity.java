package com.ex.gpsmapapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_REQUEST_CODE = 101;
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText direction1, direction2;
    private Button calculateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        direction1 = findViewById(R.id.direction1);
        direction2 = findViewById(R.id.direction2);
        calculateButton = findViewById(R.id.calculateButton);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        calculateButton.setOnClickListener(v -> calculateDistance());

        checkLocationPermission();
    }

    private void calculateDistance() {
        String address1 = direction1.getText().toString();
        String address2 = direction2.getText().toString();

        if (address1.isEmpty() || address2.isEmpty()) {
            Toast.makeText(this, "Ingrese ambas direcciones", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng location1 = getLocationFromAddress(address1);
        LatLng location2 = getLocationFromAddress(address2);

        if (location1 != null && location2 != null) {
            googleMap.addMarker(new MarkerOptions().position(location1).title("Dirección 1"));
            googleMap.addMarker(new MarkerOptions().position(location2).title("Dirección 2"));

            googleMap.addPolyline(new PolylineOptions().add(location1, location2).width(5));

            float[] results = new float[1];
            android.location.Location.distanceBetween(
                    location1.latitude, location1.longitude,
                    location2.latitude, location2.longitude,
                    results);

            Toast.makeText(this, "Distancia: " + results[0] + " metros", Toast.LENGTH_LONG).show();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location1, 10));
        } else {
            Toast.makeText(this, "No se encontraron las ubicaciones", Toast.LENGTH_SHORT).show();
        }
    }

    private LatLng getLocationFromAddress(String strAddress) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(strAddress, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && googleMap != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("Ubicación Actual"));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
