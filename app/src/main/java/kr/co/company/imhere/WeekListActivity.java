package kr.co.company.imhere;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class WeekListActivity extends AppCompatActivity {

    //리스트 관련 변수
    private ListView listView;
    WeekListAdapter adapter;

    //인텐트 받은 값
    private String userID;
    private String userCheck;
    private String course_id;
    private String sec_id;
    private String semester;
    private String year;
    private String course_title;

    String response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_list);


        // 로그인 ID와 강의자 구분을 받아온다.
        Intent itt = getIntent();
        userID = itt.getStringExtra("userID");
        userCheck = itt.getStringExtra("userCheck");
        course_id = itt.getStringExtra("course_id");
        course_title = itt.getStringExtra("course_title");
        sec_id = itt.getStringExtra("sec_id");

        semester = itt.getStringExtra("semester");
        year = itt.getStringExtra("year");
        semester = itt.getStringExtra("semester");



/****************************************커스텀 리스트 설정****************************************/

        ArrayList<String> week = new ArrayList<String>();
        for(int i=1; i<16; i++) {
            week.add(""+i);
        }

        //초기화
        TextView coursename = (TextView)findViewById(R.id.mytext);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new WeekListAdapter(getApplicationContext(), week);
        listView.setAdapter(adapter);
        coursename.setText(course_title);


        listView.setOnItemClickListener(Listener); //리스트뷰의 항목을 클릭

/**************************************************************************************************/
    }

    //리스트 항목 클릭 이벤트
    AdapterView.OnItemClickListener Listener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Intent intent = new Intent(WeekListActivity.this, StudentAttenCheckActivity.class);
            intent.putExtra("userID",  userID);
            intent.putExtra("userCheck", userCheck);
            intent.putExtra("course_id",course_id);
            intent.putExtra("sec_id", sec_id);
            intent.putExtra("semester", semester);
            intent.putExtra("year", year);
            int aa = arg2 +1;
            String week = String.valueOf(aa);
            intent.putExtra("week", week);    //arg2가 선택당한 리스트뷰의 번호
            intent.putExtra("course_title",course_title);

            startActivity(intent);
        }
    };


}
