package com.test.www.finalproject.model;

import java.io.Serializable;

/**
 * Created by kkmnb on 2017-09-08.
 */

public class PushModel implements Serializable{
    String sender, msg, channel;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
