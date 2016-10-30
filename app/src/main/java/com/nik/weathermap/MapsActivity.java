package com.nik.weathermap;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap
        .OnMarkerDragListener {

    private final String TAG = this.getClass().getSimpleName();

    private final Context context = this;
    private SharedPreferences sharedPreferences;
    private GoogleMap mMap;
    private List<Place> placesList;

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

        placesList = readFromDB();


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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        addMarkersOnMap(placesList);

    }

    @Override
    protected void onResume() {
        super.onResume();
//        setUpMap();
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

    private void addMarkersOnMap(List<Place> listPlaces) {

        double lat;
        double lon;

        Log.d(TAG, "addMarkersOnMap");

        for(Place place :listPlaces){
            try {
                Log.d(TAG,place.getName());
                lat = Double.parseDouble(place.getLat());
                lon = Double.parseDouble(place.getLon());
                LatLng coordinates = new LatLng(lat, lon);
                Log.d(TAG,coordinates.toString());
                mMap.addMarker(new MarkerOptions()
                        .position(coordinates)
                        .title(place.getName())
                        .snippet("-10"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates));
                Log.d(TAG,"norm");
            } catch (NumberFormatException e) {
                Log.e(TAG,e.toString());
            }
        }

    }

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

    private List<Place> readFromDB() {
        Log.d(TAG, "Reading from DB");
        List<Place> rowList = new ArrayList<>();

        String[] projection = {
                PlacesDataBase.PlacesEntry.COLUMN_NAME_TITLE,
                PlacesDataBase.PlacesEntry.COLUMN_NAME_LAT,
                PlacesDataBase.PlacesEntry.COLUMN_NAME_LON,
        };

        PlacesDataBase.PlacesDBHelper dbHelper = new PlacesDataBase.PlacesDBHelper(this.getBaseContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                PlacesDataBase.PlacesEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(PlacesDataBase.PlacesEntry
                    .COLUMN_NAME_TITLE));
            String lat = cursor.getString(cursor.getColumnIndexOrThrow(PlacesDataBase.PlacesEntry
                    .COLUMN_NAME_LAT));
            String lon = cursor.getString(cursor.getColumnIndexOrThrow(PlacesDataBase.PlacesEntry
                    .COLUMN_NAME_LON));
            Place place = new Place(name, lat, lon);
            Log.d(TAG, "Place " + place.getName() + " " + place.getLat() + " " + place.getLon());
            rowList.add(place);
            cursor.moveToNext();
        }

        Log.d(TAG, "Number of places:" + rowList.size());
        cursor.close();
        db.close();
        dbHelper.close();

        return rowList;
    }

}
