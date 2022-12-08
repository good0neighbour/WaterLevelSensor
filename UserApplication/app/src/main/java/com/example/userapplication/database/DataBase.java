package com.example.userapplication.database;

import com.example.userapplication.MainActivity;
import java.util.Hashtable;

public class DataBase {
    public static Hashtable<String, UserTable> Users = new Hashtable<>();

    public static void DataBaseInitializing() {
        Users.put(
                "amor2022",
                new UserTable(
                        "amor2022",
                        "00건물 안전관리 시스템",
                        MainActivity.Instance.LoadJson()
                )
        );
    }
}