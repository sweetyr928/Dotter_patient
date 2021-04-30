package gujc.dotterPatient.model;

import java.util.Date;

public class Board {
    public String name;
    public String title;
    public String id;
    public boolean match;
    public boolean request;
    private Date timestamp;
    public String doctor;
    public String hospital;
    public String doctorid;
    public int status; // 1: 등록 2: 매칭 대기 중 3: 매칭 성공
    public String phoneNum;
    public String note;

    public void setNote(String note) { this.note = note; }

    public String getNote() { return note;}

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public int getStatus() { return status;}

    public void setStatus(int status) { this.status = status;}

    public boolean isRequest() { return request;}

    public void setRequest(boolean request) { this.request = request;}

    public String getDoctorid() {
        return doctorid;
    }

    public void setDoctorid(String doctorid) {
        this.doctorid = doctorid;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Board(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
