package com.nik.weathermap;

/**
 * Created by Nik on 23.10.2016.
 */

public class Place {

    private final String TAG = this.getClass().getSimpleName();

    private String name;
    private String lat;
    private String lon;

    public Place(String name, String lat, String lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }
}