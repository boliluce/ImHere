package kr.co.company.imhere;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignupActivity extends AppCompatActivity {

    private EditText idText;
    private EditText passwordText;
    private Button signupButton;

    private String userID;
    private String userPassword;
    private String userPhone;

    private String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        idText = (EditText)findViewById(R.id.idInput);
        passwordText = (EditText)findViewById(R.id.passwordInput);
        signupButton = (Button) findViewById(R.id.signupButton);

        if(ContextCompat.checkSelfPermission(SignupActivity.this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            userPhone = tMgr.getLine1Number();
            Toast.makeText(getApplicationContext(), userPhone, Toast.LENGTH_LONG).show();
        }


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userID = idText.getText().toString();
                userPassword = passwordText.getText().toString();

                SignupBackgroundTask backgroundTask = new SignupBackgroundTask();
                backgroundTask.execute();
            }
        });




    }



    // 출석 시작 서버 통신 코드
    class SignupBackgroundTask extends AsyncTask<Void, Void, String>
    {
        String target;

        @Override
        protected  void onPreExecute(){
            target = "https://boli95.cafe24.com/android/Signup.php";
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

                outputStream.write(("userID="+userID).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("userPassword="+userPassword).getBytes());
                outputStream.write("&".getBytes());
                outputStream.write(("userPhone="+userPhone).getBytes());

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
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString().trim());
                    msg = jsonObject.getString("success");
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

            // 등록 성공
            if(msg.equals("true")){
                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                builder.setMessage("성공적으로 등록되었습니다.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent;
                                intent = new Intent(SignupActivity.this, LoginActivity.class);
                                SignupActivity.this.startActivity(intent);
                            }
                        })
                        .create()
                        .show();

            }else { //등록 실패

                String text;
                if (msg.equals("id")) text = "아이디가 없습니다.";
                else if(msg.equals("pw")) text = "비밀번호가 틀립니다.";
                else text = "이미 등록된 계정입니다.";

                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                builder.setMessage(text)
                        .setNegativeButton("다시 시도", null)
                        .create()
                        .show();
            }
        }
    }



}
