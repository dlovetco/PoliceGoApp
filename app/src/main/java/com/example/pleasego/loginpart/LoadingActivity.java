package com.example.pleasego.loginpart;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.pleasego.R;
import com.example.pleasego.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;

public class LoadingActivity extends AppCompatActivity {

    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_SMS,Manifest.permission.READ_CONTACTS};
    private List<String> permissionList = new ArrayList<>();
    private boolean showPic = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
       // init();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (showPic)
                {
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(LoadingActivity.this,LoginActivity.class));
            }
        }).start();
        for (String permission:permissions
             ) {
            if (!ifHasPermission(this,permission))
            {
                permissionList.add(permission);
            }
        }
        if (!permissionList.isEmpty())
        {
            //若有权限没有获得则申请
            String[] string = permissionList.toArray(new String[permissionList.size()]);
            requestPermission(string,1);
            Toast.makeText(this,"如果拒绝接受会导致部分功能无法正常使用！！",Toast.LENGTH_SHORT).show();
        }else{
            //若已获得全部权限则直接进入登录界面
            showPic = false;
        }
    }


//    private void init() {
//        MyUtils.initItemHashMap();
//    }

    private boolean ifHasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String[] permission, int code) {
        ActivityCompat.requestPermissions(this, permission, code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                for (int i:grantResults
                     ) {
                    if (i==PackageManager.PERMISSION_DENIED)
                    {
                        Toast.makeText(this,"必须获得所有权限！！",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                showPic = false;
            break;

        }
    }
}
