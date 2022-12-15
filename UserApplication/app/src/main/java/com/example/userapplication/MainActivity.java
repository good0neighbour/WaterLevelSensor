package com.example.userapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.userapplication.database.DataBase;
import com.example.userapplication.database.DeviceInfo;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
    /*
    멤버변수를 선언하는 자리다.
    멤버변수란, 클래스 내에 선언된 변수를 말하고 영어로는 필드(Field)라 불린다. (함수 내에 선언된 변수는 지역변수라고 부른다.)
    참고로 멤버함수라 하면 영어로 메서드(Method)라고도 불리며 클래스 내에 선언된 함수를 말한다.

    public과 private은 접근제한자라 불리고, 외부 클래스에서 접근할 수 있을지 없을지 결정한다.
    static은 정적 메모리를 할당할 때 쓰인다.
    멤버변수는 기본적으로 전역변수 위치에 있는 변수를 참조해오는 형태인데,
    static 키워드를 붙이면 멤버변수를 전역변수화하게 되는 것이다.
    멤버변수는 객체마다 고유한 값을 가지게 되나, 정적변수는 모든 객체가 한 변수를 공용으로 사용하는 것으로 볼 수 있다.

    이 클래스의 이름은 MainActivity인데, 변수의 자료형으로 MainActivity를 사용한다고 해서 클래스 안에 같은 클래스를 두는 것은 아니다.
    Java는 C++와는 달리 포인터를 선언하지 않아도 클래스를 담는 변수는 '참조변수'가 된다.
    참조변수는 C계열 언어에서 포인터라 불리는 것과 동일하다.
    
    LinkedList는 그나마 한국어로는 연결리스트라고 불린다.
    여러개의 값을 저장한다는 점에서는 배열과 비슷하나,
    배열은 메모리 공간에서 일렬로 할당되는 탓에 길이가 정해져 있는 반면,
    연결리스트는 길이가 정해지지 않고 값을 저장하는 공간이 서로 떨어져있기 때문에 다음 인덱스를 가리키는 포인터를 추가로 가진다.
    때문에 값 하나를 가져오는데의 시간복잡도는, 배열은 O(1), 연결리스트는 O(n)으로 열결리스트가 다소 성능이 떨어질 수 있다.
    그러나 배열의 길이를 변경하는 경우,
    배열은 새로운 배열을 생성하고 기존 값을 복사해와야하는데 반해
    연결리스트는 얼마든 자유롭게 값을 추가, 삭제할 수 있다.
    게다가 기존 배열이 더이상 참조되지 않으면 가비지컬렉터가 작동하면서 많은 성능을 잡아먹기 때문에
    이 경우 연결리스트가 성능에 더 유리하다.

    LinkedList 뒤에 <> 괄호가 붙는 것은 이 클래스가 일반화되었음을 말한다.
    괄호 안에는 원하는 자료형을 넣어 해당 클래스를 그에 맞춰 사용할 수 있다.
    일반화란, 이처럼 클래스가 특정 자료형에 대해서만 정의되어있는 것이 아니라 모든 자료형에 대해서 사용 가능하도록 정의된 것을 말한다.
     */


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
    /*
    위 멤버함수는 오직 반환만 하는 함수다.
    접근제한자가 private이면 외부 클래스에서 참조할 수 없기 때문에
    이처럼 public 메서드를 통해 멤버변수의 값을 반환받는 것이다.
    멤버변수를 굳이 private으로 제한하는 이유는 개발 중 실수를 줄이기 위함이 가장 크다.
    혼자 개발하는 경우에는 모든 멤버를 public으로 해도 별 문제를 못 느낄 수 있으나,
    팀원과 협업하는 경우 본인이 선언한 변수에 대해서 다른 팀원이 어떤 용도인지 모르거나 변수 이름을 헷갈리는 등의 이유로
    잘못된 변수에 잘못된 값을 대입하는 경우가 발생할 수 있기 때문에
    필요한 경우가 아니면 멤버변수는 private으로 제한하는 것이 원칙이다.
     */

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
    /*
    waterLevel은 수위 값을 담기 위해 선언된 연결리스트다.
    MQTT 연결 시 waterLevel의 인덱스를 늘려줄 목적으로 0이라는 값을 추가한다.

    client 또한 연결리스트이기 때문에 add 함수로 클라이언트를 추가해준다.
    MqttClient는 클래스 이름이고 builder()는 정적 함수이기 때문에 객체 생성 없이 접근이 가능하다.
    builder()에서 객체를 생성하기 때문에 . 표시를 이용해 그 객체의 멤버함수에 접근할 수 있게된다.
    . 표시는 객체의 멤버에 접근할 때 사용한다.
    C++에서 . 표시는 객체에서 직접 멤버에 접근할 때만 쓰이고
    포인터 변수로 간접적으로 접근할 때는 -> 표시를 썼다.
    Java는 모든 클래스 변수가 참조변수이기 때문에 . 표시 하나로 통일되었다.
    따라서 builder() 뒤쪽으로 반환값을 가지는 멤버함수가 . 표시로 길게 늘어진 것을 볼 수 있다.
    보기 좋게 줄바꿈 되어있으나 이들 모두 한 줄에 있는 코드인 것이다.

    MQTT 브로커는 HiveMQ에서 무료로 제공하는 것을 사용했다.
    서버호스트와 포트는 HiveMQ가 제공하는 것을 입력하고 유저아이디, 비밀번호는 가입한 계정의 것을 입력한다.
    비밀번호는 Constants 클래스의 멤버변수 PASSWORD를 참조하고 있고,
    Constants 스크립트는 공개하지 않는다.
    
    MQTT 서버에 연결이 되면 저장된 토픽으로 구독을 시작한다.
     */

    /**
     * 경보기 정보 수정 시 호출
     */
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
    /*
    경보기 정보를 수정하면 토픽도 변경되기 때문에 구독 중이던 것을 모두 연결 해제하고 client와 waterLevel 리스트를 모두 삭제한다.
    그 후 수정된 모든 토픽으로 다시 구독을 시작한다.

    경보기 정보를 수정하는 기능은 MainActivity에 있지 않으나,
    clients에 담긴 객체에 접근하기 위해 이 함수를 MainActivity에 두었다.
     */

    /**
     * 제목 업데이트
     */
    public void UserTitleUpdate() {
        userTitle.setText(Objects.requireNonNull(DataBase.Users.get(currentUser)).UserTitle);
    }
    /*
    유저 편의를 위해 제목 텍스트를 표시해주는데,
    이 함수는 제목이 수정되면 호출될 목적이다.
    제목 수정하는 기능 또한 MainActivity에 없으나, userTitle에 접근하기 위해 이곳에 함수를 두었다.
     */

    /**
     * 저장된 데이터 불러오기
     * @return 경보기 정보 배열 반환
     */
    public DeviceInfo[] LoadJson() {
        try {
            //json 파일 불러오기
            InputStream is = getAssets().open("DataBase.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, UTF_8);

            //DeviceInfo 가져오기
            JSONArray deviceArray = new JSONObject(json).getJSONArray("DeviceInfo");

            //DeviceInfo 배열 생성
            short deviceLength = (short)deviceArray.length();
            DeviceInfo[] deviceInfo = new DeviceInfo[deviceLength];

            //DeviceInfo 배열 저장
            for (short j = 0; j < deviceLength; j++) {
                //json 오브젝트 생성
                JSONObject deviceObject = deviceArray.getJSONObject(j);

                //DeviceInfo 생성
                deviceInfo[j] = new DeviceInfo(deviceObject.getString("location"), deviceObject.getString("topic"));
            }

            //반환
            return deviceInfo;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    /*
    이 어플리케이션에서 저장할 데이터는 json 파일로 저장된다.
    이 함수는 json 파일에서 정보를 읽어와 DeviceInfo 객체를 생성한다.
     */

    public void SaveJson() {
        File saveFile = new File("/main/assets/.DataBase.json");
        JSONArray deviceArray = new JSONArray();
        try {
            for (short i = 0; i < Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).Devices.size(); i++) {
                JSONObject deviceObject = new JSONObject();
                deviceObject.put("topic", Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).Devices.get(i).Topic);
                deviceObject.put("location", Objects.requireNonNull(DataBase.Users.get(MainActivity.Instance.GetCurrentUser())).Devices.get(i).Location);
                deviceArray.put(deviceObject);
            }
            JSONObject deviceObject = new JSONObject();
            deviceObject.put("DeviceInfo", deviceArray);
            BufferedWriter bfw = new BufferedWriter(new FileWriter("/main/assets/.DataBase.json"));
            bfw.write(deviceArray.toString());
            bfw.flush();
            bfw.close();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
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
    /*
    함수 이름에서 보듯 MainActivity 생성 시 호출될 함수다.
    Override란, 다른 클래스에 있던 멤버함수를 다시 정의하는 것을 말하는데,
    여기서 다른 클래스는 부모 클래스를 말한다.
    이 스크립트에서 MainActivity 클래스의 가장 위쪽을 보면
    public class MainActivity extends AppCompatActivity
    라고 쓰인 것을 볼 수 있는데
    extends는 어떤 부모 클래스로부터 멤버변수, 멤버함수를 상속받겠다는 것이고
    그 부모 클래스는 AppCompatActivity다.
    따라서 onCreate는 AppCompatActivity에 선언된 함수고
    MainActivity에서는 이 함수를 오버라이드하여 사용한다.

    protected라는 키워드는 private, public과 마잔가지로 접근제한자다.
    protected로 선언된 것은 기본적으로 private처럼 외부 클래스에서 접근하는 것을 차단하나
    자식 클래스에서는 public처럼 접근을 가능하게 한다.

    super은 부모 클래스에 접근할 때 사용한다.
    부모 클래스에 접근해서 onCreate 함수를 호출하므로
    부모 클래스의 onCreate 함수의 기능을 그대로 수행하되
    MainActivity에서 새로운 기능을 추가하는 것으로 오버라이드 한다.

    setContentView는 레이아웃을 불러온다.
    activity_connect는 처음 로그인 화면의 레이아웃 파일 이름이다.

    Instance는 멤버변수 선언부에서 static 변수로 선언했다.
    this는 이 MainActivity 객체의 주소값을 넣어준다.
    Instance은 public이기 때문에 외부 스크립트에서 이 MainActivity 객체의 주소에 접근할 수 있다.
    이런 방식을 싱글턴 패턴이라고 하는데,
    싱글턴 패턴은 반드시 해당 클래스의 객체가 하나만 있을 때 써야 한다.

    ConnectScreenSetting은 아래에 정의할 함수다.
    로그인 화면 초기화 용도의 함수다.
     */


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
    /*
    DataBaseInitializing는 DataBase 클래스 안에서 static 함수로 선언됐다.
    DataBase 클래스는 실제 데이터베이스를 대체하기 위해 만든 클래스다.
    
    findViewById 함수는 레이아웃 파일에서 해당 id를 가지고 있는 요소를 찾을 수 있다.
    찾은 요소는 모두 지역변수에 저장한다.

    setOnClickListener는 해당 버튼이 클릭됐을 때 호출할 함수를 설정한다.
    괄호 안 인자로 전달할 것은 함수인데,
    위와 같은 형태의 함수는 이름이 없는 익명 함수로, 이러한 표현 방식을 람다식 표현이라고 한다.
    익명함수는 함수를 인자로 전달할 경우, 함수의 용도가 그것밖에 없을 때 사용한다.

    사용자가 연결 버튼을 누르면 가장 먼저 사용자가 입력한 아이디를 문자열로 가져온다.
    아이디 확인은 해쉬테이블을 통해 한다.
    해쉬테이블은 여러 값을 저장한다는 점에서 배열과 비슷하나,
    배열은 인덱스 번호로 검색하는 반면 해쉬테이블은 어떤 값으로 검색할 수 있다.
    검색하는 데에 사용하는 값을 키값이라고 부르고
    여기서는 사용자 아이디를 키값으로 사용했다.
    containsKey는 이 키값이 존재하는지 확인해준다.
    이것이 참이면 해쉬테이블을 사용자 아이디로 검색해서 나온 데이터를 대조한다.
    비밀번호가 일치하면 로그인이 가능하므로 Connecting 함수를 호출한다.
    Connecting 함수는 아래에 정의된 함수다.
     */

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
    /*
    핸들러는 프로그램의 흐름을 제어해준다.
    처음 코딩 강의를 들으면 사람들은 콘솔 응용프로그램을 만들게 되는데,
    콘솔 응용프로그램을 실행하면 운영체제는 하드웨어 제어 권한을 그 응용 프로그램에게 준다.
    반면 일반적인 어플리케이션의 경우, 응용 프로그램이 직접 하드웨어를 제어하기보다 핸들러가 주로 제어한다.

    사용자 정보에 입력된 경보기 정보의 개수만큼 구독 함수를 반복 호출한다.
    그 후 주 화면으로 전환시켜줄 MainScreen 함수를 호출한다.
    MainScreen 함수는 아래에 정의됐다.
     */

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
    /*
    MQTT 브로커로부터 값을 받았을 때의 동작을 설정해줄 SetOnReceive 함수는 아래에 정의됐다.
    
    기능 수행 부분은 일정한 주기로 사용자 인터페이스를 업데이트하는 기능으로, Thread를 통해 수행한다.
    여기서 업데이트하는 사용자 인터페이스는 모두 텍스트 형식인데,
    문자열 생성을 최대한 적게 할 수 있도록 모두 조건문을 붙였다.
    문자열을 담는 String은 값을 담는 값 형식 char의 배열이다.
    다시 말해, String은 char의 배열을 만드는 클래스로, 참조 형식이다.
    String 변수에 새로운 문자열을 대입할 때마다 새로운 문자열을 생성한 후 그 문자열을 String 변수가 참조하고,
    기존 문자열은 더이상 참조되지 않은 채 메모리에 남게 된다.
    그렇게 참조되지 않고 남은 문자열은 가비지컬렉터에 의해 제거되는데,
    그 빈도가 증가할 수록 성능에 악영향을 준다.
    따라서 업데이트 조건을 확인해줄 값 형식의 변수를 추가해 불필요한 문자열 생성을 줄였다.
     */

    private void SetOnReceive() {
        for (short i = 0; i < clients.size(); i++) {
            short finalI = i;
            clients.get(i).toAsync().publishes(
                    ALL,
                    publish -> waterLevel.set(finalI, Short.parseShort(UTF_8.decode(publish.getPayload().get()).toString()))
            );
        }
    }
    /*
    MQTT 브로커로부터 값을 받았을 때의 동작을 설정해준다.
    브로커로부터 받을 값은 수위 정보이므로
    waterLevel 리스트의 해당 인덱스에 그 값을 정수로 치환하여 저장한다.
    이 코드는 가능한 오류가 적어지도록 작성된 형태다.
     */
}