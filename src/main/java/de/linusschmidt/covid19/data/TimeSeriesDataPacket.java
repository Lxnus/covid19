package de.linusschmidt.covid19.data;

import java.util.TreeMap;

public class TimeSeriesDataPacket {

    private String state;
    private String country;

    private double latitude;
    private double longitude;

    private TreeMap<String, Double> series;

    public TimeSeriesDataPacket() {}

    public TimeSeriesDataPacket(String state, String country, double lat, double lon, TreeMap<String, Double> series) {
        this.state = state;
        this.country = country;
        this.latitude = lat;
        this.longitude = lon;
        this.series = series;
    }

    @Override
    public String toString() {
        return "TimeSeriesDataPacket{" +
                "state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", series=" + series +
                '}';
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public TreeMap<String, Double> getSeries() {
        return series;
    }

    public void setSeries(TreeMap<String, Double> series) {
        this.series = series;
    }
}
