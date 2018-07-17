package com.liu.Class;

import java.io.Serializable;

/**
 * Created by LHD on 2018/6/14.
 */

public class vehicle implements Serializable {

    private int id;

    private int lon;

    private int lat;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLon() {
        return lon;
    }

    public void setLon(int lon) {
        this.lon = lon;
    }

    public int getLat() {
        return lat;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }
}
