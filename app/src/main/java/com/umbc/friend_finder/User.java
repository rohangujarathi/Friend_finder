package com.umbc.friend_finder;

public class User {

    public double latitude;
    public double longitude;
    public String userName;
    public long timeStamp;

    public User(){

    }

    public User(String userName, double latitude, double longitude, long timeStamp){
        this.userName = userName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeStamp = timeStamp;
    }
}
