package com.test.www.finalproject.model;

import java.util.HashMap;
import java.util.Map;

public class MessageModel
{
    String msg;         //:"hi",
    String time;          //:1231312434,
    String sender;      //:보낸사람ID,
    String type;        //:t(텍스트)
    int readCnt;        //:읽은수

    public MessageModel() {}

    public MessageModel(String msg, String time, String sender, String type, int readCnt) {
        this.msg = msg;
        this.time = time;
        this.sender = sender;
        this.type = type;
        this.readCnt = readCnt;
    }

    public Map<String, Object> toMap()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("msg", msg);
        map.put("time", time);
        map.put("sender", sender);
        map.put("type", type);
        map.put("readCnt", readCnt);
        return map;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getReadCnt() {
        return readCnt;
    }

    public void setReadCnt(int readCnt) {
        this.readCnt = readCnt;
    }
}
