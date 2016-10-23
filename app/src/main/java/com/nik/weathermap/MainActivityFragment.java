package com.nik.weathermap;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    private PlaceAdapter placeAdapter;
    private List<Place> placesList;

    public MainActivityFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        placesList = readFromDB();

        placeAdapter = new PlaceAdapter(
                this.getContext(),
                R.layout.list_item_place,
                R.id.list_item_place_textview,
                placesList);

        //Luodaan ListView hakemalla se oikealla id:llä
        ListView listView = (ListView) rootView.findViewById(R.id.listview_main);
        //Asetetaan ListViewiin adapteri
        listView.setAdapter(placeAdapter);

        //Tehdään jokaiselle listan elementille oma painalluksen kuuntelijansa
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Haetaan tiedot valitusta Paikka-oliosta String-taulukkoon

                String[] placeTiedot = {placeAdapter.getItem(position).getName(),
                        placeAdapter.getItem(position).getLat(),
                        placeAdapter.getItem(position).getLon()};

                //Näytetään Toast painalluksesta
                String toast = "Avataan: " + placeTiedot[0] + "\n" + placeTiedot[1] + ", " +
                        placeTiedot[2];
                Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();

                //Käynnistetään ForecastActivity, johon viedään valitun paikan koordinaatit
                //String-taulukkona

//                Intent intent = new Intent(getActivity(), ForecastActivity.class).putExtra
//                        (Intent.EXTRA_TEXT, paikkaTiedot);
//                startActivity(intent);
            }
        });

        return rootView;
    }

    private List<Place> readFromDB() {
        Log.d(TAG, "Reading from DB");
        List<Place> rowList = new ArrayList<>();

        String[] projection = {
                PlacesDataBase.PlacesEntry.COLUMN_NAME_TITLE,
                PlacesDataBase.PlacesEntry.COLUMN_NAME_LAT,
                PlacesDataBase.PlacesEntry.COLUMN_NAME_LON,
        };

        PlacesDataBase.PlacesDBHelper dbHelper = new PlacesDataBase.PlacesDBHelper(this.getContext());
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
