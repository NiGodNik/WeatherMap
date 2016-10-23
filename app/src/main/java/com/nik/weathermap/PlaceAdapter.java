package com.nik.weathermap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Nik on 23.10.2016.
 */

public class PlaceAdapter extends ArrayAdapter<Place> {

    private final String TAG = this.getClass().getSimpleName();

    public PlaceAdapter(Context context, int resource, int textViewResourceId, List<Place>
            objects) {
        super(context, resource, textViewResourceId, objects);
    }


    private static class ViewHolder {
        private TextView textView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.list_item_place,
                    parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.list_item_place_textview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Place place = getItem(position);

        //Jos ei tyhjä, otetaan olion nimi ja asetetaan näyttönimeksi
        if (place != null) {
            viewHolder.textView.setText(place.getName());
        }

        return convertView;
    }

}
