package com.example.userapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.example.userapplication.database.DataBase;
import java.util.Objects;

public class AdditionalInfomationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_infomation);

        //텍스트, 버튼 찾기
        TextView userTitle = findViewById(R.id.userTitle);

        //제목 표시
        userTitle.setText(Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).UserTitle);
    }
}