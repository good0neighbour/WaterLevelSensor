package com.example.userapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class CurrentStatus extends AppCompatActivity {
    private String currentSfLv;
    private String currentWtLv;
    private final Handler handler = new Handler();
    private boolean running;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_status);

        TextView safetyLevel = findViewById(R.id.safetyLevel);
        TextView waterLevel = findViewById(R.id.waterLevel);

        //수위 표시 업데이트
        running = true;
        Thread thread = new Thread(() -> {
            while (running){
                try {
                    if (!MainActivity.waterLevelText.equals(currentWtLv)){
                        currentWtLv = MainActivity.waterLevelText;
                        handler.post(() -> {
                            waterLevel.setText(currentWtLv);
                            if (!MainActivity.safetyLevelText.equals(currentSfLv)){
                                currentSfLv = MainActivity.safetyLevelText;
                                safetyLevel.setText(currentSfLv);
                            }
                        });
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        running = false;
        super.onDestroy();
    }
}