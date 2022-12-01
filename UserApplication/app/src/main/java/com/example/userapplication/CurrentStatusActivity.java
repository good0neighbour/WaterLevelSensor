package com.example.userapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.userapplication.database.DataBase;
import java.util.LinkedList;
import java.util.Objects;

public class CurrentStatusActivity extends AppCompatActivity {
    private final Handler handler = new Handler();
    private final LinkedList<TextView> currentStatus = new LinkedList<>();
    private final LinkedList<TextView> waterLevel = new LinkedList<>();
    private final LinkedList<Short> currentWaterLevel = new LinkedList<>();
    private short num = 0;
    private short currentSafeLevel = 0;
    private boolean running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_status);

        View listView;
        TextView tmpTV;
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        LinearLayout container = findViewById(R.id.currentStatus);

        //등록한 경보기 개수
        num = (short)Objects.requireNonNull(DataBase.Users.get(MainActivity.GetCurrentUser())).Devices.size();

        //경보기 리스트
        for (short i = 0; i < num; i++) {
            listView = layoutInflater.inflate(R.layout.addtion_sensor_status, null, false);

            //텍스트 정보 추가
            tmpTV = listView.findViewById(R.id.name);
            tmpTV.setText(Objects.requireNonNull(DataBase.Users.get(MainActivity.GetCurrentUser())).Devices.get(i).Name);
            tmpTV = listView.findViewById(R.id.location);
            tmpTV.setText(String.format(
                    "- 위치: %s",
                    Objects.requireNonNull(DataBase.Users.get(MainActivity.GetCurrentUser())).Devices.get(i).Location)
                    );

            //텍스트 찾기
            currentStatus.add(listView.findViewById(R.id.status));
            waterLevel.add(listView.findViewById(R.id.waterLevel));
            currentWaterLevel.add((short)0);

            //레이아웃 추가
            container.addView(listView);
        }

        //텍스트 찾기
        TextView userTitle = findViewById(R.id.userTitle);
        TextView curSfLv = findViewById(R.id.curSfLv);
        TextView danArea = findViewById(R.id.danArea);

        //제목 표시
        userTitle.setText(Objects.requireNonNull(DataBase.Users.get(MainActivity.GetCurrentUser())).UserTitle);

        //기능 수행
        running = true;
        Thread thread = new Thread(() -> {
            while (running){
                try {
                    handler.post(() -> {
                        short newLevel = 0;
                        //현재 안전레벨 확인
                        for (short i = 0; i < num; i++) {
                            if (MainActivity.GetWaterLevel(i) > 30) {
                                newLevel = 1;
                                break;
                            }
                            else if (MainActivity.GetWaterLevel(i) > 0) {
                                newLevel = 2;
                                break;
                            }
                        }
                        //현재 안전레벨 텍스트 업데이트
                        if (currentSafeLevel != newLevel) {
                            currentSafeLevel = newLevel;
                            curSfLv.setText(MainActivity.GetSafeLevelText());
                        }
                        danArea.setText(MainActivity.GetDangerousArea());

                        //경보기 리스트 수위 정보 업데이트
                        for (short i = 0; i < num; i++) {
                            if (!currentWaterLevel.get(i).equals(MainActivity.GetWaterLevel(i))) {
                                //수위 업데이트
                                currentWaterLevel.set(i, MainActivity.GetWaterLevel(i));
                                waterLevel.get(i).setText(String.format("- 수위: %scm", currentWaterLevel.get(i).toString()));

                                //현재상태 업데이트
                            }
                        }
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