package com.test.www.finalproject.model;

import java.util.HashMap;
import java.util.Map;

public class ChannelModel {
    String channel, uid, lastMsg;
    long time;
    int readCnt;

    public ChannelModel() {}

    public ChannelModel(String channel, String uid, String lastMsg, long time, int readCnt) {
        this.channel = channel;
        this.uid = uid;
        this.lastMsg = lastMsg;
        this.time = time;
        this.readCnt = readCnt;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("channel", channel);
        map.put("uid", uid);
        map.put("lastMsg", lastMsg);
        map.put("time", time);
        map.put("readCnt", readCnt);
        return map;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getReadCnt() {
        return readCnt;
    }

    public void setReadCnt(int readCnt) {
        this.readCnt = readCnt;
    }
}
