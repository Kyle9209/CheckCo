package com.test.www.finalproject.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kkmnb on 2017-10-02.
 */

public class CompanyModel {
    String adminToken;
    Beacon beacon;
    Time time;
    ArrayList<UserModel> userList;

    public CompanyModel() {}

    public CompanyModel(String adminToken, Beacon beacon, Time time, ArrayList<UserModel> userList) {
        this.adminToken = adminToken;
        this.beacon = beacon;
        this.time = time;
        this.userList = userList;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("adminToken", adminToken);
        map.put("beacon", beacon);
        map.put("time", time);
        map.put("userList", userList);
        return map;
    }

    public String getAdminToken() {
        return adminToken;
    }

    public void setAdminToken(String adminToken) {
        this.adminToken = adminToken;
    }

    public static class Beacon{
        String bName, uuid;
        int major, minor;

        public Beacon() {}

        public Beacon(String bName, String uuid, int major, int minor) {
            this.bName = bName;
            this.uuid = uuid;
            this.major = major;
            this.minor = minor;
        }

        public String getbName() {
            return bName;
        }

        public void setbName(String bName) {
            this.bName = bName;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public int getMajor() {
            return major;
        }

        public void setMajor(int major) {
            this.major = major;
        }

        public int getMinor() {
            return minor;
        }

        public void setMinor(int minor) {
            this.minor = minor;
        }
    }

    public static class Time{
        InTime inTime;
        OutTime outTime;

        public Time() {
        }

        public Time(InTime inTime, OutTime outTime) {
            this.inTime = inTime;
            this.outTime = outTime;
        }

        public static class InTime{
            int hour, minute;

            public InTime() {
            }

            public InTime(int hour, int minute) {
                this.hour = hour;
                this.minute = minute;
            }

            public int getHour() {
                return hour;
            }

            public void setHour(int hour) {
                this.hour = hour;
            }

            public int getMinute() {
                return minute;
            }

            public void setMinute(int minute) {
                this.minute = minute;
            }
        }

        public static class OutTime{
            int hour, minute;

            public OutTime() {
            }

            public OutTime(int hour, int minute) {
                this.hour = hour;
                this.minute = minute;
            }

            public int getHour() {
                return hour;
            }

            public void setHour(int hour) {
                this.hour = hour;
            }

            public int getMinute() {
                return minute;
            }

            public void setMinute(int minute) {
                this.minute = minute;
            }
        }

        public InTime getInTime() {
            return inTime;
        }

        public void setInTime(InTime inTime) {
            this.inTime = inTime;
        }

        public OutTime getOutTime() {
            return outTime;
        }

        public void setOutTime(OutTime outTime) {
            this.outTime = outTime;
        }
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public void setBeacon(Beacon beacon) {
        this.beacon = beacon;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public ArrayList<UserModel> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<UserModel> userList) {
        this.userList = userList;
    }
}
