package com.example.userapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.userapplication.database.DataBase;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import java.util.LinkedList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    /*==================== Variables ====================*/

    public static MainActivity Instance;
    private final LinkedList<Mqtt5BlockingClient> clients = new LinkedList<>();
    private final LinkedList<Short> waterLevel = new LinkedList<>();
    private TextView userTitle;
    private String currentUser;
    private String safeLevelText = "현재 안전레벨: 안전";
    private String dangerousArea = "위험 구역: 없음";
    private short currentSafeLevel = 0;
    private short currentDanArea = -1;
    private final Handler handler = new Handler();
    private short timer = 0;
    private boolean running = true;


    /*==================== Public Methods ====================*/

    /**
     * 클라이언트 가져오기
     * @return 클라이언트 리스트 길이 반환
     */
    public short GetClientSize() {
        return (short)clients.size();
    }

    /**
     * 현재 수위 가져오기
     * @param index 인덱스 번호
     * @return 해당 인덱스 수위 반환
     */
    public Short GetWaterLevel(short index) {
        return waterLevel.get(index);
    }

    /**
     * 현재 사용자 이름 가져오기
     * @return 사용자 이름 반환
     */
    public String GetCurrentUser() {
        return currentUser;
    }

    /**
     * 현재 안전레벨 가져오기
     * @return 안전레벨 텍스트 반환
     */
    public String GetSafeLevelText() {
        return safeLevelText;
    }

    /**
     * 현재 위험구역 가져오기
     * @return 위험구역 텍스트 반환
     */
    public String GetDangerousArea() {
        return dangerousArea;
    }

    /**
     * 현재 안전레벨 가져오기
     * @return 안전레벨 숫자로 반환
     */
    public short GetSafeLevelPhase() {
        return currentSafeLevel;
    }

    /**
     * 현재 위험구역 가져오기
     * @return 위험구역 숫자로 반환
     */
    public short GetDangerousAreaNum() {
        return currentDanArea;
    }

    /**
     * MQTT 연결 및 구독
     * @param index 새 클라이언트 인덱스
     */
    public void Subscribe(short index) {
        //수위 정보 리스트 추가
        waterLevel.add((short)0);

        //Mqtt 클라이언트 객체 생성
        clients.add(MqttClient.builder()
                .useMqttVersion5()
                .serverHost("1befe1d1899b49688347a6c39ec340ea.s2.eu.hivemq.cloud")
                //.serverHost("aef73941920445ed92ff3ff57355d371.s2.eu.hivemq.cloud")
                .serverPort(8883)
                .sslWithDefaultConfig()
                .buildBlocking()
        );

        //HiveMQ Cloud 연결
        clients.get(index).connectWith()
                .simpleAuth()
                .username("amor2022")
                //.username("good_neighbour")
                .password(UTF_8.encode(Constants.PASSWORD))
                .applySimpleAuth()
                .send();

        //구독
        clients.get(index).subscribeWith()
                .topicFilter("WtLvSn/" + Objects.requireNonNull(DataBase.Users.get(currentUser)).Devices.get(index).Topic)
                .send();
    }

    public void Reconnect() {
        for (short i = 0; i < clients.size(); i++) {
            clients.get(i).disconnect();
        }
        clients.clear();
        waterLevel.clear();
        for (short i = 0; i < Objects.requireNonNull(DataBase.Users.get(currentUser)).Devices.size(); i++) {
            Subscribe(i);
        }
        SetOnReceive();
    }

    public void UserTitleUpdate() {
        userTitle.setText(Objects.requireNonNull(DataBase.Users.get(currentUser)).UserTitle);
    }


    /*==================== Protected Methods ====================*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //연결 화면 불러오기
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        //싱글턴 패턴
        Instance = this;

        //연결 화면 초기화
        ConnectScreenSetting();
    }


    /*==================== Private Methods ====================*/

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
                    Connecting(statusText);
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
    private void Connecting(TextView statusText){
        try {
            handler.postDelayed(() -> {
                //MQTT 연결 및 구독
                for (short i = 0; i < Objects.requireNonNull(DataBase.Users.get(currentUser)).Devices.size(); i++) {
                    Subscribe(i);
                }

                //상태 텍스트 초기화
                statusText.setText(null);

                //주 화면으로 전환
                MainScreen();
            }, 1000);
        }
        catch (Exception e) {
            statusText.setText("연결 실패. 네트워크 상태 확인 요망.");
        }
    }

    /**
     * 주 화면 불러오기
     */
    private void MainScreen(){
        //주 화면 불러오기
        setContentView(R.layout.activity_main);

        //텍스트, 버튼 찾기
        userTitle = findViewById(R.id.userTitle);
        TextView backBtn = findViewById(R.id.backBtn);
        TextView curSfLv = findViewById(R.id.curSfLv);
        TextView danArea = findViewById(R.id.danArea);
        Button currStatus = findViewById(R.id.currStatus);
        Button management = findViewById(R.id.management);
        Button additional = findViewById(R.id.additional);

        //제목 표시
        UserTitleUpdate();
        
        //뒤로 가기 버튼
        backBtn.setOnClickListener(view -> {
            running = false;
            setContentView(R.layout.activity_connect);
            ConnectScreenSetting();
            for (short i = 0; i < clients.size(); i++) {
                clients.get(i).disconnect();
            }
            waterLevel.clear();
            clients.clear();
        });
        
        //안전진단 버튼
        currStatus.setOnClickListener(
            view -> startActivity(new Intent(MainActivity.this, CurrentStatusActivity.class))
        );
        
        //제품 관리 버튼
        management.setOnClickListener(
            view -> startActivity(new Intent(MainActivity.this, ManagementActivity.class))
        );

        //추가 정보 버튼
        additional.setOnClickListener(
            view -> startActivity(new Intent(MainActivity.this, AdditionalInfomationActivity.class))
        );

        //값 받았을 때
        SetOnReceive();

        //기능 수행
        running = true;
        Thread thread = new Thread(() -> {
            while (running){
                try {
                    handler.post(() -> {
                        short newLevel = 0;
                        short maxLevel = 0;
                        short area = -1;

                        //현재 안전레벨 확인
                        for (short i = 0; i < waterLevel.size(); i++) {
                            if (waterLevel.get(i) > 2) {
                                if (timer > 10) {
                                    newLevel = 2;
                                }
                                else {
                                    timer++;
                                    newLevel = 1;
                                }
                            }
                            else if (waterLevel.get(i) > 0) {
                                if (newLevel == 0) {
                                    newLevel = 1;
                                }
                            }
                            if (waterLevel.get(i) > maxLevel) {
                                maxLevel = waterLevel.get(i);
                                area = i;
                            }
                        }
                        //현재 안전레벨 텍스트 업데이트
                        if (currentSafeLevel != newLevel) {
                            currentSafeLevel = newLevel;
                            switch (currentSafeLevel) {
                                case 0:
                                    safeLevelText = "현재 안전레벨: 안전";
                                    break;
                                case 1:
                                    safeLevelText = "현재 안전레벨: 위험";
                                    break;
                                case 2:
                                    safeLevelText = "현재 안전레벨: 대피";
                                    break;
                                default:
                                    break;
                            }
                        }
                        //텍스트 업데이트 실패 가능성이 있으므로 반복 확인 필요
                        if (!curSfLv.getText().equals(safeLevelText)) {
                            curSfLv.setText(safeLevelText);
                        }

                        //위험구역 업데이트
                        if (currentDanArea != area) {
                            currentDanArea = area;
                            if (currentDanArea > -1) {
                                dangerousArea = String.format("위험 구역: %s", Objects.requireNonNull(DataBase.Users.get(currentUser)).Devices.get(currentDanArea).Location);
                            }
                            else {
                                dangerousArea = "위험 구역: 없음";
                                timer = 0;
                            }
                        }
                        //텍스트 업데이트 실패 가능성이 있으므로 반복 확인 필요
                        if (!danArea.getText().equals(dangerousArea)) {
                            danArea.setText(dangerousArea);
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

    private void SetOnReceive() {
        for (short i = 0; i < clients.size(); i++) {
            short finalI = i;
            clients.get(i).toAsync().publishes(
                    ALL,
                    publish -> waterLevel.set(finalI, Short.parseShort(UTF_8.decode(publish.getPayload().get()).toString()))
            );
        }
    }
}