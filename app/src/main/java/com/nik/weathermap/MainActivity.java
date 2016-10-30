package com.nik.weathermap;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(view.getContext(), MapsActivity.class));
            }
        });

        FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
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
                                        DataLongOperationAsynchTask longOperation = new DataLongOperationAsynchTask();
                                        longOperation.execute(textInput.getText().toString());
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
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
        this.recreate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveToDB(String placeName, String placeLat, String placeLon) {
        Log.d(TAG, "Saving to DB..");
        PlacesDataBase.PlacesDBHelper dbHelper = new PlacesDataBase.PlacesDBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //Init values for db input
        ContentValues values = new ContentValues();
        //Marker wasn't moved, use home location
        if (placeLat != null || placeLon != null) {
            Log.d(TAG, "..using home location..");
            values.put(PlacesDataBase.PlacesEntry.COLUMN_NAME_LAT, String.valueOf(placeLat));
            values.put(PlacesDataBase.PlacesEntry.COLUMN_NAME_LON, String.valueOf(placeLon));
        }
        //Marker was moved, use new location
        else {
            Log.d(TAG, "Empty location");
//            values.put(PlacesDataBase.PlacesEntry.COLUMN_NAME_LAT, placeLat);
//            values.put(PlacesDataBase.PlacesEntry.COLUMN_NAME_LON, placeLon);
            return;
        }

        values.put(PlacesDataBase.PlacesEntry.COLUMN_NAME_TITLE, placeName);

        long newRowId = db.insert(
                PlacesDataBase.PlacesEntry.TABLE_NAME,
                null,
                values
        );
        Log.d(TAG, "..created new row " + newRowId);

        refreshActivity();
    }

    private void refreshActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private class DataLongOperationAsynchTask extends AsyncTask<String, Void, String> {

        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        String placeName;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            placeName = params[0];
            String response;
            try {
                response = getLatLongByURL("http://maps.google.com/maps/api/geocode/json?address=" + placeName + "&sensor=false");
                Log.d(TAG, "" + response);
                return response;
            } catch (Exception e) {
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);

                double lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");

                double lon = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

                Log.d(TAG, "lat " + lat);
                Log.d(TAG, "lat " + lon);

                saveToDB(placeName, Double.toString(lat), Double.toString(lon));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    public String getLatLongByURL(String requestURL) {
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
