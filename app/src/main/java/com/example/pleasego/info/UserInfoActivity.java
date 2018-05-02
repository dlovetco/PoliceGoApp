package com.example.pleasego.info;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pleasego.R;
import com.example.pleasego.utils.Apis;
import com.example.pleasego.utils.HttpDealResponse;
import com.example.pleasego.utils.ImageUtils;
import com.example.pleasego.utils.MyUtils;
import com.example.pleasego.utils.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private Uri imageUri;//图片存储的路径
    private File file;//需要存储的图片文件

    private ImageView headPicture_iv;
    private TextView username_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        file = new File(Environment.getExternalStorageDirectory(), "headPicture.jpg");//新建一个文件（路径，文件名称）
        imageUri = Uri.fromFile(file);
        headPicture_iv = (ImageView) findViewById(R.id.headpicture);
        headPicture_iv.setOnClickListener(this);
        if (Boolean.parseBoolean(MyUtils.getUserInfoStore().getString("headPicture", "false"))) {
            try {
                Bitmap bitmap = ImageUtils.toRoundBitmap(BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri)));
                headPicture_iv.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        username_tv = (TextView) findViewById(R.id.username);
        username_tv.setOnClickListener(this);
        username_tv.setText(MyUtils.getUserInfoStore().getString("username", "未知"));//初始化昵称
        TextView gunNum_tv = (TextView) findViewById(R.id.gun_num);
        TextView grenadeNum_tv = (TextView) findViewById(R.id.grenade_num);
        gunNum_tv.setText(CharacterInfo.getGun() + "");
        grenadeNum_tv.setText(CharacterInfo.getGrenade() + "");
        TextView money_tv = (TextView) findViewById(R.id.money_tv);
        TextView energy_tv = (TextView) findViewById(R.id.energy_tv);

        money_tv.setText(CharacterInfo.getMoney() + "");
        energy_tv.setText(CharacterInfo.getEnergy() + "");

        ProgressBar money_pb = (ProgressBar) findViewById(R.id.money);
        money_pb.setProgress(CharacterInfo.getMoney());
        ProgressBar energy_pb = (ProgressBar) findViewById(R.id.energy);
        energy_pb.setProgress((int) (CharacterInfo.getEnergy()));
    }

    public void goback(View view) {
        this.finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.headpicture:
                String string[] = {"拍照", "从相册获取"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("选择您的头像方式");
                builder.setItems(string, new DialogInterface.OnClickListener() {//(实现了charsequence的类数组这里用string数组，一个点击事件的监听器)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                //拍照
                                Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//打开系统照相机
                                openCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivityForResult(openCamera, 0);
                                break;
                            case 1:
                                //从相册里选择
                                Intent chooseInAlbum = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(chooseInAlbum, 1);
                                break;
                        }
                    }
                });
                builder.show();
                break;
            case R.id.username:
                final EditText et = new EditText(this);
                final AlertDialog.Builder editUserNameDialog = new AlertDialog.Builder(this);
                editUserNameDialog.setTitle("请输出您的名字").setView(et);
                editUserNameDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String tempUserName = et.getText().toString();
                        JSONObject updateUserNameJson = new JSONObject();
                        try {
                            updateUserNameJson.put("mobile", CharacterInfo.getMobile());
                            updateUserNameJson.put("username", tempUserName);
                            OkHttpUtils.doPost(Apis.updateUsername, updateUserNameJson, new HttpDealResponse() {
                                @Override
                                public void dealResponse(ResponseBody responseBody) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(responseBody.string());
                                        if ("true".equals(jsonObject.getString("success"))) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    username_tv.setText(tempUserName);
                                                    CharacterInfo.setUsername(tempUserName);
                                                    MyUtils.getUserInfoStore().edit().putString("username", tempUserName).apply();
                                                }
                                            });

                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void dealError(Exception e) {

                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                editUserNameDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                editUserNameDialog.show();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                //拍照
                if (resultCode == RESULT_OK) {
                    cropPicture(imageUri);
                }
                break;
            case 1:
                //从相册获取
                if (resultCode == RESULT_OK) {
                    cropPicture(data.getData());
                }
                break;
            case 2:
                //裁剪之后
                if (resultCode == RESULT_OK) {
                    postHeadPicture();
                    setHeadPicture();
                }
                break;
        }
    }

    private void setHeadPicture() {
        try {
            //根据imageUri用getContentResolver来获取流对象 再转化成bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            bitmap = ImageUtils.toRoundBitmap(bitmap);
            if (bitmap == null) {
                //判断bitmap是否为空
                Toast.makeText(this, "图像没有存储到sd卡根目录", Toast.LENGTH_SHORT).show();
            }
            headPicture_iv.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {

        }
    }

    private void postHeadPicture() {
        if (!file.exists()) {
            Toast.makeText(this, "图片不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("headPicture", CharacterInfo.getMobile() + ".png", RequestBody.create(MediaType.parse("image/png"), file)).build();

        Request.Builder builder = new Request.Builder().url(Apis.updateHeadPicture).post(body);
        final Request request = builder.build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UserInfoActivity.this, "上传图片失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MyUtils.getUserInfoStore().edit().putString("headPicture", "true").apply();
                Log.d("HttpResponse", response.body().string());
            }
        });
    }

    private void cropPicture(Uri uri) {
        //新建一个表示裁剪的Intent
        Intent intent = new Intent("com.android.camera.action.CROP");
        //表明我要裁剪的目标是uri这个地址，文件类型是图片
        intent.setDataAndType(uri, "image/*");
        //指定长宽的比例为1:1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //指定宽高为500
        intent.putExtra("outputX", 500);
        intent.putExtra("outputY", 500);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, 2);
    }
}
