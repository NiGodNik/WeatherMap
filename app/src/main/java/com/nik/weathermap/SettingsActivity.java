package com.nik.weathermap;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyPreferenceFragment myPreference = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, myPreference).commit();

//        setContentView(R.layout.activity_settings);
    }



    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey() == getString(R.string.remove)) {
            Log.d(TAG, "Click remove");

            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setTitle("You want to delete the list of places?")
                    .setPositiveButton("Yes", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.d(TAG, "Yes pressed");
                                    removeDB();
                                }
                            })
                    .setNegativeButton("Cancel", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.d(TAG, "Cancel pressed");
                                }
                            });
            dialog.show();
            return true;
        }
        return false;
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        private final String TAG = this.getClass().getSimpleName();

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
            Preference remove = findPreference(getString(R.string.remove));
            //Redirecting the click.listener in Activity
            remove.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) MyPreferenceFragment.this.getActivity());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "preference: " + preference.getClass().getSimpleName() + ", key: " +
                preference.getKey() + ", new value: " + newValue.toString());
        String stringNewValue = newValue.toString();
        preference.setSummary(stringNewValue);
        return true;
    }

    private void removeDB() {
        Log.d(TAG, "Database removing");
        PlacesDataBase.PlacesDBHelper dbHelper = new PlacesDataBase.PlacesDBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(
                PlacesDataBase.PlacesEntry.TABLE_NAME,
                null,
                null
        );

        db.close();
        Log.d(TAG, "Database removed");
        Toast.makeText(this, "Places removed", Toast.LENGTH_SHORT).show();
    }


}
