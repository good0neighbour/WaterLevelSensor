package com.example.userapplication.database;

import java.util.Hashtable;

public class DataBase {
    public static Hashtable<String, UserTable> Users = new Hashtable<>();

    public static void DataBaseInitializing() {
        Users.put("amor2022",
                new UserTable(
                        "amor2022",
                        new String[] { "test0", "test1" }
                ));
    }
}