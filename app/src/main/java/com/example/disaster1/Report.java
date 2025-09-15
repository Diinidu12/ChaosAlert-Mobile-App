package com.example.disaster1;

public class Report {
    private String location;
    private String fileName;
    private String incidentType;
    private String date;
    private String time;

    public Report(String fileName, String incidentType, String date, String time, String location) {
        this.fileName = fileName;
        this.incidentType = incidentType;
        this.date = date;
        this.time = time;
        this.location = location;
    }

    public String getFileName() {
        return fileName;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
    public String getLocation() {
        return location;
    }
}

