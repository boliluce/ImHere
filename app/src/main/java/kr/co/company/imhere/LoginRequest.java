package kr.co.company.imhere;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by boli on 2017-10-11.
 */
/*--------------------------경란------------------------------*/
public class LoginRequest extends StringRequest {
    final static private String URL = "https://boli95.cafe24.com/android/Login.php";
    private Map<String, String> parameters;

    public LoginRequest(String userID, String userPassword, String userPhone, String userCheck, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        parameters = new HashMap<>();
        parameters.put("userID", userID);
        parameters.put("userPassword", userPassword);
        parameters.put("userPhone", userPhone);
        parameters.put("userCheck", userCheck);
    }

    @Override
    public  Map<String,String> getParams(){ return parameters; }
}
