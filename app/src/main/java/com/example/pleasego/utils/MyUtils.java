package com.example.pleasego.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.SparseArray;
import android.view.inputmethod.InputMethodManager;

import com.example.pleasego.R;

import java.io.File;


public class MyUtils {

//    public static SparseArray<String> getItemNameSparseArray() {
//        return itemNameSparseArray;
//    }
//
//    public static SparseArray<Integer> getItemPicSparseArray() {
//        return itemPicSparseArray;
//    }
//
//    public static SparseArray<String> getItemBuyPriceSparseArray() {
//        return itemBuyPriceSparseArray;
//    }
//
//    public static SparseArray<String> getItemSellPriceSparseArray() {
//        return itemSellPriceSparseArray;
//    }
//
//    public static SparseArray<String> getSpeItemNameSparseArray() {
//        return SpeItemNameSparseArray;
//    }
//
//    public static SparseArray<Integer> getSpeItemPicSparseArray() {
//        return SpeItemPicSparseArray;
//    }
//
//    public static SparseArray<String> getSpeItemBuyPriceSparseArray() {
//        return SpeItemBuyPriceSparseArray;
//    }
//
//    public static SparseArray<String> getSpeItemSellPriceSparseArray() {
//        return SpeItemSellPriceSparseArray;
//    }
//
//    private static SparseArray<String> itemNameSparseArray = new SparseArray<String>();
//
//    private static SparseArray<Integer> itemPicSparseArray = new SparseArray<Integer>();
//
//    private static SparseArray<String> itemBuyPriceSparseArray = new SparseArray<String>();
//
//    private static SparseArray<String> itemSellPriceSparseArray = new SparseArray<String>();
//
//    private static SparseArray<String> SpeItemNameSparseArray = new SparseArray<String>();
//
//    private static SparseArray<Integer> SpeItemPicSparseArray = new SparseArray<Integer>();
//
//    private static SparseArray<String> SpeItemBuyPriceSparseArray = new SparseArray<String>();
//
//    private static SparseArray<String> SpeItemSellPriceSparseArray = new SparseArray<String>();
//
//    public static void initItemHashMap() {
//        itemNameSparseArray.put(0, "树枝");
//        itemNameSparseArray.put(1, "木头");
//        itemNameSparseArray.put(2, "石头");
//        itemNameSparseArray.put(3, "黏土");
//        itemNameSparseArray.put(4, "草");
//        itemNameSparseArray.put(5, "机械组件");
//        itemNameSparseArray.put(6, "玻璃");
//        itemNameSparseArray.put(7, "金子");
//        itemNameSparseArray.put(8, "水晶");
//        itemNameSparseArray.put(9, "宝石");
//
//        itemPicSparseArray.put(0, R.drawable.src0);
//        itemPicSparseArray.put(1, R.drawable.src1);
//        itemPicSparseArray.put(2, R.drawable.src2);
//        itemPicSparseArray.put(3, R.drawable.src3);
//        itemPicSparseArray.put(4, R.drawable.src4);
//        itemPicSparseArray.put(5, R.drawable.src5);
//        itemPicSparseArray.put(6, R.drawable.src6);
//        itemPicSparseArray.put(7, R.drawable.src7);
//        itemPicSparseArray.put(8, R.drawable.src8);
//        itemPicSparseArray.put(9, R.drawable.src9);
//
//        itemBuyPriceSparseArray.put(0, "5");
//        itemBuyPriceSparseArray.put(1, "10");
//        itemBuyPriceSparseArray.put(2, "20");
//        itemBuyPriceSparseArray.put(3, "5");
//        itemBuyPriceSparseArray.put(4, "1");
//        itemBuyPriceSparseArray.put(5, "50");
//        itemBuyPriceSparseArray.put(6, "100");
//        itemBuyPriceSparseArray.put(7, "200");
//        itemBuyPriceSparseArray.put(8, "500");
//        itemBuyPriceSparseArray.put(9, "1000");
//
//        itemSellPriceSparseArray.put(0, "2");
//        itemSellPriceSparseArray.put(1, "5");
//        itemSellPriceSparseArray.put(2, "10");
//        itemSellPriceSparseArray.put(3, "2");
//        itemSellPriceSparseArray.put(4, "1");
//        itemSellPriceSparseArray.put(5, "25");
//        itemSellPriceSparseArray.put(6, "50");
//        itemSellPriceSparseArray.put(7, "100");
//        itemSellPriceSparseArray.put(8, "200");
//        itemSellPriceSparseArray.put(9, "500");
//
//        SpeItemNameSparseArray.put(0, "技能石");
//        SpeItemNameSparseArray.put(1, "炸弹");
//        SpeItemNameSparseArray.put(2, "导弹");
//
//        SpeItemPicSparseArray.put(0, R.drawable.skillstone);
//        SpeItemPicSparseArray.put(1, R.drawable.grenade);
//        SpeItemPicSparseArray.put(2, R.drawable.rocket);
//
//        SpeItemBuyPriceSparseArray.put(0, "200");
//        SpeItemBuyPriceSparseArray.put(1, "200");
//        SpeItemBuyPriceSparseArray.put(2, "500");
//
//        SpeItemSellPriceSparseArray.put(0, "50");
//        SpeItemSellPriceSparseArray.put(1, "50");
//        SpeItemSellPriceSparseArray.put(2, "100");
//
//    }


    private static SharedPreferences userInfoStore;

    public static SharedPreferences getUserInfoStore() {
        return userInfoStore;
    }

    public static void setUserInfoStore(SharedPreferences sharedPreferences) {
        userInfoStore = sharedPreferences;
    }

    public static File night_background =  new File(Environment.getExternalStorageDirectory(), "mystyle_sdk_1505905740_0100.data");

    public static void hideKeyboard(Activity currentActivity) {
        if (currentActivity.getCurrentFocus() != null && currentActivity.getCurrentFocus().getWindowToken() != null) {
            ((InputMethodManager) MyApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(currentActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
