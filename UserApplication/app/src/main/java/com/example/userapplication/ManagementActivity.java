package com.example.userapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.userapplication.database.DataBase;
import java.util.Objects;

public class ManagementActivity extends AppCompatActivity {
    public static ManagementActivity Instance;
    private TextView userTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);

        //싱글턴 패턴
        Instance = this;

        //텍스트, 버튼 찾기
        userTitle = findViewById(R.id.userTitle);
        Button register = findViewById(R.id.register);
        Button modify = findViewById(R.id.modify);
        Button delete = findViewById(R.id.delete);

        //제목 표시
        UserTitleUpdate();

        //제품 등록 버튼
        register.setOnClickListener(
                view -> startActivity(new Intent(Instance, RegisterActivity.class))
        );

        //제품 수정 버튼
        modify.setOnClickListener(
                view -> startActivity(new Intent(Instance, ModifyActivity.class))
        );
    }

    public void TextMessage(String message) {
        Toast.makeText(Instance, message, Toast.LENGTH_LONG).show();
    }

    public void UserTitleUpdate() {
        userTitle.setText(Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).UserTitle);
    }
}