package kr.co.company.imhere;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/*--------------------------경란------------------------------*/

public class CourseListActivity extends AppCompatActivity {

    //리스트 관련 변수
    private ListView listView;
    private CourseListAdapter adapter;
    private List<Course> courseList;

    //인텐트 받은 값
    private String userID;
    private String userPassword;
    private String userCheck;

    String response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);


        // 로그인 ID와 강의자 구분을 받아온다.
        Intent itt = getIntent();
        userID = itt.getStringExtra("userID");
        userPassword = itt.getStringExtra("userPassword");
        userCheck = itt.getStringExtra("userCheck");


/****************************************커스텀 리스트 설정****************************************/

        //초기화
        listView = (ListView) findViewById(R.id.listView);
        courseList = new ArrayList<Course>();

        listView.setOnItemClickListener(Listener); //리스트뷰의 항목을 클릭

/**************************************************************************************************/
/******************************************서버 통신 코드******************************************/

        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.execute();

/**************************************************************************************************/

    }

    //리스트 항목 클릭 이벤트
    AdapterView.OnItemClickListener Listener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Intent intent = new Intent(CourseListActivity.this, InstructorActivity.class);
            intent.putExtra("userID",  userID);
            intent.putExtra("userPassword", userPassword);
            intent.putExtra("userCheck", userCheck);
            intent.putExtra("course_title",courseList.get(arg2).courseTitle);
            intent.putExtra("course_id",courseList.get(arg2).courseID);
            intent.putExtra("sec_id", courseList.get(arg2).secID);
            intent.putExtra("semester", courseList.get(arg2).semester);
            intent.putExtra("year", courseList.get(arg2).year);

            startActivity(intent);
        }
    };

    //서버 통신 클래스

    class BackgroundTask extends AsyncTask<Void, Void, String>
    {
        String target;

        @Override
        protected  void onPreExecute(){
            target = "https://boli95.cafe24.com/android/Course_list.php";
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
                outputStream.write(("userID=" + userID).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("userCheck=" + userCheck).getBytes());


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

//            //리스트 임시 데이터 테스트
//            courseList.add(new Course("소프트웨어 프로젝트2", "WED", "AM 09:00", "PM 01:00"));
//            courseList.add(new Course("소프트웨어 개발방법", "WED", "PM 02:00", "PM 05:00"));
//            courseList.add(new Course("컴퓨터 시뮬레이션의 이해", "THU", "PM 02:00", "PM 05:00"));

            //리스트 등록
            adapter = new CourseListAdapter(getApplicationContext(), courseList);
            listView.setAdapter(adapter);

            try{
                //JSON배열이므로 JSONArray 객체로 받아준다.
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("response");

                int count = 0;
                String courseTitle, courseDay, courseStart, courseEnd;

                //배열의 갯수만큼 반복한다.
                while (count < jsonArray.length())
                {
                    JSONObject object = jsonArray.getJSONObject(count);

                    // 강의에 대한 정보를 변수에 저장
                    String course_id = object.getString("course_id");
                    String sec_id = object.getString("sec_id");
                    String semester = object.getString("semester");
                    String year = object.getString("year");

                    //시간 형식을 [AM/PM] 00 : 00 으로 바꿔준다.
//                    int s_hr = Integer.parseInt(object.getString("start_hr"));
//                    int e_hr = Integer.parseInt(object.getString("end_hr"));
//                    int s_hr = object.getInt("start_hr");
//                    int e_hr = object.getInt("end_hr");
//                    String start_hr = (s_hr > 12) ? "PM " + String.format("%02d",s_hr-12) : "AM " + String.format("%02d",s_hr);
//                    String end_hr = (e_hr > 12) ? "PM " + String.format("%02d",e_hr-12) : "AM " + String.format("%02d",e_hr);
//                    String start_min = String.format("%02d",Integer.parseInt(object.getString("start_min")));
//                    String end_min = String.format("%02d",Integer.parseInt(object.getString("end_min")));

                    //제목, 요일, 시작시간, 끝시간을 가져온다.
                    courseTitle = object.getString("title");
                    courseDay = object.getString("day");
                    courseStart = object.getString("start_time");
                    courseEnd = object.getString("end_time");
//                    courseStart = start_hr + " : " + start_min;
//                    courseEnd = end_hr + " : " + end_min;

                    //객체로 만든 후 리스트에 등록한다.
                    Course course = new Course(courseTitle, courseDay, courseStart, courseEnd, course_id, sec_id, semester, year);
                    courseList.add(course);

                    //카운트를 하나 늘려준다.
                    count++;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }

}
