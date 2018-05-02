package com.example.pleasego.loginpart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;

import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.content.ContentValues.TAG;

public class LoginActivity extends Activity implements View.OnClickListener {


    private ImageView registerImageView;
    private EditText inputPhoneNumEditText;
    private EditText inputPasswordEditText;
    private Button loginButton;

    private String password;
    private String mobile;

    private final int LOGIN_ERROR = 1;
    private final int SERVER_ERROR = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_login);

        MyUtils.setUserInfoStore(getSharedPreferences("userInfo", Context.MODE_PRIVATE));
        mobile = MyUtils.getUserInfoStore().getString("mobile", "");
        password = MyUtils.getUserInfoStore().getString("password", "");
        CharacterInfo.setUsername(MyUtils.getUserInfoStore().getString("userName", ""));
        init();
        assetsToSd();
    }

    private void assetsToSd() {
        //若文件不在则从assets复制一份到sd卡中
        File file = MyUtils.night_background;
        if (file.exists()) {
            return;
        }
        AssetManager assetManager = this.getAssets();
        try {
            InputStream inputStream =  assetManager.open("mystyle_sdk_1505905740_0100.data");

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] byteBuffer = new byte[1024];
            while (inputStream.read(byteBuffer)!=-1) {
                outputStream.write(byteBuffer);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void init() {
        registerImageView = (ImageView) findViewById(R.id.register_imageview);
        registerImageView.setOnClickListener(this);
        PercentRelativeLayout loginLayout = (PercentRelativeLayout) findViewById(R.id.activity_login);
        loginLayout.setOnClickListener(this);
        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);
        inputPhoneNumEditText = (EditText) findViewById(R.id.login_input_phoneNum_edit);
        inputPhoneNumEditText.setText(mobile);
        inputPasswordEditText = (EditText) findViewById(R.id.login_input_password_edit);
        inputPasswordEditText.setText(password);
    }

    @Override
    public void onClick(View view) {
        MyUtils.hideKeyboard(this);
        switch (view.getId()) {
            case R.id.register_imageview:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));//跳转到注册页面
                break;
            case R.id.login_button:
                //进入游戏界面
                JSONObject loginJson = new JSONObject();//用户信息的json字符串
                if (inputPhoneNumEditText.getText().length() > 0) {
                    mobile = inputPhoneNumEditText.getText().toString();
                    if (inputPasswordEditText.getText().length() > 0) {
                        password = inputPasswordEditText.getText().toString();
                        try {
                            loginJson.put("mobile", mobile);
                            loginJson.put("password", password);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        final CustomProgressDialog myProgressDialog = new CustomProgressDialog(LoginActivity.this, R.style.MyDialog);
                        myProgressDialog.createDialog(getWindowManager()).show();
                        OkHttpUtils.doPost(Apis.login, loginJson, new HttpDealResponse() {
                            @Override
                            public void dealResponse(ResponseBody responseBody) {
                                //关闭loadingdialog
                                myProgressDialog.dismiss();
                                try {
                                    JSONObject jsonObject = new JSONObject(responseBody.string());
                                    if ("false".equals(jsonObject.getString("success"))) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(LoginActivity.this, "用户名或密码不正确", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        MyUtils.getUserInfoStore().edit().putString("mobile", mobile).putString("password", password).apply();
                                        CharacterInfo.setMobile(mobile);
                                        startActivity(new Intent(LoginActivity.this, GameActivity.class));//跳转到游戏界面
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
                                Log.d(TAG, "dealError: ",e);
                                myProgressDialog.dismiss();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this, "连接超时", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "手机号码不能为空", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN_ERROR:
                    Toast.makeText(LoginActivity.this, "手机号不存在或密码错误", Toast.LENGTH_SHORT).show();
                    break;
                case SERVER_ERROR:
                    Toast.makeText(LoginActivity.this, "服务器异常", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
