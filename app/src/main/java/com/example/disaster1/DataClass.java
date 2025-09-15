package com.example.disaster1;

public class DataClass {
    private String disasterType;
    private String latitude;
    private String longitude;
    private String date;
    private String time;
    private String description;

    public String getDisasterType() {
        return disasterType;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public DataClass(String disasterType, String latitude, String longitude, String date, String time, String description) {
        this.disasterType = disasterType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.time = time;
        this.description = description;
    }
}
