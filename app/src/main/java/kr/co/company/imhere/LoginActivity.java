package kr.co.company.imhere;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
/*--------------------------경란------------------------------*/
public class LoginActivity extends AppCompatActivity {

    String userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //액션바를 없애는 기능
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText idText = (EditText)findViewById(R.id.idInput);
        final EditText passwordText = (EditText)findViewById(R.id.passwordInput);
        final RadioButton checkButton = (RadioButton)findViewById(R.id.radio_instructor);
        final Button loginButton = (Button)findViewById(R.id.loginButton);
        final TextView signupButton = (TextView) findViewById(R.id.signup);


        //권한 설정
        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        }

        // 휴대폰 번호 가져오기
        if(ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            userPhone = tMgr.getLine1Number();
            Toast.makeText(getApplicationContext(), userPhone, Toast.LENGTH_LONG).show();
        }

        //로그인 버튼 클릭 이벤트
        loginButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                //서버로 전달할 값
                final String userID = idText.getText().toString();
                final String userPassword = passwordText.getText().toString();
                final String userCheck = checkButton.isChecked() ? "instructor" : "student" ;

                //서버 통신, 서버에서 값을 받아옴
                Response.Listener<String> responseListener = new Response.Listener<String>(){

                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            String signup = jsonResponse.getString("signup");

                            String text;

                            if(signup.equals("미등록")){
                                text = "등록되지 않은 사용자입니다.";
                                IDialog(text);
                            }else {
                                if(signup.equals("성공")) {
                                    if (success) {
                                        text = "";
                                        Intent intent;
                                        if (userCheck.equals("instructor")) {
                                            intent = new Intent(LoginActivity.this, CourseListActivity.class);
                                        } else {
                                            intent = new Intent(LoginActivity.this, StudentCourseListActivity.class);
                                        }
                                        intent.putExtra("userID", userID);
                                        intent.putExtra("userPassword", userPassword);
                                        intent.putExtra("userCheck", userCheck);
                                        LoginActivity.this.startActivity(intent);

                                    } else {
                                        text = "로그인에 실패하였습니다.";
                                        IDialog(text);
                                    }
                                }else{
                                    text = "등록된 휴대폰과 일치하지 않습니다.";
                                    IDialog(text);
                                }
                            }

//                            if(!text.endsWith("")) {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//                                builder.setMessage(text)
//                                        .setNegativeButton("다시 시도", null)
//                                        .create()
//                                        .show();
//                            }
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };

                //서버 통신, 서버에 값을 전달함
                LoginRequest loginRequest = new LoginRequest(userID, userPassword, userPhone, userCheck, responseListener);
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);

                queue.add(loginRequest);

            }
        });


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TextView 클릭될 시 할 코드작성
                Intent intent;
                intent = new Intent(LoginActivity.this, SignupActivity.class);
                LoginActivity.this.startActivity(intent);
            }
        });


    }


    // 출석 취소를 할지 묻는 다이얼로그
    public void IDialog(String text) {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(text);
        builder.setNegativeButton("확인", null);
        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

}
