package com.example.pleasego.info;

/**
 * Created by 马杭辉 on 2017/3/16.
 */

public class BagItem {

    private int type;//道具类型

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private String name;//道具名字

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    private int sellPrice;

    public int getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(int sellPrice) {
        this.sellPrice = sellPrice;
    }

    private int buyPrice;

    public int getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(int buyPrice) {
        this.buyPrice = buyPrice;
    }

    private int itemCount;

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
