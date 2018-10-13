package com.test.www.finalproject.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kkmnb on 2017-09-15.
 */

public class WorkModel {
    String state, time;

    public WorkModel() {
    }

    // 회원용 그릇
    public WorkModel(String state, String time) {
        this.state = state;
        this.time = time;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("state", state);
        map.put("time", time);
        return map;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
