package kr.co.company.imhere;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StudentAttenCheckActivity extends AppCompatActivity {

    //리스트 관련 변수
    private ListView listView;
    private CheckListAdapter adapter;
    private List<AttendanceCheck> AttendanceCheckList;


    //인텐트 받은 값
    private String userID;
    private String userCheck;
    private String course_id;
    private String sec_id;
    private String semester;
    private String year;
    private String week;
    private String ID;
    private String Exist;
    private String course_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_attendance_check);
        // 로그인 ID와 강의자 구분을 받아온다.
        Intent itt = getIntent();
        userID = itt.getStringExtra("userID");
        userCheck = itt.getStringExtra("userCheck");
        course_id = itt.getStringExtra("course_id");
        sec_id = itt.getStringExtra("sec_id");
        semester = itt.getStringExtra("semester");
        year = itt.getStringExtra("year");
        week = itt.getStringExtra("week");
        course_title = itt.getStringExtra("course_title");
        Exist = "false";

/****************************************커스텀 리스트 설정****************************************/

        //초기화
        listView = (ListView) findViewById(R.id.listView);
        AttendanceCheckList = new ArrayList<AttendanceCheck>();      //출석여부를 넣을 리스트
        TextView course_name = (TextView)findViewById(R.id.mytext);
        course_name.setText(course_title);

/**************************************************************************************************/
        listView.setOnItemClickListener(Listener); //리스트뷰의 항목을 클릭
/******************************************서버 통신 코드******************************************/

        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.execute();

        //Toast.makeText(StudentAttenCheckActivity.this, userID + userCheck, Toast.LENGTH_SHORT).show();
        //Toast.makeText(StudentAttenCheckActivity.this, week.toString(), Toast.LENGTH_SHORT).show();

