package com.example.pleasego.utils;

import android.app.Activity;

import java.util.ArrayList;

/**
 * Created by 马杭辉 on 2017/3/1.
 */

public class ActivityManager {

    private static ArrayList<Activity> activityList = new ArrayList<Activity>();

    public static void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activityList.remove(activity);
    }

    public static void exit() {
        for (Activity activity :
                activityList) {
            activity.finish();
            activityList.remove(activity);
        }
    }
}
