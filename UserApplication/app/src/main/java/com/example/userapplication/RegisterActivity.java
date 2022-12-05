package com.example.userapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.userapplication.database.DataBase;
import com.example.userapplication.database.DeviceInfo;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        //입력칸, 버튼 찾기
        TextView userTitle = findViewById(R.id.userTitle);
        EditText topic = findViewById(R.id.topic);
        EditText location = findViewById(R.id.location);
        Button registerBtn = findViewById(R.id.registerBtn);

        //제목 표시
        userTitle.setText(Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).UserTitle);

        //등록 버튼
        registerBtn.setOnClickListener(view -> {
            Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).Devices.add(new DeviceInfo(location.getText().toString(), topic.getText().toString()));
            MainActivity.Instance.Subscribe(MainActivity.Instance.GetClientSize());
            ManagementActivity.Instance.TextMessage("경보기가 등록됐습니다.");
            finish();
        });
    }
}