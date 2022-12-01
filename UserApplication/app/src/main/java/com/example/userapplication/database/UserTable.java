package com.example.userapplication.database;

import java.util.Collections;
import java.util.LinkedList;

public class UserTable {
    public LinkedList<DeviceInfo> Devices = new LinkedList<>();
    public String Password;
    public String UserTitle;

    public UserTable(String password, String userTitle, DeviceInfo[] devices) {
        Password = password;
        UserTitle = userTitle;
        Collections.addAll(Devices, devices);
    }
}
