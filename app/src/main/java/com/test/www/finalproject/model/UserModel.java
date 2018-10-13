package com.test.www.finalproject.model;

import java.util.HashMap;
import java.util.Map;

public class UserModel {
    String uid, eid, name, tel, img, token;
    Company company;
    int level, state;

    public static class Company {
        String cName, dept, position;

        public Company() {}

        public Company(String cName, String dept, String position) {
            this.cName = cName;
            this.dept = dept;
            this.position = position;
        }

        public Map<String, Object> toMap(){
            Map<String, Object> map = new HashMap<>();
            map.put("cName", cName);
            map.put("dept", dept);
            map.put("position", position);
            return map;
        }

        public String getcName() {
            return cName;
        }

        public void setcName(String cName) {
            this.cName = cName;
        }

        public String getDept() {
            return dept;
        }

        public void setDept(String dept) {
            this.dept = dept;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }

    public UserModel() {}

    public UserModel(String uid, String eid, String name, String tel, String img, String token, Company company, int level, int state) {
        this.uid = uid;
        this.eid = eid;
        this.name = name;
        this.tel = tel;
        this.img = img;
        this.token = token;
        this.company = company;
        this.level = level;
        this.state = state;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("eid", eid);
        map.put("name", name);
        map.put("tel", tel);
        map.put("img", img);
        map.put("level", level);
        map.put("company", company);
        map.put("state", state);
        map.put("token", token);
        return map;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
