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



    // ?????? ?????? ?????? ?????? ??????
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

                // ????????? connection ?????? ????????? ????????? ??????
                httpURLConnection.setDoOutput(true);

                // ?????? ???????????? ??????
                // - ??????????????? ?????? ???????????? ???????????? ?????? (ex) ??????=???&??????=??? ??????&...
                // - ??????????????? ????????? ????????? ?????? ???????????? ????????? URL ???????????? ?????? ???.
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
                    // ???????????? ?????? ???????????? ???
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
            //????????? ?????? ?????? ??? ????????? ????????? ??????

            // ?????? ??????
            if(msg.equals("true")){
                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                builder.setMessage("??????????????? ?????????????????????.")
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent;
                                intent = new Intent(SignupActivity.this, LoginActivity.class);
                                SignupActivity.this.startActivity(intent);
                            }
                        })
                        .create()
                        .show();

            }else { //?????? ??????

                String text;
                if (msg.equals("id")) text = "???????????? ????????????.";
                else if(msg.equals("pw")) text = "??????????????? ????????????.";
                else text = "?????? ????????? ???????????????.";

                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                builder.setMessage(text)
                        .setNegativeButton("?????? ??????", null)
                        .create()
                        .show();
            }
        }
    }



}
