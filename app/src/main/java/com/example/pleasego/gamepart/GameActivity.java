package com.example.pleasego.gamepart;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.example.pleasego.R;
import com.example.pleasego.info.CharacterInfo;
import com.example.pleasego.info.UserInfoActivity;
import com.example.pleasego.utils.Apis;
import com.example.pleasego.utils.AttackChooseDialog;
import com.example.pleasego.utils.CustomProgressDialog;
import com.example.pleasego.utils.HttpDealResponse;
import com.example.pleasego.utils.OkHttpUtils;
import com.example.pleasego.utils.SensorEventHelper;
import com.example.pleasego.utils.UseWeapons;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;

public class GameActivity extends AppCompatActivity implements LocationSource, AMapLocationListener,
        AMap.OnMapLoadedListener, View.OnClickListener,
        AMap.OnMarkerClickListener, AMap.InfoWindowAdapter, AMap.OnInfoWindowClickListener, AMap.OnMapClickListener {

    private MapView mapView;
    private AMap aMap;
    private AMapLocationClient mLocationClient;
    private LatLng mLocation = new LatLng(0, 0);// 当前位置
    private LocationSource.OnLocationChangedListener mListener;// 定位监听器
    private Marker mLocMarker;// 自身的图标
    private Marker showMarker;//展开的图标
    private int otherMoneyNum;
    private List<Marker> otherMarkers = new ArrayList<>();//其他人的图标
    private View view;

    private Button radar_imgv;
    private Button topList_btn;
    private int satellites;//卫星数量
    private int gpsStatus;//卫星强度
    private String address;//自身当前地址


    //自身图标动画相关
    private TimerTask mTimerTask;
    private Timer mTimer = new Timer();
    private Circle ac;
    private Circle c;
    private long startCircle;//动画的时间
    private final Interpolator interpolator1 = new LinearInterpolator();//圆动画的线性加速器

    private boolean mFirstFix = false;// 是否添加过自身marker
    private boolean isGaming = true;//判断是否处于游戏状态
    private boolean ifCanExit = false;//能否退出游戏

    private CustomProgressDialog loadingDialog;
    private LocationManager locationManager;
    private Vibrator vibrator;// 手机振动
    private SensorEventHelper mySensorHelper;// 传感器
    private static long exitTime;// 记录退出时间

    private final int GUN = 1;
    private final int GRENADE = 2;

    private final int SERVER_ERROR = 0;//服务器异常
    private final int NET_CONNECTION_ERROR = 1;//网络连接异常

    private Map<String, LatLng> otherLocationsMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        view = LayoutInflater.from(this).inflate(R.layout.activity_game, null);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(R.layout.activity_game);
        mapView = (MapView) findViewById(R.id.map);

        mapView.onCreate(savedInstanceState);

        if (aMap == null) {
            aMap = mapView.getMap();// 得到地图对象
        }
        init();
    }

    /**
     * 总的初始化
     */
    private void init() {
        initAmap();
        initUiSettings();
        initLocationStyle();
        initOthers();
        initGPS();
    }

    /**
     * 初始化一些杂七杂八的东西
     */
    private void initOthers() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);// 获取震动服务
        loadingDialog = new CustomProgressDialog(this, R.style.MyDialog);
        loadingDialog.createDialog(getWindowManager()).show();
        mySensorHelper = new SensorEventHelper(this);
        mySensorHelper.registerSensorListener();//注册传感器

        topList_btn = (Button) findViewById(R.id.top_list);
        topList_btn.setOnClickListener(this);
    }

    /**
     * 初始化Amap的UISetting
     */
    private void initUiSettings() {
        UiSettings settings = aMap.getUiSettings();// 自带UI组件
        settings.setMyLocationButtonEnabled(true);// 不设置定位按钮
        settings.setScaleControlsEnabled(true);// 显示比例尺控件
        settings.setZoomControlsEnabled(false);// 设置有无缩放级别控件
        settings.setCompassEnabled(false);// 不显示指南针
        settings.setZoomGesturesEnabled(false);// 不允许缩放
        settings.setScrollGesturesEnabled(true);// 允许滑动
        settings.setTiltGesturesEnabled(false);// 不允许倾斜
        settings.setRotateGesturesEnabled(true);// 允许旋转
        settings.setGestureScaleByMapCenter(false);//缩放以地图中心为中心
    }

    /**
     * 初始化LocationStyle
     */
    private void initLocationStyle() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.temp));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.alpha(0));// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.alpha(0));// 设置圆形的填充颜色
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//设置旋转跟随 不移动到中间的模式
        aMap.setMyLocationStyle(myLocationStyle);
    }

    /**
     * 初始化Amap的设置
     */
    private void initAmap() {
        aMap.setOnMapClickListener(this);
        aMap.setInfoWindowAdapter(this);//设置自定义弹出窗口
        aMap.setOnInfoWindowClickListener(this);
        aMap.showIndoorMap(true);//显示室内地图
        aMap.setLocationSource(this);// 设置定位监听
        aMap.setOnMarkerClickListener(this);//设置地图标签点击事件
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setOnMapLoadedListener(this);//地图加载完毕监听
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 18) {
            aMap.setMapType(AMap.MAP_TYPE_NAVI);// 导航模式
        } else {
            aMap.setMapType(AMap.MAP_TYPE_NIGHT);// 夜间模式
        }
        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //点击地图的监听回调
            }
        });
        aMap.setOnMapLongClickListener(new AMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                startActivity(new Intent(GameActivity.this, UserInfoActivity.class));
            }
        });
    }

    /**
     * 判断GPS模块是否开启，如果没有则开启
     */
    private void initGPS() {
        if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("请打开GPS并选择高精度模式");
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {

                    // 转到手机设置界面，用户设置GPS
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
                }
            });
            dialog.show();
        }
    }

    /**
     * 从服务器获取用户的武器数量
     */
    private void initUserWeapons() {
        OkHttpUtils.doGet(Apis.getUserWeapons + "?mobile=" + CharacterInfo.getMobile(), new HttpDealResponse() {
            @Override
            public void dealResponse(ResponseBody responseBody) {
                try {
                    JSONObject jsonObject = new JSONObject(responseBody.string());
                    if ("true".equals(jsonObject.getString("success"))) {
                        JSONObject weapons = jsonObject.getJSONObject("data");
                        CharacterInfo.setGun(Integer.parseInt(weapons.getString("gun")));
                        CharacterInfo.setGrenade(Integer.parseInt(weapons.getString("grenade")));
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void dealError(Exception e) {
                errorHandler.sendEmptyMessage(NET_CONNECTION_ERROR);
            }
        });
    }

    /**
     * 实时从服务器获取用户的能量以及金币用来判断自己是否被人攻击了
     */
    private void initUserSrcInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isGaming) {
                    OkHttpUtils.doGet(Apis.getUserSrc + "?mobile=" + CharacterInfo.getMobile(), new HttpDealResponse() {
                        @Override
                        public void dealResponse(ResponseBody responseBody) {
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(responseBody.string());
                                if ("true".equals(jsonObject.getString("success"))) {
                                    JSONObject src = jsonObject.getJSONObject("data");
                                    ToastAttacked(Integer.parseInt(src.getString("money")));
                                    CharacterInfo.setEnergy(Integer.parseInt(src.getString("energy")));
                                }
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void dealError(Exception e) {
                            errorHandler.sendEmptyMessage(NET_CONNECTION_ERROR);
                        }
                    });
                    try {
                        Thread.sleep(10000);//每十秒获取一次
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    /**
     * 提示用户是否被攻击
     */
    private void ToastAttacked(int newMoney) {
        //判断服务器数据是否跟本地一样，若本地金币大于服务器数据 则认定自己被攻击
        //并更新自己本地数据
        if (newMoney < CharacterInfo.getMoney()) {
            vibrator.vibrate(1000);//抖动一秒表示被攻击
            Toast.makeText(GameActivity.this, "哇！有人搞你！看看自己钱少了多少!/n凶手就在附近，炸他！！！", Toast.LENGTH_SHORT).show();
        }
        CharacterInfo.setMoney(newMoney);
    }

    /**
     * 出现错误时的提示信息
     */
    Handler errorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SERVER_ERROR:
                    Toast.makeText(GameActivity.this, "服务器或网络异常", Toast.LENGTH_SHORT).show();
                    break;
                case NET_CONNECTION_ERROR:
                    Toast.makeText(GameActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * 开始定位
     *
     * @param onLocationChangedListener
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        if (mListener == null) {
            mListener = onLocationChangedListener;
        }
        initLocationClient();
    }

    /**
     * 初始化LocationClient
     */
    private void initLocationClient() {
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(getApplicationContext());
            mLocationClient.setLocationListener(this);// 设置定位监听
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
            mLocationOption.setSensorEnable(true);//允许使用传感器
            mLocationOption.setInterval(1000);// 设置定位间隔,单位毫秒,默认为2000ms小于1000ms不生效
//            mLocationOption.setOnceLocation(true);
//            mLocationOption.setOnceLocationLatest(true);
            // 设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
        }
    }

    /**
     * 结束定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
    }

    /**
     * 每一次定位回调
     *
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点 定位按钮才有用
            if (mTimerTask != null) {
                mTimerTask.cancel();
                mTimerTask = null;
            }
            if (aMapLocation.getErrorCode() == 0) {
                mLocation = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());// 当前的坐标
                if (!mFirstFix) {
                    //第一次定位
                    mFirstFix = true;
                    mLocMarker = addMarkers(mLocation, BitmapFactory.decodeResource(this.getResources(), R.drawable.my_location));
                    ac = aMap.addCircle(new CircleOptions().center(mLocation).fillColor(Color.argb(100, 255, 218, 185)).radius(0)
                            .strokeColor(Color.argb(255, 255, 228, 185)).strokeWidth(5));
                    c = aMap.addCircle(new CircleOptions().center(mLocation).fillColor(Color.argb(70, 255, 218, 185)).radius(20)
                            .strokeColor(Color.argb(255, 255, 228, 185)).strokeWidth(0));
                    mySensorHelper.setCurrentMarker(mLocMarker);// 定位图标旋转
                    CameraPosition cp = new CameraPosition.Builder()
                            .target(mLocation).zoom(19f).tilt(90).build();
                    aMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
                    loadingDialog.dismiss();

                    //第一次定位成功之后开始向服务器获取数据
                    initUserSrcInfo();
                    initUserWeapons();
                    updateEnergy();
                    startCalculateEnergy();
                    startUpdateCoordinate();
                    searchOthersPlayers();
                } else {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.changeTilt(90);
                    aMap.moveCamera(cameraUpdate);
                    cameraUpdate = CameraUpdateFactory.zoomTo(19f);
                    aMap.moveCamera(cameraUpdate);
                    mLocMarker.setPosition(mLocation);
                    ac.setCenter(mLocation);
                    ac.setRadius(0);
                    c.setCenter(mLocation);
                    c.setRadius(10);
                }
//                satellites = aMapLocation.getSatellites();//获取卫星数量
//                gpsStatus = aMapLocation.getGpsAccuracyStatus();//获取卫星信号强度
//                address = aMapLocation.getAddress();//获取自身当前地址
                scaleCircle(c);
            } else {
                // 定位失败
                Toast.makeText(GameActivity.this, "定位失败，错误码:" + aMapLocation.getErrorCode(), Toast.LENGTH_SHORT).show();
                Log.e("exception", aMapLocation.getLocationDetail());
            }
        }
    }

    /**
     * 开始记录用户运动距离（能量）
     */
    private void startCalculateEnergy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isGaming) {
                    LatLng oldPlace = mLocation;
                    try {
                        Thread.sleep(5 * 60 * 1000);
                        LatLng newPlace = mLocation;
                        float distance = AMapUtils.calculateLineDistance(oldPlace, newPlace);
                        //速度在3米每秒之内算是有效的
                        if (distance / 300 < 3) {
                            int energy = CharacterInfo.getEnergy();
                            int oldGunSign = energy / 100;
                            int newGunSign = (int) (energy + distance) / 100;
                            if (newGunSign > oldGunSign) {
                                addWeapons(GUN);
                            }
                            int oldGrenadeSign = energy / 1000;
                            int newGrenadeSign = (int) (energy + distance) / 1000;
                            if (newGrenadeSign > oldGrenadeSign) {
                                addWeapons(GRENADE);
                            }
                            CharacterInfo.addEnergy((int) distance);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 增加用户的武器数量
     */
    private void addWeapons(final int weaponKind) {
        //每当用户能量达到100则增加一发手枪
        //每当用户能量达到1000则增加一发手榴弹
        OkHttpUtils.doGet(Apis.addWeapons + "?mobile=" + CharacterInfo.getMobile() + "weaponKind=" + weaponKind
                , new HttpDealResponse() {
                    @Override
                    public void dealResponse(ResponseBody responseBody) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseBody.string());
                            if ("true".equals(jsonObject.getString("success"))) {
                                switch (weaponKind) {
                                    case GUN:
                                        CharacterInfo.setGun(CharacterInfo.getGun() + 1);
                                        vibrator.vibrate(1000);
                                        Toast.makeText(GameActivity.this, "又是精力满满的一天呢！！手枪+1", Toast.LENGTH_SHORT).show();
                                        break;
                                    case GRENADE:
                                        CharacterInfo.setGrenade(CharacterInfo.getGrenade() + 1);
                                        vibrator.vibrate(1000);
                                        Toast.makeText(GameActivity.this, "又是元气满满的一天呢！！手榴弹+1", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            } else {
                                errorHandler.sendEmptyMessage(SERVER_ERROR);
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void dealError(Exception e) {
                        errorHandler.sendEmptyMessage(NET_CONNECTION_ERROR);
                    }
                });
    }

    /**
     * 每隔30秒更新一次用户运动数据
     */
    private void updateEnergy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isGaming) {
                    OkHttpUtils.doGet(Apis.updateEnergy + "?mobile=" + CharacterInfo.getMobile() + "&energy=" + CharacterInfo.getEnergy()
                            , new HttpDealResponse() {
                                @Override
                                public void dealResponse(ResponseBody responseBody) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(responseBody.string());
                                        if ("false".equals(jsonObject.getString("success"))) {
                                            errorHandler.sendEmptyMessage(SERVER_ERROR);
                                        }
                                    } catch (JSONException | IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void dealError(Exception e) {
                                    errorHandler.sendEmptyMessage(NET_CONNECTION_ERROR);
                                }
                            });
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 开始实时更新自己当前坐标
     */
    private void startUpdateCoordinate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isGaming) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("mobile", CharacterInfo.getMobile());
                        jsonObject.put("latitude", mLocation.latitude + "");
                        jsonObject.put("longitude", mLocation.longitude + "");
                        OkHttpUtils.doPost(Apis.updateCoordinate, jsonObject, new HttpDealResponse() {
                            @Override
                            public void dealResponse(ResponseBody responseBody) {
                                try {
                                    JSONObject json = new JSONObject(responseBody.string());
                                    if ("false".equals(json.getString("success"))) {
                                        Toast.makeText(GameActivity.this, "网络异常,上传自身数据失败", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    errorHandler.sendEmptyMessage(SERVER_ERROR);
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
            }
        }).start();
    }

    /**
     * 开始寻找附近玩家
     */
    private void searchOthersPlayers() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isGaming) {
                    JSONObject findOtherJson = new JSONObject();
                    try {
                        findOtherJson.put("mobile", CharacterInfo.getMobile());
                        findOtherJson.put("longitude", mLocation.longitude);
                        findOtherJson.put("latitude", mLocation.latitude);
                        OkHttpUtils.doPost(Apis.findLocal, findOtherJson, new HttpDealResponse() {
                            @Override
                            public void dealResponse(ResponseBody responseBody) {
                                try {
                                    String responseString = responseBody.string();
                                    final JSONObject jsonObject = new JSONObject(responseString);
                                    if ("false".equals(jsonObject.getString("success"))) {
                                        final String message = jsonObject.getString("message");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(GameActivity.this, message, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        String array = jsonObject.getString("data");
                                        JSONArray jsonArray = new JSONArray(array);
                                        int size = jsonArray.length();
                                        JSONObject other = null;
                                        for (int i = 0; i < size; i++) {
                                            other = jsonArray.getJSONObject(i);
                                            clearOldMarkers();
                                            otherLocationsMap.put(other.getString("mobile"), new LatLng(other.getDouble("latitude"), other.getDouble("longitude")));
                                            for (Map.Entry<String, LatLng> entry : otherLocationsMap.entrySet()) {
                                                String mobile = entry.getKey();
                                                LatLng location = entry.getValue();
                                                Marker marker = addMarkers(location, BitmapFactory.decodeResource(GameActivity.this.getResources(), R.drawable.other_location));
                                                marker.setObject(mobile);
                                                otherMarkers.add(marker);
                                            }
                                        }
                                    }
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void dealError(Exception e) {

                            }
                        });
                    } catch (JSONException e) {
                        errorHandler.sendEmptyMessage(SERVER_ERROR);
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(10000);//每隔10秒搜索一次
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    /**
     * 清除原来的marker
     */
    private void clearOldMarkers() {
        int size = otherMarkers.size();
        for (int i = 0; i < size; i++) {
            otherMarkers.get(i).remove();
        }
        otherMarkers.clear();
    }

    /**
     * 是否开启gps的回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(this, "若不开启GPS可能会有糟糕的游戏体验！！！", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * marker的点击时间回调
     *
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        showMarker = marker;
        return false;
    }

    /**
     * 点击图标显示其他玩家的头像昵称和金币数量
     *
     * @param marker
     * @return
     */
    @Override
    public View getInfoWindow(Marker marker) {
        View infoWindow = getLayoutInflater().inflate(R.layout.marker_infowindow, null);
        render(infoWindow, marker);
        return infoWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View infoWindow = getLayoutInflater().inflate(R.layout.marker_infowindow, null);
        render(infoWindow, marker);
        return infoWindow;
    }

    /**
     * 自定义infoWindow窗口
     *
     * @param view
     * @param marker
     */
    private void render(View view, Marker marker) {
        final String otherMobile = (String) marker.getObject();//目标的电话号码
        final ImageView otherHeadPicture_iv = (ImageView) view.findViewById(R.id.other_headpicture);
        final TextView otherUserName = (TextView) view.findViewById(R.id.other_username);
        final TextView otherMoney = (TextView) view.findViewById(R.id.other_money);

        //获取其他用户的昵称和金币数量
        OkHttpUtils.doGet(Apis.getOtherBasicInfo + "?otherMobile=" + otherMobile, new HttpDealResponse() {
            @Override
            public void dealResponse(final ResponseBody responseBody) {
                try {
                    final JSONObject jsonObject = new JSONObject(responseBody.string());
                    if ("true".equals(jsonObject.getString("success"))) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject info = jsonObject.getJSONObject("data");
                                    otherUserName.setText(info.getString("username"));
                                    otherMoneyNum = info.getInt("money");
                                    otherMoney.setText(otherMoneyNum + "");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void dealError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GameActivity.this, "服务器异常", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

        //获取其他用户的头像
        OkHttpUtils.doGet(Apis.getOtherHeadPicture + "?otherMobile=" + otherMobile, new HttpDealResponse() {
            @Override
            public void dealResponse(final ResponseBody responseBody) {
                InputStream inputStream = responseBody.byteStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] bytes = new byte[1024];
                int length;
                try {
                    while ((length = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, length);
                    }
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BitmapFactory.Options options = new BitmapFactory.Options();
                //设置计算得到的压缩比例
                options.inSampleSize = 2;
                //设置为false，确保可以得到bitmap != null
                options.inJustDecodeBounds = false;
                byte[] bytes1 = outputStream.toByteArray();
                final Bitmap otherHeadPicture = BitmapFactory.decodeByteArray(bytes1, 0, bytes1.length, options);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        otherHeadPicture_iv.setImageBitmap(otherHeadPicture);
                    }
                });
            }

            @Override
            public void dealError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GameActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Button attack = (Button) view.findViewById(R.id.attack);
        attack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AttackChooseDialog attackChooseDialog = new AttackChooseDialog(GameActivity.this);
                attackChooseDialog.createDialog(new UseWeapons() {
                    @Override
                    public void setUseGunButton() {
                        int gunNum = CharacterInfo.getGun();
                        double energy = CharacterInfo.getEnergy();
                        if (gunNum < 1) {
                            Toast.makeText(GameActivity.this, "弹尽粮绝啦大兄弟~", Toast.LENGTH_SHORT).show();
                        } else {
                            if (energy < 100) {
                                Toast.makeText(GameActivity.this, "身体瘦弱得连枪都举不动啦~", Toast.LENGTH_SHORT).show();
                            } else {
                                double rate = Math.random() / 10 / GUN;//掉钱的比例
                                int stolenMoneyNum = (int) (otherMoneyNum * rate);
                                attack(otherMobile, GUN, stolenMoneyNum);
                            }
                        }
                        attackChooseDialog.dismiss();
                    }

                    @Override
                    public void setUseGrenadeButton() {
                        int grenadeNum = CharacterInfo.getGrenade();
                        double energy = CharacterInfo.getEnergy();
                        if (grenadeNum < 1) {
                            Toast.makeText(GameActivity.this, "弹尽粮绝啦大兄弟~", Toast.LENGTH_SHORT).show();
                        } else {
                            if (energy < 1000) {
                                Toast.makeText(GameActivity.this, "没能量导致手榴弹卡在档下面掏不出来啦~", Toast.LENGTH_SHORT).show();
                            } else {
                                double rate = Math.random() / 10 / GRENADE;//掉钱的比例
                                int stolenMoneyNum = (int) (otherMoneyNum * rate);
                                attack(otherMobile, GRENADE, stolenMoneyNum);
                            }
                        }
                        attackChooseDialog.dismiss();
                    }
                }).show();
            }
        });
    }

    /**
     * 攻击其他人
     *
     * @param otherMobile
     * @param weaponKind
     * @param stolenMoneyNum
     */
    private void attack(String otherMobile, final int weaponKind, final int stolenMoneyNum) {

        OkHttpUtils.doGet(Apis.updateInfoAfterAttack + "?mobile=" + CharacterInfo.getMobile()
                + "&stolenMoneyNum=" + stolenMoneyNum + "&otherMobile=" + otherMobile + "&weaponKind=" + weaponKind, new HttpDealResponse() {
            @Override
            public void dealResponse(ResponseBody responseBody) {
                try {
                    JSONObject jsonObject = new JSONObject(responseBody.string());
                    if ("true".equals(jsonObject.getString("success"))) {
                        if (weaponKind == GUN) {
                            CharacterInfo.setEnergy(CharacterInfo.getEnergy() - 100);
                            CharacterInfo.setMoney(CharacterInfo.getMoney() + stolenMoneyNum);
                            CharacterInfo.setGun(CharacterInfo.getGun() - 1);
                        } else if (weaponKind == GRENADE) {
                            CharacterInfo.setEnergy(CharacterInfo.getEnergy() - 1000);
                            CharacterInfo.setMoney(CharacterInfo.getMoney() + stolenMoneyNum);
                            CharacterInfo.setGun(CharacterInfo.getGrenade() - 1);
                        }
                    }
                } catch (JSONException | IOException e) {
                    errorHandler.sendEmptyMessage(SERVER_ERROR);
                    e.printStackTrace();
                }
            }

            @Override
            public void dealError(Exception e) {
                Toast.makeText(GameActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 点击infowindow的回调
     *
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    /**
     * 点击地图事件
     *
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        showMarker.hideInfoWindow();
    }

    /**
     * 定位范围定时动画内部类
     */
    private class circleTask extends TimerTask {
        private double r;
        private Circle circle;
        private long duration = 1000;

        circleTask(Circle circle, long rate) {
            this.circle = circle;
            this.r = circle.getRadius();
            if (rate > 0) {
                this.duration = rate;
            }
        }

        @Override
        public void run() {
            try {
                long elapsed = SystemClock.uptimeMillis() - startCircle;
                float input = (float) elapsed / duration;
                float t = interpolator1.getInterpolation(input);
                double r1 = (t + 1) * r;
                circle.setRadius(r1);
                if (input > 2) {
                    startCircle = SystemClock.uptimeMillis();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开启定时动画
     *
     * @param circle
     */
    public void scaleCircle(final Circle circle) {
        startCircle = SystemClock.uptimeMillis();
        mTimerTask = new circleTask(circle, 1000);
        mTimer.schedule(mTimerTask, 0, 30);
    }

    /**
     * 添加marker
     *
     * @param latlng
     * @param bitMap
     * @return
     */
    private Marker addMarkers(LatLng latlng, Bitmap bitMap) {
        BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bitMap);
        return aMap.addMarker(new MarkerOptions().position(latlng).icon(des).anchor(0.5f,0.5f).setFlat(true));
    }

    /**
     * 地图装载完毕回调
     */
    @Override
    public void onMapLoaded() {

    }

    /**
     * 整体点击事件回调
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.top_list:
                startActivity(new Intent(this, TopListActivity.class));
                break;
        }
    }

    /**
     * 页面被销毁
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isGaming = false;
        mapView.onDestroy();
        deactivate();
        mySensorHelper.unRegisterSensorListener();
    }

    /**
     * 按下两次返回键退出程序
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 判断是否退出程序
     */
    public void exit() {
        if (!ifCanExit) {
            ifCanExit = true;
            return;
        }
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            isGaming = false;
            System.exit(0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
