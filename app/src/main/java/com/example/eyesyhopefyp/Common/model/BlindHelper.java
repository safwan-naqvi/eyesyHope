package com.example.eyesyhopefyp.Common.model;

public class BlindHelper {
    String phoneNo,phoneNoGuardian,userName;

    public BlindHelper(String phoneNo, String phoneNoGuardian, String userName) {
        this.phoneNo = phoneNo;
        this.phoneNoGuardian = phoneNoGuardian;
        this.userName = userName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getPhoneNoGuardian() {
        return phoneNoGuardian;
    }

    public void setPhoneNoGuardian(String phoneNoGuardian) {
        this.phoneNoGuardian = phoneNoGuardian;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
