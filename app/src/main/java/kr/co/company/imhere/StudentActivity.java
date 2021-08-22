package kr.co.company.imhere;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.nio.charset.*;
import java.util.*;
import android.nfc.*;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.os.*;
import android.view.View;
import android.view.Window;
import android.widget.*;
import android.content.Intent;

//데이터를 보내는 쪽이 학생, 수강자
//휴대폰에 등록되있는 수강자의 데이터를 NDEF 메세지로 전송
/*--------------------------경란------------------------------*/
public class StudentActivity extends AppCompatActivity
        implements CreateNdefMessageCallback, OnNdefPushCompleteCallback{
    NfcAdapter mNfcAdapter = null; // NFC 어댑터

    String userID;
    String userCheck;
    private String course_id;
    private String sec_id;
    private String semester;
    private String year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //액션바를 없애는 기능
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        //인텐트로 넘어온 데이터를 받아옴
        Intent itt = getIntent();
        userID = itt.getStringExtra("userID");
        userCheck = itt.getStringExtra("userCheck");
        course_id = itt.getStringExtra("course_id");
        sec_id = itt.getStringExtra("sec_id");
        semester = itt.getStringExtra("semester");
        year = itt.getStringExtra("year");

        //수강자(student)로 로그인 했을 경우만 '보내기' 가능
        if(userCheck.equals("student")) {
            // NFC 어댑터를 구한다
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

            // NFC 어댑터가 null 이라면 칩이 존재하지 않는 것으로 간주
        if( mNfcAdapter != null )
            Toast.makeText(StudentActivity.this, "NFC 기능이 준비되었습니다.", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(StudentActivity.this, "NFC 칩이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();

            //이벤트 등록
            //NDEF 메시지 생성 & 전송을 위한 콜백 함수 설정
            //2대의 NFC 단말이 서로를 인식했을 때 화면의 사이즈가 줄어드는데 이때 사용자가 화면을 터치하면 createNdefMessage() 라는 이벤트 함수가 실행
            //NDEF 푸쉬 메시지 생성 이벤트 함수이며, 이 함수에서 NDEF 메시지를 생성해서 반환하면 상대편 NFC 디바이스로 전달
            mNfcAdapter.setNdefPushMessageCallback(this, this);
            // NDEF 메시지 전송 완료 이벤트 콜백 함수 설정
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);

        }
//        Button button = (Button)findViewById(R.id.attendance);
//        button.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(StudentActivity.this, StudentAttenCheckActivity.class);
//                intent.putExtra("userID",  userID);
//                intent.putExtra("userCheck", userCheck);
//
//                startActivity(intent);
//            }
//            });
    }


    // NDEF 메시지 생성 이벤트 함수
    // 상대의 휴대폰으로 보낼 메세지를 이 함수에서 작성한다.
    // setNdefPushMessageCallback리스너의 함수

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        // 여러개의 NDEF 레코드를 모아서 하나의 NDEF 메시지를 생성
        NdefMessage message = new NdefMessage( new NdefRecord[] {
                //학생 ID를 보낸다.
                createTextRecord("ID=" + userID, Locale.KOREAN)

//                createTextRecord("이것은 Sender가 보내는 메세지 입니다.", Locale.KOREAN),
//                createTextRecord("이것은 Sender가 보내는 메세지 입니다.", Locale.KOREAN)

//                //텍스트
//                //Locale은 설정언어 값. 해당 언어로 인코딩
//                createTextRecord("Text sample record-1", Locale.ENGLISH),
//                createTextRecord("한국어 sample record-2", Locale.KOREAN),
//                //URI
//                createUriRecord("www.google.com"),
//                createUriRecord("cafe.naver.com/tizenity")
        });
        return message;
    }

    // 텍스트 형식의 레코드를 생성
    // 상대의 휴대폰으로 보낼 Text 메세지를 이 함수에서 작성한다.
    public NdefRecord createTextRecord(String text, Locale locale) {
        // 텍스트 데이터를 인코딩해서 byte 배열로 변환
        byte[] data = byteEncoding(text, locale); //byteEncoding함수호출
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    // 텍스트 데이터를 인코딩해서 byte 배열(byte[])로 변환
    public byte[] byteEncoding(String text, Locale locale) {
        // 언어 지정 코드 생성
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        // 인코딩 형식 생성
        Charset utfEncoding = Charset.forName("UTF-8");
        // 텍스트를 byte 배열로 변환
        byte[] textBytes = text.getBytes(utfEncoding);

        // 전송할 버퍼 생성
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte)langBytes.length;
        // 버퍼에 언어 코드 저장
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        // 버퍼에 데이터 저장
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        return data;
    }



    /*
    URL 은 웹 상에서 서비스를 제공하는 파일들의 위치를 표시하기 위한 홈페이지 주소에 해당하는 것이고,
    Uri 는 컨텐트 프로바이더(Content Provider) 의 접근규칙, 즉 컨텐트 프로바이더의 주소이다.

    Uri 는 홈페이지 주소 뿐만 아니라, 파일의 경로 ( 폰으로 치자면, SD 카드 내의 폴더에 접근 할 수도 있다.),
    데이터 베이스로의 접근 등을 값으로 가질 수 있다. 즉 Uri 가 URL 을 포함하는 상위 개념
    외부의 접근을 대신해서 맡아주는 것이 바로 컨텐트 프로바이더

    Uri 는 일반적으로 content://Authority/Path
    content:// 로 시작한다. 여기서 http:// 혹은 content:// 부분을 스키마(Scheme)

    Authority 는 컨텐트 프로바이더의 고유 주소, 다른 앱과 차별화 되는 고유한 이름
    http://www.google.co.kr 중에 www.google.co.kr 에 해당하는 부분

    Path 는 해당 프로바이더 내에 있는 리소스의 위치
    http://developer.android.com/develop/index.html 에서 스키마와 Authority 로 접근 후 찾아갈 수 있는 위치에 해당하는 /develop/index.html 부분
    */

    // URI 형식의 레코드를 생성
    // 상대의 휴대폰으로 보낼 URI 메세지를 이 함수에서 작성한다.
    public NdefRecord createUriRecord(String url) {
        // URI 경로를 byte 배열로 변환할 때 US-ACSII 형식으로 지정
        byte[] uriField = url.getBytes(Charset.forName("US-ASCII"));
        // URL 경로를 의미하는 1 을 첫번째 byte 에 추가
        byte[] payload = new byte[uriField.length + 1];
        payload[0] = 0x01;
        System.arraycopy(uriField, 0, payload, 1, uriField.length);
        // NDEF 레코드를 생성해서 반환
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[0], payload);
    }

    // NDEF 메시지 전송 완료 이벤트 함수
    // setOnNdefPushCompleteCallback리스너의 함수
    // 메세지 전송이 완료되면 실행되는 함수.
    // 함수에서 완료된 사실을 핸들러에 전달해 그 메세지를 TextView에 띄우는 동작을 수행한다.
    @Override
    public void onNdefPushComplete(NfcEvent event) {
        // 핸들러에 메시지를 전달한다
        mHandler.obtainMessage(1).sendToTarget();
    }

    // NDEF 메시지 전송이 완료되면 TextView 에 결과를 표시한다
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    DialogTextPicker();
//                    Toast.makeText(StudentActivity.this, "성공적으로 전송되었습니다.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    // 출석이 완료되었음을 알려주는 다이얼로그
    public void DialogTextPicker() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("출석이 완료되었습니다.");
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}