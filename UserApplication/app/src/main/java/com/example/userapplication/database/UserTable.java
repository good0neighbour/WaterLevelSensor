package com.example.userapplication.database;

import java.util.Collections;
import java.util.LinkedList;

public class UserTable {
    public String Password;
    public LinkedList<String> Devices = new LinkedList<>();

    public UserTable(String password, String[] devices) {
        Password = password;
        Collections.addAll(Devices, devices);
    }
}
