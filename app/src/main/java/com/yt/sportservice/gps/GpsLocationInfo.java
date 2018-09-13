package com.yt.sportservice.gps;

/**
 * project : GpsDemo
 * describe : LocationInfo
 *
 * @author : ChenJP
 * @date : 2017/11/17  17:56
 */

public class GpsLocationInfo {
    private double longitude;
    private double latitude;
    private float  accuracy;
    private float  speed;
    private double altitude;
    private float  bearing;
    private int satelliteNumber;

    public GpsLocationInfo() {
    }

    public GpsLocationInfo(double longitude, double latitude, float accuracy, float speed, double altitude, float bearing) {
        this(longitude,  latitude,  accuracy,  speed,  altitude,  bearing ,0);
    }

    public GpsLocationInfo(double longitude, double latitude, float accuracy, float speed, double altitude, float bearing, int satelliteNumber) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.accuracy = accuracy;
        this.speed = speed;
        this.altitude = altitude;
        this.bearing = bearing;
        this.satelliteNumber = satelliteNumber;
    }

    public GpsLocationInfo setLocationInfo(double longitude, double latitude, float accuracy, float speed, double altitude, float bearing, int satelliteNumber) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.accuracy = accuracy;
        this.speed = speed;
        this.altitude = altitude;
        this.bearing = bearing;
        this.satelliteNumber = satelliteNumber;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public int getSatelliteNumber() {
        return satelliteNumber;
    }

    public void setSatelliteNumber(int satelliteNumber) {
        this.satelliteNumber = satelliteNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GpsLocationInfo)) return false;

        GpsLocationInfo that = (GpsLocationInfo) obj;

        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Float.compare(that.accuracy, accuracy) != 0) return false;
        if (Float.compare(that.speed, speed) != 0) return false;
        if (Double.compare(that.altitude, altitude) != 0) return false;
        if (Float.compare(that.bearing, bearing) != 0) return false;
        return satelliteNumber == that.satelliteNumber;
    }

    @Override
    public String toString() {
        return "LocationInfo{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                ", accuracy=" + accuracy +
                ", speed=" + speed +
                ", altitude=" + altitude +
                ", bearing=" + bearing +
                ", satelliteNumber=" + satelliteNumber +
                '}';
    }
}
