package com.example.userapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;

public class MainActivity extends AppCompatActivity {
    public static String waterLevelText;
    //public static String safetyLevelText;
    private Mqtt5BlockingClient client;
    private final Handler handler = new Handler();
    private String newData = "0";
    private String currentData = "0";
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
        //입력칸, 연결 버튼 찾기
        EditText serialText = findViewById(R.id.serialText);
        Button connectBtn = findViewById(R.id.connectBtn);
        TextView statusText = findViewById(R.id.statusText);

        //버튼 클릭 시
        connectBtn.setOnClickListener(view -> {
            //입력한 시리얼 번호 가져오기
            String topic = serialText.getText().toString();

            //시리얼 번호를 입력했을 경우
            if (topic.length() > 0) {
                Connecting(statusText, topic);
            }
            //시리얼 번호를 입력하지 않았을 경우
            else {
                statusText.setText("제품 시리얼 번호를 입력하십시오.");
            }
        });
    }

    /**
     * MQTT 연결
     */
    private void Connecting(TextView statusText, String topic){
        try {
            //Mqtt 클라이언트 객체 생성
            client = MqttClient.builder()
                    .useMqttVersion5()
                    .serverHost("aef73941920445ed92ff3ff57355d371.s2.eu.hivemq.cloud")
                    .serverPort(8883)
                    .sslWithDefaultConfig()
                    .buildBlocking();

            //HiveMQ Cloud 연결
            client.connectWith()
                    .simpleAuth()
                    .username("good_neighbour")
                    .password(UTF_8.encode(Constants.PASSWORD))
                    .applySimpleAuth()
                    .send();

            //구독
            client.subscribeWith()
                    .topicFilter("WtLvSn/" + topic)
                    .send();

            //상태 텍스트 초기화
            statusText.setText(null);
            
            //주 화면으로 전환
            MainScreen(topic);
        }
        catch (Exception e) {
            statusText.setText("연결 실패. 네트워크 상태 확인 요망.");
        }
    }

    /**
     * 주 화면 불러오기
     */
    private void MainScreen(String topic){
        //주 화면 불러오기
        setContentView(R.layout.activity_main);

        //시리얼 번호, 수위 표시용 텍스트 찾기
        TextView backBtn = findViewById(R.id.backBtn);
        TextView currDevice = findViewById(R.id.currDevice);
        //TextView safetyLevel = findViewById(R.id.safetyLevel);
        TextView waterLevel = findViewById(R.id.waterLevel);
        Button currStatus = findViewById(R.id.currStatus);
        Button addition = findViewById(R.id.addition);
        Button information = findViewById(R.id.information);
        String deviceText = "제품 시리얼 번호와 일치하는지 확인 바랍니다.\n" + topic;

        //사용자가 입력한 시리얼 번호 표시
        currDevice.setText(deviceText);
        
        //뒤로 가기 버튼
        backBtn.setOnClickListener(view -> {
            running = false;
            client.disconnect();
            newData = "0";
            currentData = "0";
            setContentView(R.layout.activity_connect);
            ConnectScreenSetting();
        });
        
        //현재상황 버튼
        currStatus.setOnClickListener(
            view -> startActivity(new Intent(MainActivity.this, CurrentStatus.class))
        );
        
        //추가정보기입 버튼
        addition.setOnClickListener(
            view -> startActivity(new Intent(MainActivity.this, AdditionalInformation.class))
        );

        //기타정보 버튼
        information.setOnClickListener(
            view -> startActivity(new Intent(MainActivity.this, ExtraInformation.class))
        );

        //값 받았을 때
        client.toAsync().publishes(ALL, publish -> {
            newData = UTF_8.decode(publish.getPayload().get()).toString();
            System.out.println(newData);
        });

        //수위 표시 업데이트
        running = true;
        Thread thread = new Thread(() -> {
            while (running){
                try {
                    if (!newData.equals(currentData)){
                        currentData = newData;
                        waterLevelText = currentData + "cm";
                        handler.post(() -> waterLevel.setText(waterLevelText));
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}