package com.example.pleasego.loginpart;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.percent.PercentRelativeLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pleasego.R;
import com.example.pleasego.gamepart.GameActivity;
import com.example.pleasego.info.CharacterInfo;
import com.example.pleasego.utils.Apis;
import com.example.pleasego.utils.CustomProgressDialog;
import com.example.pleasego.utils.HttpDealResponse;
import com.example.pleasego.utils.MyUtils;
import com.example.pleasego.utils.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import okhttp3.ResponseBody;


public class RegisterActivity extends Activity implements View.OnClickListener {

    private String mobile;
    private String password;
    private int time = 30;
    private final int REGISTER_ERROR = 1;
    private final int SERVER_ERROR = 2;
    private final int START_COUNT_DOWN = 1;
    private final int RESTORE_TIME = 2;


    private boolean ifIdentifyTrue = false;
    private boolean ifInputPassword = false;
    private boolean ifPasswordSame = false;
    private boolean ifRestore = false;
//    private boolean flag = false;//校验验证码的响应位

    private EditText inputPassword1EditText;
    private EditText inputPassword2EditText;
    private EditText phoneNumEditText;
    private EditText identifyCodeEditText;
    private Button getCodeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_register);
        init();
        SMSSDK.initSDK(this, "1af1a4e90dbee", "9db28e13a40c1d70c423b5f478a03f25");
        EventHandler eh = new EventHandler() {


            @Override

            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handlerIdentifyCode.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eh);//注册短信回调
    }

    private void init() {
        PercentRelativeLayout registerLayout = (PercentRelativeLayout) findViewById(R.id.activity_register);
        registerLayout.setOnClickListener(this);
        inputPassword1EditText = (EditText) findViewById(R.id.input_password1_edittext);
        inputPassword2EditText = (EditText) findViewById(R.id.input_password2_edittext);
        Button finishRegisterButton = (Button) findViewById(R.id.finish_register_button);
        finishRegisterButton.setOnClickListener(this);
        getCodeButton = (Button) findViewById(R.id.get_code_button);
        getCodeButton.setOnClickListener(this);
        phoneNumEditText = (EditText) findViewById(R.id.input_phoneNum_edittext);
        identifyCodeEditText = (EditText) findViewById(R.id.input_identify_code_edittext);
    }


    @Override
    public void onClick(View view) {
        MyUtils.hideKeyboard(this);
        switch (view.getId()) {
            case R.id.get_code_button:
                if (!TextUtils.isEmpty(phoneNumEditText.getText().toString().trim())) {
                    if (phoneNumEditText.getText().toString().trim().length() == 11) {
                        mobile = phoneNumEditText.getText().toString().trim();
                        SMSSDK.getVerificationCode("86", mobile);//请求获取短信验证码，在监听中返回
                        identifyCodeEditText.requestFocus();
                        getCodeButton.setClickable(false);//不可点击
                        ifRestore = false;
                        handlerText.sendEmptyMessage(START_COUNT_DOWN);//开始倒计时
                    } else {
                        //长度不等于11位
                        Toast.makeText(RegisterActivity.this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
                        phoneNumEditText.requestFocus();
                    }
                } else {
                    //电话号码为空
                    Toast.makeText(RegisterActivity.this, "请输入您的手机号码", Toast.LENGTH_SHORT).show();
                    phoneNumEditText.requestFocus();
                }
                break;
            case R.id.finish_register_button:
                if (!ifIdentifyTrue) {
                    if (mobile == null) {
                        Toast.makeText(RegisterActivity.this, "请输入您的手机号码", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!TextUtils.isEmpty(identifyCodeEditText.getText().toString().trim())) {
                            if (identifyCodeEditText.getText().toString().trim().length() == 4) {
                                if (inputPassword1EditText.getText().toString().trim().length() < 8) {
                                    Toast.makeText(RegisterActivity.this, "密码长度为8~16位", Toast.LENGTH_SHORT).show();
                                } else {
                                    ifInputPassword = true;
                                    if (!inputPassword2EditText.getText().toString().equals(inputPassword1EditText.getText().toString())) {
                                        Toast.makeText(RegisterActivity.this, "两次密码不同", Toast.LENGTH_SHORT).show();
                                    } else {
                                        ifPasswordSame = true;
                                        password = inputPassword2EditText.getText().toString().trim();
                                        String iCode = identifyCodeEditText.getText().toString().trim();
                                        SMSSDK.submitVerificationCode("86", mobile, iCode);//提交短信验证码
                                    }
                                }
                            } else {
                                Toast.makeText(RegisterActivity.this, "请输入完整验证码", Toast.LENGTH_SHORT).show();
                                identifyCodeEditText.requestFocus();
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "请输入验证码", Toast.LENGTH_SHORT).show();
                            identifyCodeEditText.requestFocus();
                        }
                    }
                }
                break;
            default:
                break;
        }
    }


    private Handler handlerText = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_COUNT_DOWN:
                    if (time > 0) {
                        time--;
                        getCodeButton.setText("已发送" + time + "秒");
                        if (!ifRestore) {
                            handlerText.sendEmptyMessageDelayed(START_COUNT_DOWN, 1000);
                        }
                    } else {
                        time = 30;
                        getCodeButton.setText("获取验证码");
                        getCodeButton.setClickable(true);
                    }
                    break;
                case RESTORE_TIME:
                    identifyCodeEditText.setText("");//清空验证码
                    time = 30;
                    getCodeButton.setText("获取验证码");
                    getCodeButton.setClickable(true);
                    break;
            }
        }
    };


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REGISTER_ERROR:
                    Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                    break;
                case SERVER_ERROR:
                    Toast.makeText(RegisterActivity.this, "服务器异常", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private Handler handlerIdentifyCode = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            if (result == SMSSDK.RESULT_COMPLETE) {//回调完成
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//提交验证码成功,验证通过
                    Toast.makeText(getApplicationContext(), "验证码校验成功", Toast.LENGTH_SHORT).show();
                    //   flag = true;
                    handlerText.sendEmptyMessage(RESTORE_TIME);//还原初始状态
                    ifIdentifyTrue = true;
                    sendRegisterJson();
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {//服务器验证码发送成功
                    Toast.makeText(getApplicationContext(), "验证码已经发送有效时间为5分钟", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 根据服务器返回的网络错误，给toast提示
                try {
                    Throwable throwable = (Throwable) data;
                    throwable.printStackTrace();
                    JSONObject object = new JSONObject(throwable.getMessage());
                    String des = object.optString("detail");//错误描述
                    int status = object.optInt("status");//错误代码
                    if (status > 0 && !TextUtils.isEmpty(des)) {
                        Toast.makeText(RegisterActivity.this, des, Toast.LENGTH_SHORT).show();
                        ifRestore = true;
                        handlerText.sendEmptyMessageDelayed(RESTORE_TIME, 1000);//重制获取验证码
                        getCodeButton.setClickable(true);
                    }
                } catch (Exception e) {
                    //do something
                }
            }
        }
    };

    private void sendRegisterJson() {
        if (ifIdentifyTrue && ifInputPassword & ifPasswordSame) {
            JSONObject registerJson = new JSONObject();
            try {

                registerJson.put("mobile", mobile);
                registerJson.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final CustomProgressDialog myProgressDialog = new CustomProgressDialog(RegisterActivity.this, R.style.MyDialog);
            myProgressDialog.createDialog(getWindowManager()).show();
            OkHttpUtils.doPost(Apis.register, registerJson, new HttpDealResponse() {
                @Override
                public void dealResponse(ResponseBody responseBody) {
                    try {
                        myProgressDialog.dismiss();
                        JSONObject responseJson = new JSONObject(responseBody.string());
                        if ("false".equals(responseJson.getString("success"))) {
                            handler.sendEmptyMessage(REGISTER_ERROR);
                        } else {
                            MyUtils.getUserInfoStore().edit().putString("mobile", mobile).putString("password", password).putString("username", "未知").apply();
                            CharacterInfo.setMobile(mobile);
                            startActivity(new Intent(RegisterActivity.this, GameActivity.class));//跳转到游戏界面
                        }
                    } catch (JSONException e) {
                        handler.sendEmptyMessage(SERVER_ERROR);
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void dealError(Exception e) {
                    myProgressDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this, "连接超时", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }
}