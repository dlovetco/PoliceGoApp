package com.example.pleasego.utils;

import android.app.Application;
import android.content.Context;

/**
 * Created by dlovetco on 2017/9/19.
 */

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        //获取Context
        context = getApplicationContext();
    }

    //返回
    public static Context getContext(){
        return context;
    }
}
