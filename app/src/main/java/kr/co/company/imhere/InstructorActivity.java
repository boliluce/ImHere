package kr.co.company.imhere;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import android.nfc.*;
import android.nfc.tech.*;
import android.os.*;
import android.provider.*;
import android.app.*;
import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;

//데이터를 받는 쪽이 관리자
//받은 데이터를 읽어 DB와 대조하여 해당 학생이 출석했음을 확인
/*--------------------------경란------------------------------*/
public class InstructorActivity extends AppCompatActivity {

    NfcAdapter mNfcAdapter; // NFC 어댑터
    PendingIntent mPendingIntent; // 수신받은 데이터가 저장된 인텐트
    IntentFilter[] mIntentFilters; // 인텐트 필터
    String[][] mNFCTechLists;

    private String userID;
    private String userPassword;
    private String userCheck;
    private String course_id;
    private String sec_id;
    private String semester;
    private String year;
    private String stdID;
    private String course_title;
    private String week;
    private String time;

    private  String change_start_time;
    private  String change_end_time;

    public TextView weekText;
    public TextView courseTimeText;
    public Button startButton;
    public Button endButton;

    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //액션바를 없애는 기능
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor);

        weekText =(TextView)findViewById(R.id.week);
        courseTimeText =(TextView)findViewById(R.id.course_time);
        startButton = (Button)findViewById(R.id.att_start);
        endButton = (Button)findViewById(R.id.att_end);

        //버튼 클릭 이벤트
        startButton.setOnClickListener(startClickListener);
        endButton.setOnClickListener(endClickListener);

        InitBackgroundTask initBackgroundTask = new InitBackgroundTask();
        initBackgroundTask.execute();

        // 인텐트로 넘어온 데이터를 변수에 저장
        Intent itt = getIntent();
        userID = itt.getStringExtra("userID");
        userPassword = itt.getStringExtra("userPassword");
        userCheck = itt.getStringExtra("userCheck");
        course_id = itt.getStringExtra("course_id");
        course_title = itt.getStringExtra("course_title");
        sec_id = itt.getStringExtra("sec_id");
        semester = itt.getStringExtra("semester");
        year = itt.getStringExtra("year");

