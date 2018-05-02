package com.example.pleasego.utils;
import okhttp3.ResponseBody;

/**
 * Created by 马杭辉 on 2017/2/27.
 */

public interface HttpDealResponse {

    void dealResponse(ResponseBody responseBody);

    void dealError(Exception e);
}
