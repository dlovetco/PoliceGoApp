package com.example.pleasego.info;

import com.example.pleasego.utils.MyUtils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by 马杭辉 on 2017/3/1.
 */

public class CharacterInfo implements Serializable {

    private static String mobile;

    public static String getMobile() {
        return mobile;
    }

    public static void setMobile(String mobile) {
        CharacterInfo.mobile = mobile;
    }

    private static String username;

    public static void setUsername(String username) {
        CharacterInfo.username = username;
    }

    public static String getUsername() {
        return username;
    }

    public static int getEnergy() {
        return energy;
    }

    public static void setEnergy(int energy) {
        CharacterInfo.energy = energy;
    }

    public static void addEnergy(int energy) {
        CharacterInfo.energy += energy;
    }

    public static int getGrenade() {
        return grenade;
    }

    public static void setGrenade(int grenade) {
        CharacterInfo.grenade = grenade;
    }

    public static int getGun() {
        return gun;
    }

    public static void setGun(int gun) {
        CharacterInfo.gun = gun;
    }

    public static int getMoney() {
        return money;
    }

    public static void setMoney(int money) {
        CharacterInfo.money = money;
    }


    private static int energy;
    private static int money;
    private static int grenade;
    private static int gun;
}