//        String str = userID + "\n" + userCheck + "\n" + course_id + "\n" + sec_id + "\n" + semester + "\n" + year + "\n";
//        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();

        //강의자(instructor)로 로그인 했을 경우만 '받기' 가능
        if(userCheck.equals("instructor")) {

            // NFC 어댑터를 구한다
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

            // NFC 어댑터가 null 이라면 칩이 존재하지 않는 것으로 간주
            if (mNfcAdapter == null) {
                Toast.makeText(InstructorActivity.this, "NFC 칩이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // NFC 데이터 활성화에 필요한 인텐트를 생성
            // SINGLE_TOP은 Activity를 재사용. A-B-B로 호출할 경우 새로운 B를 생성하지 않고 재사용
            Intent intent = new Intent(this, getClass());
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            // NFC 데이터 활성화에 필요한 인텐트 필터를 생성
            IntentFilter iFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            try {
                iFilter.addDataType("*/*");
                mIntentFilters = new IntentFilter[]{iFilter};
            } catch (Exception e) {
                Toast.makeText(InstructorActivity.this, "필터 생성 오류", Toast.LENGTH_SHORT).show();
            }
            mNFCTechLists = new String[][]{new String[]{NfcF.class.getName()}};


        }
        Button button = (Button)findViewById(R.id.attendance);
        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InstructorActivity.this, WeekListActivity.class);
                intent.putExtra("userID",  userID);
                intent.putExtra("userCheck", userCheck);
                intent.putExtra("course_id",course_id);
                intent.putExtra("course_title",course_title);
                intent.putExtra("sec_id", sec_id);
                intent.putExtra("semester", semester);
                intent.putExtra("year", year);
                startActivity(intent);
            }
        });
    }


    //앱 실행 시 호출되는함수
    public void onResume() {
        super.onResume();

        //강의자(instructor)로 로그인 했을 경우만 실행
        if(userCheck.equals("instructor")) {

            // 앱이 실행될때 NFC 어댑터를 활성화 한다
            if (mNfcAdapter != null)
                //NFC 태그 인식을 기다리고 있는 중
                mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mNFCTechLists);

            // NFC 태그 스캔으로 앱이 자동 실행되었을때
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()))
                // 인텐트에 포함된 정보를 분석해서 화면에 표시
                //자기 자신을 호출하며, 갱신
                onNewIntent(getIntent());

        }
    }

    //앱 종료 시 호출되는함수
    public void onPause() {
        super.onPause();

        //강의자(instructor)로 로그인 했을 경우만 실행
        if(userCheck.equals("instructor")) {

            // 앱이 종료될때 NFC 어댑터를 비활성화 한다
            if (mNfcAdapter != null)
                mNfcAdapter.disableForegroundDispatch(this);

        }
    }

    // NFC 태그 정보 수신 함수. 인텐트에 포함된 정보를 분석해서 화면에 표시
    @Override
    public void onNewIntent(Intent intent) {

//        // 인텐트에서 액션을 추출
//        String action = intent.getAction();
//        // 인텐트에서 태그 정보 추출
//        String tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG).toString();
//        String strMsg = action + "\n\n" + tag;
//        // 액션 정보와 태그 정보를 화면에 출력
//        mTextView.setText(strMsg);

        // 인텐트에서 NDEF 메시지 배열을 구한다
        Parcelable[] messages = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        if(messages == null) return;

        //메세지 출력
        for(int i=0; i < messages.length; i++)
            // NDEF 메시지를 화면에 출력
            //showMsg함수가 실제 출력을 담당하는 함수
            showMsg((NdefMessage)messages[i]);
    }

    // NDEF 메시지를 화면에 출력
    public void showMsg(NdefMessage mMessage) {
        String strMsg = "", strRec="";
        // NDEF 메시지에서 NDEF 레코드 배열을 구한다
        NdefRecord[] recs = mMessage.getRecords();
        for (int i = 0; i < recs.length; i++) {
            // 개별 레코드 데이터를 구한다
            NdefRecord record = recs[i];
            byte[] payload = record.getPayload();
            // 레코드 데이터 종류가 텍스트 일때
            if( Arrays.equals(record.getType(), NdefRecord.RTD_TEXT) ) {
                // 버퍼 데이터를 인코딩 변환
                strRec = byteDecoding(payload);
            }
            // 레코드 데이터 종류가 URI 일때
            // 이건 안씀
            else if( Arrays.equals(record.getType(), NdefRecord.RTD_URI) ) {
                strRec = new String(payload, 0, payload.length);
            }
            strMsg += (strRec);
        }

        // strMsg에 'ID=학생ID'형태로 데이터가 담겨있다.
        stdID = strMsg.substring(3,strMsg.length());
        String str = stdID + "\n" + course_id + "\n" + sec_id + "\n" + semester + "\n" + year;

        // Toast.makeText(getApplicationContext(), stdID.toString(), Toast.LENGTH_LONG).show();
        //Toast.makeText(getApplicationContext(), str.toString(), Toast.LENGTH_SHORT).show();

        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.execute();
    }

    // 버퍼 데이터를 디코딩해서 String 으로 변환
    public String byteDecoding(byte[] buf) {
        String strText="";
        String textEncoding = ((buf[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
        int langCodeLen = buf[0] & 0077;

        try {
            strText = new String(buf, langCodeLen + 1,
                    buf.length - langCodeLen - 1, textEncoding);
        } catch(Exception e) {
            Log.d("tag1", e.toString());
        }
        return strText;
    }


    //******경란******//
    //진행중인 강의 정보가 있다면 설정을 셋팅해주는 서버 통신 코드
    class InitBackgroundTask extends AsyncTask<Void, Void, String>
    {
        String target;
        Boolean ing;

        @Override
        protected  void onPreExecute(){
            target = "https://boli95.cafe24.com/android/Init.php";
        }

        @Override
        protected  String doInBackground(Void... voids){

            try {
                URL url = new URL(target);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");

                // 연결된 connection 에서 출력도 하도록 설정
                httpURLConnection.setDoOutput(true);

                // 요청 파라미터 출력
                // - 파라미터는 쿼리 문자열의 형식으로 지정 (ex) 이름=값&이름=값 형식&...
                // - 파라미터의 값으로 한국어 등을 송신하는 경우는 URL 인코딩을 해야 함.
                OutputStream outputStream = httpURLConnection.getOutputStream();

                outputStream.write(("course_id="+course_id).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("sec_id="+sec_id).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("semester="+semester).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("year="+year).getBytes());

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp;
                StringBuilder stringBuilder = new StringBuilder();
                while((temp = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(temp + "\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                try {
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString().trim());
                    ing = jsonObject.getBoolean("ing");
                    week = jsonObject.getString("week");

                    // 진행중인 수업이 있으면 진행중인 수업의 주차를 week에 초기화한다.
                    // 마감 버튼을 누를 때, 몇주차 수업을 마감할건지 알기 위해 week변수를 파라메터로 보내기 때문에 필요.
                    if(ing){    week = jsonObject.getString("week");    }

                }catch (Exception e)
                {
                    e.printStackTrace();
                }

                return stringBuilder.toString().trim();
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public  void onProgressUpdate(Void... values){
            super.onProgressUpdate(values);
        }

        public void onPostExecute(String result){
            //여기서 서버 통신 후 넘어온 데이터 처리
            //진행중인 수업이 있으면 시작버튼을 비활성화하고, 마감버튼을 활성화한다.
            if(ing){
                startButton.setEnabled(false);
                endButton.setEnabled(true);
                weekText.setText(week+"주차");
            }else{
                startButton.setEnabled(true);
                endButton.setEnabled(false);
                weekText.setText("진행 중인 강의가 없습니다.");
            }

        }
    }


    //******현영******//
    //서버 통신 클래스
    // 출석 체크하는 서버 통신 코드
    class BackgroundTask extends AsyncTask<Void, Void, String>
    {
        String target;

        @Override
        protected  void onPreExecute(){
            target = "https://boli95.cafe24.com/android/Check.php";
        }

        @Override
        protected  String doInBackground(Void... voids){

            try {
                URL url = new URL(target);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");

                // 연결된 connection 에서 출력도 하도록 설정
                httpURLConnection.setDoOutput(true);

                // 요청 파라미터 출력
                // - 파라미터는 쿼리 문자열의 형식으로 지정 (ex) 이름=값&이름=값 형식&...
                // - 파라미터의 값으로 한국어 등을 송신하는 경우는 URL 인코딩을 해야 함.
                OutputStream outputStream = httpURLConnection.getOutputStream();


                outputStream.write(("ID="+stdID).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("course_id="+course_id).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("sec_id="+sec_id).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("semester="+semester).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("year="+year).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("week="+week).getBytes());

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp;
                StringBuilder stringBuilder = new StringBuilder();
                while((temp = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(temp + "\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return stringBuilder.toString().trim();
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public  void onProgressUpdate(Void... values){
            super.onProgressUpdate(values);
        }

        public void onPostExecute(String result){
            //여기서 서버 통신 후 넘어온 데이터 처리
            try {
                JSONObject jsonObject = new JSONObject(result);
                //jsonObject.getString("takes");
                if(jsonObject.getString("takes").equals("미수강"))
                    Toast.makeText(getApplicationContext(),jsonObject.getString("takes")+"자입니다.", Toast.LENGTH_LONG).show();
                else if(jsonObject.getString("takes").equals("수강"))
                    Toast.makeText(getApplicationContext(), jsonObject.getString("takes")+"자 입니다. "+jsonObject.getString("attendance")+"하였습니다.", Toast.LENGTH_LONG).show();

            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }

    }

    //********현영*******//*/
    // 출석 시작 서버 통신 코드
    class StartBackgroundTask extends AsyncTask<Void, Void, String>
    {
        String target;

        @Override
        protected  void onPreExecute(){
            target = "https://boli95.cafe24.com/android/Start.php";
        }

        @Override
        protected  String doInBackground(Void... voids){

            try {
                URL url = new URL(target);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");

                // 연결된 connection 에서 출력도 하도록 설정
                httpURLConnection.setDoOutput(true);

                // 요청 파라미터 출력
                // - 파라미터는 쿼리 문자열의 형식으로 지정 (ex) 이름=값&이름=값 형식&...
                // - 파라미터의 값으로 한국어 등을 송신하는 경우는 URL 인코딩을 해야 함.
                OutputStream outputStream = httpURLConnection.getOutputStream();

                outputStream.write(("course_id="+course_id).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("sec_id="+sec_id).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("semester="+semester).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("year="+year).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("time="+time).getBytes());

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp;
                StringBuilder stringBuilder = new StringBuilder();
                while((temp = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(temp + "\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                try {
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString().trim());
                    week = jsonObject.getString("week");
                    change_start_time = jsonObject.getString("change_start_time");
                    change_end_time = jsonObject.getString("change_end_time");


                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return stringBuilder.toString().trim();
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        public  void onProgressUpdate(Void... values){
            super.onProgressUpdate(values);
        }

        public void onPostExecute(String result){
            //여기서 서버 통신 후 넘어온 데이터 처리
            weekText.setText(week+"주차");
            courseTimeText.setText(change_start_time + " ~ " + change_end_time);

        }
    }

    //********경란*******//*/
    // 출석 종료 서버 통신 코드
    class EndBackgroundTask extends AsyncTask<Void, Void, String>
    {
        String target;

        @Override
        protected  void onPreExecute(){
            target = "https://boli95.cafe24.com/android/End.php";
        }

        @Override
        protected  String doInBackground(Void... voids){

            try {
                URL url = new URL(target);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");

                // 연결된 connection 에서 출력도 하도록 설정
                httpURLConnection.setDoOutput(true);

                // 요청 파라미터 출력
                // - 파라미터는 쿼리 문자열의 형식으로 지정 (ex) 이름=값&이름=값 형식&...
                // - 파라미터의 값으로 한국어 등을 송신하는 경우는 URL 인코딩을 해야 함.
                OutputStream outputStream = httpURLConnection.getOutputStream();

                outputStream.write(("course_id="+course_id).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("sec_id="+sec_id).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("semester="+semester).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("year="+year).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("week="+week).getBytes());

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp;
                StringBuilder stringBuilder = new StringBuilder();
                while((temp = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(temp + "\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                try {
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString().trim());
                    week = jsonObject.getString("msg");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return stringBuilder.toString().trim();
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        public  void onProgressUpdate(Void... values){
            super.onProgressUpdate(values);
        }

        public void onPostExecute(String result){
            //여기서 서버 통신 후 넘어온 데이터 처리
            weekText.setText("진행 중인 강의가 없습니다.");
            courseTimeText.setText("00:00:00 ~ 00:00:00");
        }
    }

    // 출석 시작 서버 통신 코드
    class CancelBackgroundTask extends AsyncTask<Void, Void, String>
    {
        String target;

        @Override
        protected  void onPreExecute(){
            target = "https://boli95.cafe24.com/android/Cancel.php";
        }

        @Override
        protected  String doInBackground(Void... voids){

            try {
                URL url = new URL(target);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");

                // 연결된 connection 에서 출력도 하도록 설정
                httpURLConnection.setDoOutput(true);

                // 요청 파라미터 출력
                // - 파라미터는 쿼리 문자열의 형식으로 지정 (ex) 이름=값&이름=값 형식&...
                // - 파라미터의 값으로 한국어 등을 송신하는 경우는 URL 인코딩을 해야 함.
                OutputStream outputStream = httpURLConnection.getOutputStream();

                outputStream.write(("course_id="+course_id).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("sec_id="+sec_id).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("semester="+semester).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("year="+year).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("week="+week).getBytes());

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp;
                StringBuilder stringBuilder = new StringBuilder();
                while((temp = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(temp + "\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                try {
                    // 서버에서 변수 넘겨받는 곳
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return stringBuilder.toString().trim();
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        public  void onProgressUpdate(Void... values){
            super.onProgressUpdate(values);
        }

        public void onPostExecute(String result){
            //여기서 서버 통신 후 넘어온 데이터 처리
            weekText.setText("진행 중인 강의가 없습니다.");
            courseTimeText.setText("00:00:00 ~ 00:00:00");
        }
    }


    //********경란*******//*/
    // 출석 시작 버튼 이벤트
    Button.OnClickListener startClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            //이곳에 버튼 클릭시 일어날 일을 적습니다.
            if(startButton.getText().equals("출석 시작")){
                DialogTimePicker();
            }else{
                DialogCancelPicker();
            }
        }
    };

    //********경란*******//*/
    // 출석 종료 버튼 이벤트
    Button.OnClickListener endClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            //이곳에 버튼 클릭시 일어날 일을 적습니다.

            DialogPasswordPicker();
        }
    };

    //********경란*******//*/
    // 시작 버튼 클릭 시 시간 설정 다이얼로그
    public void DialogTimePicker() {
        TimePickerDialog.OnTimeSetListener mTimeSetListener =
                new TimePickerDialog.OnTimeSetListener() {
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        time = "00:00";
                        int sum = hourOfDay * 60 + minute;
                        if(9*60 <= sum && 18*60 >= sum){
                            time = hourOfDay + ":" + minute;
                        }

                        StartBackgroundTask startBackgroundTask = new StartBackgroundTask();
                        startBackgroundTask.execute();

//                        startButton.setEnabled(false);
                        startButton.setText("출석 취소");
                        endButton.setEnabled(true);
                    }
                };
        TimePickerDialog alert =
                new TimePickerDialog(this, mTimeSetListener, 0, 0, false);
        alert.show();
    }

    //********경란*******//*/
    // 종료 버튼 클릭 시 비밀번호 입력 다이얼로그
    public void DialogPasswordPicker() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        input = new EditText(this);

        builder.setTitle("비밀번호를 입력해주세요");
        builder.setView(input);
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
//                        Toast.makeText(InstructorActivity.this, "pw"+userPassword, Toast.LENGTH_SHORT).show();
//                        Toast.makeText(InstructorActivity.this, "input"+input.getText().toString(), Toast.LENGTH_SHORT).show();
                        if(input.getText().toString().equals(userPassword)){
                            EndBackgroundTask endBackgroundTask = new EndBackgroundTask();
                            endBackgroundTask.execute();

                            endButton.setEnabled(false);
                            startButton.setText("출석 시작");
//                            startButton.setEnabled(true);

                            Toast.makeText(InstructorActivity.this, "출석이 마감되었습니다", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(InstructorActivity.this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    // 출석 취소를 할지 묻는 다이얼로그
    public void DialogCancelPicker() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("진행 중인 출석을 취소하시겠습니까?");
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        CancelBackgroundTask cancelBackgroundTask = new CancelBackgroundTask();
                        cancelBackgroundTask.execute();

                        endButton.setEnabled(false);
                        startButton.setText("출석 시작");
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


}
