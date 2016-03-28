package com.zyys.yunxin.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.zyys.yunxin.Api;
import com.zyys.yunxin.R;
import com.zyys.yunxin.utils.DataUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText et_username,et_password;
    private CheckBox cb_rememberpwd;
    private DataUtil util;
    private RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mQueue = Volley.newRequestQueue(LoginActivity.this);

        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        cb_rememberpwd = (CheckBox) findViewById(R.id.cb_rememberpwd);
        util = new DataUtil(this);

        Map<String, ?> map = util.getData("user");
        Object name = map.get("userName");
        if (name != null && !name.equals("")) {
            et_username.setText((String) name);
        }
        Object password = map.get("userPassword");
        if (password != null && !password.equals("")) {
            et_password.setText((String) password);
        }
        Object isPassword = map.get("isPassword");
        if (isPassword != null && !isPassword.equals("")) {
            cb_rememberpwd.setChecked((Boolean) isPassword);
        }

    }

    //click login
    public void login(View view) {
        final String username = et_username.getText().toString().trim();
        final String password = et_password.getText().toString().trim();

        if (username.equals("") || username == null) {
            Toast.makeText(getApplicationContext(), "用户名不能为空！", Toast.LENGTH_SHORT).show();
        } else if (password.equals("") || password == null) {
            Toast.makeText(getApplicationContext(), "密码不能为空！", Toast.LENGTH_SHORT).show();
        } else if (username.equals("drug") && password.equals("test")){

            getLoginData(username,password);//

            if (cb_rememberpwd.isChecked()) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("userName", username);
                map.put("userPassword", password);
                map.put("isPassword", cb_rememberpwd.isChecked());
                util.putData("user", map);
            } else {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("userName", "");
                map.put("userPassword", "");
                map.put("isPassword", false);
                util.putData("user", map);
            }
            startActivity(new Intent(LoginActivity.this, ContactsActivity.class));
        }else {
            Toast.makeText(this,"账号或密码错误！",Toast.LENGTH_SHORT).show();
        }

    }



    public void doLogin(String account,String token) {
        LoginInfo info = new LoginInfo(account,token); // config...
        RequestCallback<LoginInfo> callback =
                new RequestCallback<LoginInfo>() {
                    @Override
                    public void onSuccess(LoginInfo loginInfo) {


                        Log.d("loginInfo=getAccount=",loginInfo.getAccount());
                        Log.d("loginInfo=getToken=",loginInfo.getToken());
                        Log.d("loginInfo=","登录成功="+loginInfo.toString());

                    }

                    @Override
                    public void onFailed(int i) {
                        Log.d("loginInfo=i=",i+"");
                    }

                    @Override
                    public void onException(Throwable throwable) {

                    }
                    // 可以在此保存LoginInfo到本地，下次启动APP做自动登录用

                };
        NIMClient.getService(AuthService.class).login(info)
                .setCallback(callback);
    }


    //返回登录返回数据
    public void getLoginData(final String username, final String password){//获取登录消息（成功获取）
        //String url = "http://testapi.zhaoyang120.cn/api/drug/login";
        String url = Api.loginUrl;
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(LoginActivity.this, "获取用户信息成功！", Toast.LENGTH_SHORT).show();
                        Log.d("LoginActivity", response.toString());
                        try {
                            JSONObject object = new JSONObject(response);

                            JSONObject jsonObject = object.getJSONObject("data").getJSONObject("account");

                            String yunxin_accid = jsonObject.getString("yunxin_accid");
                            String yunxin_token = jsonObject.getString("yunxin_token");
                            Log.d("LoginActivity", yunxin_accid.toString());
                            Log.d("LoginActivity", yunxin_token.toString());

                            doLogin(yunxin_accid, yunxin_token);//login


                            // 57
                            // 312410cf26e9d2bf606f4fd40067f572
                            //doLogin("57", "312410cf26e9d2bf606f4fd40067f572");//login


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> param = new HashMap<String,String>();
                param.put("name",username);
                param.put("password",password);
                return param;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return super.getHeaders();
            }
        };
        mQueue.add(request);
    }

}
