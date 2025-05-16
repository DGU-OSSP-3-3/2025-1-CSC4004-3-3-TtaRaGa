package com.example.ttaraga.ttaraga.dto;

public class Bikedto {
    private long stationId;
    private long parkingBikeTotCnt;
    private double stationLatitude;
    private double stationLongitude;
    private String stationName;

    public Bikedto(){}

    public Bikedto(long stationId, long partkingBikeTotCnt, double stationLatitude, double stationLongitude, String stationName) {
        this.stationId = stationId;
        this.parkingBikeTotCnt = partkingBikeTotCnt;
        this.stationLatitude = stationLatitude;
        this.stationLongitude = stationLongitude;
        this.stationName = stationName;
    }
    public long getStationId() {
        return stationId;
    }
    public long getParkingBikeTotCnt() {
        return parkingBikeTotCnt;
    }
    public double getStationLatitude() {
        return stationLatitude;
    }
    public double getStationLongitude() {
        return stationLongitude;
    }
    public String getStationName() {
        return stationName;
    }
    public void setStationId(long stationId) {
        this.stationId = stationId;
    }
    public void setParkingBikeTotCnt(long parkingBikeTotCnt) {
        this.parkingBikeTotCnt = parkingBikeTotCnt;
    }
    public void setStationLatitude(double stationLatitude) {
        this.stationLatitude = stationLatitude;
    }
    public void setStationLongitude(double stationLongitude) {
        this.stationLongitude = stationLongitude;
    }
    public void setStationName(String stationName) {
        this.stationName = stationName;
    }
}
