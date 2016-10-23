package com.nik.weathermap;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap
        .OnMarkerDragListener {

    private final String TAG = this.getClass().getSimpleName();

    final Context context = this;
    private SharedPreferences sharedPreferences;
    private GoogleMap mMap;
    private double mLatitude;
    private double mLongitude;
    private String placeName = null;
    private String placeLon = null;
    private String placeLat = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                (MapsActivity.this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_maps);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Save-button pressed");

                //Initialize EditText field
                final EditText textInput = new EditText(context);
                textInput.setInputType(InputType.TYPE_CLASS_TEXT);

                //Initialize AlertDialog
                AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                        .setTitle("Save the selected location?")
                        .setView(textInput)
                        .setPositiveButton("Yes", new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Log.d(TAG, "PositiveButton pressed");
                                        placeName = textInput.getText().toString();
                                        Log.d(TAG, "Updated placeName " + placeName);
                                        saveToDB();
//                                        closeActivity();
                                    }
                                })
                        .setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Log.d(TAG, "NegativeButton pressed");
                                    }
                                });
                dialog.show();
            }
        });
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
        mMap = googleMap;

//        useHomeLocation();

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

//    private void useHomeLocation() {
//        Geocoder geocoder;
//        List<Address> addressList;
//
//        //Get location from preferences
//        String location = sharedPreferences.getString("Tomsk, Russia",
//                "Tomsk, Russia");
//        Log.d(TAG, "Location: " + location);
//
//        //Try getting coordinates using location name
//        try {
//            geocoder = new Geocoder(MapsActivity.this);
//            addressList = geocoder.getFromLocationName(location, 1);
//            if (addressList.size() > 0) {
//                //Get latitude and longitude
//                mLatitude = addressList.get(0).getLatitude();
//                mLongitude = addressList.get(0).getLongitude();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void saveToDB() {
        Log.d(TAG, "Saving to DB..");
        PlacesDataBase.PlacesDBHelper dbHelper = new PlacesDataBase.PlacesDBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Init values for db input
        ContentValues values = new ContentValues();
        //Marker wasn't moved, use home location
        if (placeLat == null || placeLon == null) {
            Log.d(TAG, "..using home location..");
            values.put(PlacesDataBase.PlacesEntry.COLUMN_NAME_LAT, String.valueOf(mLatitude));
            values.put(PlacesDataBase.PlacesEntry.COLUMN_NAME_LON, String.valueOf(mLongitude));
        }
        //Marker was moved, use new location
        else {
            Log.d(TAG, "..using marker location..");
            values.put(PlacesDataBase.PlacesEntry.COLUMN_NAME_LAT, placeLat);
            values.put(PlacesDataBase.PlacesEntry.COLUMN_NAME_LON, placeLon);
        }

        values.put(PlacesDataBase.PlacesEntry.COLUMN_NAME_TITLE, placeName);

        long newRowId = db.insert(
                PlacesDataBase.PlacesEntry.TABLE_NAME,
                null,
                values
        );
        Log.d(TAG, "..created new row " + newRowId);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.d(TAG, "Drag start: " + marker.getTitle());
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Log.d(TAG, "Drag end: " + marker.getTitle());
        placeLat = String.valueOf(marker.getPosition().latitude);
        placeLon = String.valueOf(marker.getPosition().longitude);
        Log.d(TAG, "Updated placeLat " + placeLat + ", placeLon " + placeLon);
        Toast.makeText(this, "Updated placeLat " + placeLat + ", placeLon " + placeLon, Toast.LENGTH_SHORT).show();
    }

    private void closeActivity() {
        this.finish();
    }
}
