package com.example.userapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.example.userapplication.database.DataBase;

import java.util.Objects;

public class CurrentStatusActivity extends AppCompatActivity {
    private final Handler handler = new Handler();
    private boolean running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_status);
        
        //경보기 리스트
        for (short i = 0; i < Objects.requireNonNull(DataBase.Users.get(MainActivity.CurrentUser)).Devices.size(); i++) {
            //todo
        }
        
        //텍스트 찾기
        TextView curSfLv = findViewById(R.id.curSfLv);
        TextView danArea = findViewById(R.id.danArea);

        //기능 수행
        running = true;
        Thread thread = new Thread(() -> {
            while (running){
                try {
                    handler.post(() -> {
                        curSfLv.setText(MainActivity.SafeLevel);
                        danArea.setText(MainActivity.DangerousArea);
                    });
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}