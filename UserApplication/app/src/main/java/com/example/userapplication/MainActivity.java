package com.example.userapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.userapplication.database.DataBase;
import com.example.userapplication.database.UserTable;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import java.util.LinkedList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static short WaterLevel = 0;
    private final Handler handler = new Handler();
    private final LinkedList<Mqtt5BlockingClient> clients = new LinkedList<>();
    private String currentUser;
    private boolean running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //연결 화면 불러오기
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        //연결 화면 초기화
        ConnectScreenSetting();

        //저장된 시리얼 번호가 있을 때
        //if (false){
        //    Connecting(statusText, serialText, currDevice);
        //}
    }

    /**
     * 연결 화면 초기화
     */
    private void ConnectScreenSetting(){
        //데이터베이스 초기화
        DataBase.DataBaseInitializing();

        //입력칸, 연결 버튼 찾기
        EditText userId = findViewById(R.id.userId);
        EditText passWord = findViewById(R.id.passWord);
        Button connectBtn = findViewById(R.id.connectBtn);
        TextView statusText = findViewById(R.id.statusText);

        //버튼 클릭 시
        connectBtn.setOnClickListener(view -> {
            //사용자가 입력한 아이디
            currentUser = userId.getText().toString();
            
            //존재하는 아이디인 경우
            if (DataBase.Users.containsKey(currentUser)) {
                //비밀번호 일치할 경우
                if (passWord.getText().toString().equals(Objects.requireNonNull(DataBase.Users.get(currentUser)).Password)) {
                    statusText.setText("연결 중.");
                    Connecting(statusText, Objects.requireNonNull(DataBase.Users.get(currentUser)).Devices);
                }
                //비밀번호가 틀릴 경우
                else {
                    statusText.setText("잘못된 비밀번호입니다.");
                }
            }
            //없는 아이디인 경우
            else {
                statusText.setText("존재하지 않는 아이디입니다.");
            }
        });
    }

    /**
     * MQTT 연결
     */
    private void Connecting(TextView statusText, LinkedList<String> topic){
        try {
            handler.postDelayed(() -> {
                for (short i = 0; i < topic.size(); i++) {
                    //Mqtt 클라이언트 객체 생성
                    clients.add(MqttClient.builder()
                            .useMqttVersion5()
                            .serverHost("1befe1d1899b49688347a6c39ec340ea.s2.eu.hivemq.cloud")
                            .serverPort(8883)
                            .sslWithDefaultConfig()
                            .buildBlocking()
                    );

                    //HiveMQ Cloud 연결
                    clients.get(i).connectWith()
                            .simpleAuth()
                            .username("amor2022")
                            .password(UTF_8.encode(Constants.PASSWORD))
                            .applySimpleAuth()
                            .send();

                    //구독
                    clients.get(i).subscribeWith()
                            .topicFilter("WtLvSn/" + topic.get(i))
                            .send();

                    //상태 텍스트 초기화
                    statusText.setText(null);

                    //주 화면으로 전환
                    MainScreen(topic);
                }
            }, 1000);
        }
        catch (Exception e) {
            statusText.setText("연결 실패. 네트워크 상태 확인 요망.");
        }
    }

    /**
     * 주 화면 불러오기
     */
    private void MainScreen(LinkedList<String> topic){
        //주 화면 불러오기
        setContentView(R.layout.activity_main);

        //텍스트, 버튼 찾기
        TextView backBtn = findViewById(R.id.backBtn);
        TextView userTitle = findViewById(R.id.userTitle);
        TextView curSfLv = findViewById(R.id.curSfLv);
        TextView danArea = findViewById(R.id.danArea);
        Button currStatus = findViewById(R.id.currStatus);
        Button management = findViewById(R.id.management);
        Button additional = findViewById(R.id.additional);

        //제목 표시
        userTitle.setText(Objects.requireNonNull(DataBase.Users.get(currentUser)).UserTitle);
        
        //뒤로 가기 버튼
        backBtn.setOnClickListener(view -> {
            running = false;
            WaterLevel = 0;
            setContentView(R.layout.activity_connect);
            ConnectScreenSetting();
            for (short i = 0; i < clients.size(); i++) {
                clients.get(i).disconnect();
            }
        });
        
        //안전진단 버튼
        currStatus.setOnClickListener(
            view -> startActivity(new Intent(MainActivity.this, CurrentStatus.class))
        );
        
        //제품 관리 버튼
        management.setOnClickListener(
            view -> startActivity(new Intent(MainActivity.this, AdditionalInformation.class))
        );

        //추가 정보 버튼
        additional.setOnClickListener(
            view -> startActivity(new Intent(MainActivity.this, ExtraInformation.class))
        );

        //값 받았을 때
        for (short i = 0; i < clients.size(); i++) {
            clients.get(i).toAsync().publishes(ALL, publish -> {
                WaterLevel = Short.parseShort(UTF_8.decode(publish.getPayload().get()).toString());
            });
        }

        //기능 수행
        running = true;
        Thread thread = new Thread(() -> {
            while (running){
                try {
                    handler.post(() -> {
                        //현재 안전레벨 업데이트
                        if (WaterLevel > 30) {
                            curSfLv.setText("현재 안전레벨: 대피");
                        }
                        else if (WaterLevel > 0) {
                            curSfLv.setText("현재 안전레벨: 위험");
                        }
                        else {
                            curSfLv.setText("현재 안전레벨: 안전");
                        }

                        //위험구역 업데이트

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