package com.example.userapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.example.userapplication.database.DataBase;
import java.util.LinkedList;
import java.util.Objects;

public class ModifyActivity extends AppCompatActivity {
    private LinkedList<EditText> topics = new LinkedList<>();
    private LinkedList<EditText> locations = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);

        View listView;
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        LinearLayout container = findViewById(R.id.container);

        //입력칸, 버튼 찾기
        EditText userTitle = findViewById(R.id.userTitle);
        Button adoptBtn = findViewById(R.id.adoptBtn);

        //제목 표시
        userTitle.setText(Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).UserTitle);

        //경보기 리스트
        for (short i = 0; i < Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).Devices.size(); i++) {
            listView = layoutInflater.inflate(R.layout.addition_sensor_info, null, false);

            //텍스트 정보 추가
            topics.add(listView.findViewById(R.id.topic));
            topics.get(i).setText(Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).Devices.get(i).Topic);
            locations.add(listView.findViewById(R.id.location));
            locations.get(i).setText(Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).Devices.get(i).Location);

            //레이아웃 추가
            container.addView(listView);
        }

        //적용 버튼
        adoptBtn.setOnClickListener(view -> {
            Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).UserTitle = userTitle.getText().toString();
            for (short i = 0; i < topics.size(); i++) {
                Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).Devices.get(i).Topic = topics.get(i).getText().toString();
                Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).Devices.get(i).Location = locations.get(i).getText().toString();
            }
            ManagementActivity.Instance.TextMessage("경보기가 수정됐습니다.");
            MainActivity.Instance.UserTitleUpdate();
            ManagementActivity.Instance.UserTitleUpdate();
            MainActivity.Instance.Reconnect();
            finish();
        });
    }
}