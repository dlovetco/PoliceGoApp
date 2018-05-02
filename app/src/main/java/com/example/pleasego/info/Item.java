package com.example.pleasego.info;

import com.amap.api.maps.model.LatLng;

/**
 * Created by 马杭辉 on 2017/3/13.
 */

public class Item {

    private String itemId;//道具ID

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    private LatLng location;//道具坐标

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    private int type;//道具类型

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
