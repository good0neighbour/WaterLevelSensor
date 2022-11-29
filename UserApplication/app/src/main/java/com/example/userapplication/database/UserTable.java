package com.example.userapplication.database;

import java.util.Collections;
import java.util.LinkedList;

public class UserTable {
    public String Password;
    public String UserTitle;
    public LinkedList<String> Devices = new LinkedList<>();

    public UserTable(String password, String userTitle, String[] devices) {
        Password = password;
        UserTitle = userTitle;
        Collections.addAll(Devices, devices);
    }
}
