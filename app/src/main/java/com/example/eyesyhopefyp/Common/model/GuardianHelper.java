package com.example.eyesyhopefyp.Common.model;

public class GuardianHelper {
    String userName,phoneNo,password;

    public GuardianHelper(String userName, String phoneNo, String password) {
        this.userName = userName;
        this.phoneNo = phoneNo;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getPassword() {
        return password;
    }
}