/**************************************************************************************************/
    }
    AdapterView.OnItemClickListener Listener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            adapter.notifyDataSetInvalidated();
            ID = AttendanceCheckList.get(arg2).StudentID;
            Dialog(arg2);       //다이얼로그를 띄워주며 선택된 리스트목록의 번호를 넘겨준다.
            //InsCheckBackgroundTask insCheckBackgroundTask = new InsCheckBackgroundTask();
            //insCheckBackgroundTask.execute();
        }
    };


    //서버 통신 클래스

    class BackgroundTask extends AsyncTask<Void, Void, String> {
        String target;

        @Override
        protected void onPreExecute() {
            target = "https://boli95.cafe24.com/android/Student_list.php";
        }

        @Override
        protected String doInBackground(Void... voids) {

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
                outputStream.write(("course_id=" + course_id).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("sec_id=" + sec_id).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("semester=" + semester).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("year=" + year).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("week=" + week).getBytes());

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp;
                StringBuilder stringBuilder = new StringBuilder();
                while ((temp = bufferedReader.readLine()) != null) {
                    stringBuilder.append(temp + "\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        public void onPostExecute(String result) {

//            //리스트 임시 데이터 테스트
            //studentattenList.add(new Student("학생 이름", "학생학번" ,"주차","출석여부"));
            //리스트 등록
            adapter = new CheckListAdapter(getApplicationContext(), AttendanceCheckList);
            listView.setAdapter(adapter);
//            Toast.makeText(StudentAttenCheckActivity.this,result,Toast.LENGTH_LONG).show();

            //AttendanceCheck check = new AttendanceCheck("이름","2014097027",week, "1");
            //AttendanceCheckList.add(check);
            try {
                //JSON배열이므로 JSONArray 객체로 받아준다.
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray1 = jsonObject.getJSONArray("response1");//takes에서 찾아온 학생 데이터
                JSONArray jsonArray2 = jsonObject.getJSONArray("response2");    //attendace에서 찾아온 출석 여부
                //Toast.makeText(StudentAttenCheckActivity.this, result, Toast.LENGTH_LONG).show();

                int count = 0;
                String name;
                String studentID;
                String ox;

                //배열의 갯수만큼 반복한다.
                while (count < jsonArray1.length()) {
                    JSONObject object1 = jsonArray1.getJSONObject(count);

                    // 강의에 대한 정보를 변수에 저장
                    studentID = object1.getString("ID");
                    name = object1.getString("name");
                    ox = "0";
                    for (int i = 0; i < jsonArray2.length(); i++) {                //출석 데이터가 있는 학생을 찾아 출석 여부 표시
                        JSONObject object2 = jsonArray2.getJSONObject(i);
                        if (studentID.equals(object2.getString("ID"))) {
                            ox = object2.getString("chk");
                        }
                    }
                    //객체로 만든 후 리스트에 등록한다.
                    AttendanceCheck check = new AttendanceCheck(name, studentID, week, ox);

                    AttendanceCheckList.add(check);

                    //카운트를 하나 늘려준다.
                    count++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

        class InsCheckBackgroundTask extends AsyncTask<Void, Void, String> {
            String target;

            @Override
            protected void onPreExecute() {
                target = "https://boli95.cafe24.com/android/Ins_Check.php";
                //Toast.makeText(StudentAttenCheckActivity.this,ID + " "+course_id+ " "+sec_id+ " "+semester+ " "+year+ " "+week ,Toast.LENGTH_SHORT).show();
            }

            @Override
            protected String doInBackground(Void... voids) {

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

                    outputStream.write(("ID=" + ID).getBytes());
                    outputStream.write("&".getBytes());
                    outputStream.write(("course_id=" + course_id).getBytes());
                    outputStream.write("&".getBytes());
                    outputStream.write(("sec_id=" + sec_id).getBytes());
                    outputStream.write("&".getBytes());
                    outputStream.write(("semester=" + semester).getBytes());
                    outputStream.write("&".getBytes());
                    outputStream.write(("year=" + year).getBytes());
                    outputStream.write("&".getBytes());
                    outputStream.write(("week=" + week).getBytes());

                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String temp;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((temp = bufferedReader.readLine()) != null) {
                        stringBuilder.append(temp + "\n");
                    }
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();
                    return stringBuilder.toString().trim();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }

            public void onPostExecute(String result) {

                try {
                    //JSON배열이므로 JSONArray 객체로 받아준다.
                    JSONObject jsonObject = new JSONObject(result);
                    Exist = jsonObject.getString("exist");//수업 여부를 알려주는 데이터

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (Exist.equals("false")) {
                    Toast.makeText(StudentAttenCheckActivity.this, "아직 진행되지 않은 수업입니다.", Toast.LENGTH_SHORT).show();
                }

            }
        }


    public void Dialog(int arg2) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final int i = arg2;
        // 제목셋팅
        alertDialogBuilder.setTitle("출석");

        // AlertDialog 셋팅
        alertDialogBuilder
                .setMessage(AttendanceCheckList.get(i).getName()+"수강생을 출석되도록 하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("출석",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                InsCheckBackgroundTask ins_CheckBackgroundTask = new InsCheckBackgroundTask();
                                ins_CheckBackgroundTask.execute();
                                if(Exist.equals("true")) {
                                    Exist = "false";

                                    AttendanceCheckList.get(i).setOX("1");
                                    //리스트뷰를 다시 만들어서 하면 새로 생성되는것
                                    //이기에 새로 생성 하지 않고 재구성 하면 된다.
                                    //adapter = new CheckListAdapter(getApplicationContext(), AttendanceCheckList);
                                    //listView.setAdapter(adapter);
                                    adapter.notifyDataSetInvalidated();                                     //스크롤이 그대로 남아잇다.
                                    //참고: http://www.masterqna.com/android/12311/listview-%EA%B0%B1%EC%8B%A0-%EB%AC%B8%EC%A0%9C%EC%9E%85%EB%8B%88%EB%8B%A4
                                    //listView.invalidateViews();
                                    //listView.refreshDrawableState();
                                }

                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                // 다이얼로그를 취소한다
                                dialog.cancel();
                            }
                        });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }

        }



