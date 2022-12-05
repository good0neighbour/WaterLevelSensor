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
    private final LinkedList<Short> currentSafeLevel = new LinkedList<>();
    private short currentSafeLevelGlobal = 0;
    private short currentDanArea = 0;
    private short num = 0;
    private short timer = 0;
    private boolean running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_status);

        View listView;
        TextView tmpTV;
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        LinearLayout container = findViewById(R.id.currentStatus);

        //텍스트 찾기
        TextView userTitle = findViewById(R.id.userTitle);
        TextView curSfLv = findViewById(R.id.curSfLv);
        TextView danArea = findViewById(R.id.danArea);

        //제목 표시
        userTitle.setText(Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).UserTitle);

        //등록한 경보기 개수
        num = (short)Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).Devices.size();

        //경보기 리스트
        for (short i = 0; i < num; i++) {
            listView = layoutInflater.inflate(R.layout.addtion_sensor_status, null, false);

            //텍스트 정보 추가
            tmpTV = listView.findViewById(R.id.name);
            tmpTV.setText(Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).Devices.get(i).Topic);
            tmpTV = listView.findViewById(R.id.location);
            tmpTV.setText(String.format(
                    "- 위치: %s",
                    Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).Devices.get(i).Location)
                    );

            //텍스트 찾기
            currentStatus.add(listView.findViewById(R.id.status));
            waterLevel.add(listView.findViewById(R.id.waterLevel));
            currentWaterLevel.add((short)0);
            currentSafeLevel.add((short)0);

            //레이아웃 추가
            container.addView(listView);
        }

        //기능 수행
        running = true;
        Thread thread = new Thread(() -> {
            while (running){
                try {
                    handler.post(() -> {
                        //현재 안전레벨 텍스트 업데이트
                        if (MainActivity.Instance.GetSafeLevelPhase() != currentSafeLevelGlobal) {
                            currentSafeLevelGlobal = MainActivity.Instance.GetSafeLevelPhase();
                            curSfLv.setText(MainActivity.Instance.GetSafeLevelText());
                        }

                        //위험구역 텍스트 업데이트
                        if (MainActivity.Instance.GetDangerousAreaNum() != currentDanArea) {
                            currentDanArea = MainActivity.Instance.GetDangerousAreaNum();
                            danArea.setText(MainActivity.Instance.GetDangerousArea());
                        }

                        //경보기 리스트 수위 정보 업데이트
                        for (short i = 0; i < num; i++) {
                            short newLevel = 0;

                            //현재상태 업데이트
                            if (MainActivity.Instance.GetWaterLevel(i) > 2) {
                                if (timer > 10) {
                                    newLevel = 2;
                                }
                                else {
                                    timer++;
                                    newLevel = 1;
                                }
                            }
                            else if (MainActivity.Instance.GetWaterLevel(i) > 0) {
                                if (newLevel == 0) {
                                    newLevel = 1;
                                }
                            }
                            if (currentSafeLevel.get(i) != newLevel) {
                                currentSafeLevel.set(i, newLevel);
                                switch (currentSafeLevel.get(i)) {
                                    case 0:
                                        currentStatus.get(i).setText("- 현재상태: 안전");
                                        timer = 0;
                                        break;
                                    case 1:
                                        currentStatus.get(i).setText("- 현재상태: 위험");
                                        break;
                                    case 2:
                                        currentStatus.get(i).setText("- 현재상태: 대피");
                                        break;
                                    default:
                                        break;
                                }
                            }

                            //수위 업데이트
                            if (!currentWaterLevel.get(i).equals(MainActivity.Instance.GetWaterLevel(i))) {
                                currentWaterLevel.set(i, MainActivity.Instance.GetWaterLevel(i));
                                waterLevel.get(i).setText(String.format("- 수위: %scm", currentWaterLevel.get(i).toString()));
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

    @Override
    public void onBackPressed() {
        running = false;
        super.onBackPressed();
    }
}