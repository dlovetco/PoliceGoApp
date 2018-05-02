package com.example.pleasego.utils;

import android.util.Log;
import android.widget.Toast;

import com.example.pleasego.info.CharacterInfo;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class OkHttpUtils {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void doPost(String url, JSONObject jsonObject, final HttpDealResponse httpDealResponse)
    {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS);//设置5秒连接超时
        OkHttpClient okHttpClient = clientBuilder.build();
        RequestBody requestBody = RequestBody.create(JSON,jsonObject.toString());
        Request request = new Request.Builder().url(url).post(requestBody).build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                httpDealResponse.dealError(e);
                Log.d("HttpFailure",e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                httpDealResponse.dealResponse(response.body());
            }
        });
    }

    //参数为要上传的网址，本地照片在本地的地址，Json字符串，我们自己定义的接口
    public static void doPostPicture(String url, File file, JSONObject jsonObject, final HttpDealResponse httpDealResponse) {
        //1.定义一个OkhttpClient
        OkHttpClient client = new OkHttpClient();
        //2.创建一个请求体
        RequestBody body;
        //3.创建一个请求体建造器
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("mobile", CharacterInfo.getMobile());
        if (file != null) {//若有图片传入则加上图片内容
            builder.addFormDataPart("headPicture", "headPicture.png", RequestBody.create(MediaType.parse("image/png"), file));
        }

        body = builder.build();

        //3.创建一个请求，利用构建器方式添加url和请求体。
        Request request = new Request.Builder().post(body).url(url).build();

        //4.定义一个call，利用okhttpclient的newcall方法来创建对象。因为Call是一个接口不能利用构造器实例化。
        Call call = client.newCall(request);

        //5.这是异步调度方法，上传和接受的工作都在子线程里面运作，如果要使用同步的方法就用call.execute(),此方法返回的就是Response
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                httpDealResponse.dealError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                httpDealResponse.dealResponse(response.body());//把服务器发回来的数据response解析成string传入方法
        }
        });
    }

    public static void doGet(String url,final HttpDealResponse httpDealResponse) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                httpDealResponse.dealError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                httpDealResponse.dealResponse(response.body());
            }
        });
    }
}
