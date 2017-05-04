package com.byv7.scef;


import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.SyncStateContract;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.AnimatedVectorDrawable;

import com.amap.api.fence.GeoFence;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;


class thkActivity extends AppCompatActivity {//Activity{

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public Animatable anim(View view) {
        ImageView imageView = (ImageView) view;
        final Drawable drawable = imageView.getDrawable();
        if (drawable instanceof Animatable) {
            return ((Animatable) drawable);
        }

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if(((Animatable) drawable).isRunning() == false)
//                    ((Animatable) drawable).stop();
//            }
//        }, 200);
        return null;
    }
}

public class MainActivity extends thkActivity implements LocationSource, AMapLocationListener {
    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static final String GEOFENCE_BROADCAST_ACTION = "com.example.geofence.round";
    private static final Boolean IS_ARM_CPU = (System.getProperty("os.arch").indexOf("arm") > -1);

    private LinearLayout tvLoc = null;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private DrawerLayout drawerLayout;

    AlarmManager alar;
    PendingIntent pi;
    MapView mMapView;
    AMap aMap;
    LatLng latLng;

    ImageView menu;
    ImageView tips;
    ImageView locator_botton;
    ImageView action;

    ObjectAnimator loc_refersh_anim;

    public MainActivity(){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);// no nav !!! 去除导航栏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  //去除状态栏/通知栏
        setContentView(R.layout.plugin_geofence_map);

        menu = (ImageView) findViewById(R.id.nav_left_menu);
        tips = (ImageView) findViewById(R.id.nav_right_tips);
        locator_botton = (ImageView) findViewById(R.id.btn_loc);
        action = (ImageView) findViewById(R.id.btn_act);

        Log.i("----------mytag", "os.arch: " + System.getProperty("os.arch") + "  " + System.getProperty("os.arch").indexOf("arm"));

        if(IS_ARM_CPU) {
            try {
                initAmap(savedInstanceState);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        adjustDraw();
        initButton();
        initDrawerLayout();
        Log.i("time", String.valueOf(SystemClock.elapsedRealtime()));
    }
    private String getInternalStorageFile(String filename){
        FileInputStream fin = null;

        try {
            fin = openFileInput(filename);
//            String fl[] = fileList();
//            for(int i=0; i<fl.length;i++){
//                Log.i("---------file-list", getFilesDir()+"/"+fl[i]);
//            }

            byte[] buffer = new byte[fin.available()];

            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            while (fin.read(buffer) != -1) {
                arrayOutputStream.write(buffer, 0, buffer.length);
            }
            fin.close();
            arrayOutputStream.close();

            return new String(arrayOutputStream.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }
    private LatLng getInitData(){
        String contents = getInternalStorageFile("latlng");
        if(contents != null && contents.length()>0) {
            String[] ll = contents.split(" ");
            return new LatLng(Double.parseDouble(ll[0]), Double.parseDouble(ll[1]));
        } else {
            return null;
        }
    }
    private void initAmap(Bundle savedInstanceState) throws IOException {
        tvLoc = (LinearLayout) findViewById(R.id.tv_loc);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mMapView.getMap();

        // 恢复配置
        latLng = getInitData();
        if(latLng == null) {
            double lat = 22.977290;
            double lng = 113.337000;
            latLng = new LatLng(lat, lng);
        }
        //参数依次是：视角调整区域的中心点坐标、希望调整到的缩放级别、俯仰角0°~45°（垂直与地图时为0）、偏航角 0~360° (正北方为0)
        CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng,17,0,0));
        aMap.moveCamera(mCameraUpdate);

        // 设置定位监听
        aMap.setLocationSource(this);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);

        //自定义地图
//        aMap.setCustomMapStylePath("/sdcard/custom_config");
//        aMap.setMapCustomEnable(true);//true 开启; false 关闭

        //clear +-
        UiSettings mUiSettings;//定义一个UiSettings对象
        mUiSettings = aMap.getUiSettings();//实例化Ugs类对象
        mUiSettings.setZoomControlsEnabled(false);
    }
    private void initDrawerLayout(){
        drawerLayout = (DrawerLayout)super.findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Color.TRANSPARENT);
        drawerLayout.addDrawerListener(new ActionBarDrawerToggle(this, drawerLayout, R.string.cancel, R.string.showOption){
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        });

        //使用ListView
        ListView list = (ListView)findViewById(R.id.listview);
        String func[] = {"打开定时","关闭定时","交互提示条","最新消息","打开音乐","图片管理","文件管理","滑动刷新","离开"};
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, func);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView arg0, View v, int arg2, long arg3) {
            // TODO Auto-generated method stub
            ListView listView = (ListView) arg0;
            Toast.makeText(MainActivity.this, "ID：" + arg3 + "   選單文字："+ listView.getItemAtPosition(arg2).toString(), Toast.LENGTH_LONG).show();

            Intent intent;
            switch ((int) arg3){
                case 0:
                    /*Calendar c = Calendar.getInstance();
                    c.getTimeInMillis();*/
                    intent = new Intent(MainActivity.this, MusicActivity.class);
                    pi = PendingIntent.getActivity(MainActivity.this,0,intent,0);
                    alar = (AlarmManager) getSystemService(ALARM_SERVICE);
                    alar.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+2000, AlarmManager.INTERVAL_FIFTEEN_MINUTES/5, pi);//INTERVAL_HALF_HOUR
                    break;
                case 1:
                    alar.cancel(pi);
                    //Snackbar.make(v, "把我往右滑动看看会发生什么事", Snackbar.LENGTH_LONG).show();
                    break;
                case 2:
                    Snackbar.make(v, "这是一个可交互的提示条", Snackbar.LENGTH_LONG).setAction("点我", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        Toast.makeText(MainActivity.this, " 您轻轻点了一下Snackbar", Toast.LENGTH_LONG).show();
                        }
                    }).setActionTextColor(Color.YELLOW).show();
                    break;
                case 3:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    intent = new Intent(MainActivity.this, DisplayMessageActivity.class);
                    startActivity(intent);
                    break;
                case 4:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    intent = new Intent(MainActivity.this, MusicActivity.class);
                    startActivity(intent);
                    break;
                case 5:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    intent = new Intent(MainActivity.this, PictureManageActivity.class);
                    startActivity(intent);
                    break;
                case 6:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    intent = new Intent(MainActivity.this, FileManageActivity.class);
                    startActivity(intent);
                    break;
                case 7:
                    drawerLayout.closeDrawer(GravityCompat.START);
                    intent = new Intent(MainActivity.this, RefreshActivity.class);
                    startActivity(intent);
                    break;
                case 8:
                    finish();
                    System.exit(0);
                    break;
            }
            }
        });
    }
    private void initButton() {

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("menu","ok!");
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("tips","ok!");
                Intent intent = new Intent(MainActivity.this, SongInformationListActivity.class);
                startActivity(intent);
            }
        });

        locator_botton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.i("loc","ok!");
                if(IS_ARM_CPU) {
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));//动画移动
                }

                loc_refersh_anim = ObjectAnimator.ofFloat(v,"rotation",360,0);
                loc_refersh_anim.setDuration(2000);
                loc_refersh_anim.setRepeatCount(loc_refersh_anim.INFINITE);//Animation.INFINITE 表示重复多次
                loc_refersh_anim.setRepeatMode(loc_refersh_anim.RESTART);//RESTART表示从头开始，REVERSE表示从末尾倒播
                loc_refersh_anim.start();

                mlocationClient.startLocation();//定位

//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        repeatCheckUntilLocRefershAnimEnd(loc_refersh_anim, v);
//                    }
//                }, 3000);

            }
        });

        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("loc","ok!");
                Intent intent = new Intent(MainActivity.this, Byv7ListActivity.class);
                startActivity(intent);
//                List<Marker> mks = aMap.getMapScreenMarkers();
//                for(int i=0; i<mks.size(); i++) {
//                    Marker mk = mks.get(i);
//                    mk.remove();
//                }
//                CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng,18,0,0));
//                aMap.moveCamera(mCameraUpdate);
//                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            }
        });
    }
    private void repeatCheckUntilLocatorRefershAnimEnd(final ObjectAnimator animator, final View v){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(animator != null) {
                    Log.i("anim-time", String.valueOf(animator.getCurrentPlayTime()));
                    if(100 > animator.getCurrentPlayTime() || animator.getCurrentPlayTime() > 1000) {
                        anim(v).stop();
                        animator.end();
                    } else {
                        repeatCheckUntilLocatorRefershAnimEnd(animator, v);
                    }
                }
            }
        }, 50);
    }

    private void adjustDraw() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.nav);

        //dm
        DisplayMetrics dm = getResources().getDisplayMetrics();
        ActionBar statusBar = getActionBar();

        if(tvLoc != null) {
            //amap height
            LinearLayout.LayoutParams locLP = (LinearLayout.LayoutParams) tvLoc.getLayoutParams();
            locLP.height = 0;
            if (statusBar != null) {
                if (statusBar.isShowing()) {
                    locLP.height = statusBar.getHeight();
                }
            }
            locLP.height = locLP.height + (dm.heightPixels - layout.getLayoutParams().height);
            tvLoc.setLayoutParams(locLP);
        }
        //Log.i("height:", "--" + locLP.height + "--" + dm.heightPixels + "--" + layout.getLayoutParams().height + "--" + getStatusBarHeight(this));
    }
    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener(this);
            //关闭缓存机制
            mLocationOption.setLocationCacheEnable(false);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            //获取一次定位结果：
            //该方法默认为false。
            mLocationOption.setOnceLocation(true);
            //获取最近3s内精度最高的一次定位结果：
            //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
            mLocationOption.setOnceLocationLatest(true);

            //给定位客户端对象设置定位参数
            mlocationClient.setLocationOption(mLocationOption);

            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位
        }
    }
    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }
    private void setFirstInitLatLng(double latitude, double longitude){
        String llll = latitude + " " + longitude;
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("latlng", Context.MODE_PRIVATE);
            fos.write(llll.getBytes());
            fos.close();
//            Log.i("latlng--", String.valueOf(llll));
        } catch (IOException e) {
        }
    }
    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        Log.i("locater","-----------dingweichengogngla");
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getErrorCode() == 0) {

                repeatCheckUntilLocatorRefershAnimEnd(loc_refersh_anim, locator_botton);
                mListener.onLocationChanged(amapLocation); // 显示系统小蓝点

                //取出经纬度
                latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                setFirstInitLatLng(latLng.latitude, latLng.longitude);
                Marker marker = null;
                //添加Marker显示定位位置
                if (marker == null) {
                    //如果是空的添加一个新的,icon方法就是设置定位图标，可以自定义
                    MarkerOptions markerOption = new MarkerOptions();
                    markerOption.position(latLng);
                    markerOption.title("西安市").snippet("西安市：34.341568, 108.940174");

                    markerOption.draggable(true);//设置Marker可拖动
                    markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_clear_black_24dp)));
                    // 将Marker设置为贴地显示，可以双指下拉地图查看效果
                    markerOption.setFlat(true);//设置marker平贴地图效果

//                    marker = aMap.addMarker(new MarkerOptions()
//                            .position(latLng));
                            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_clear_black_24dp))); // 開啟 vectorDrawables 支持 icon設置無效
                } else {
                    //已经添加过了，修改位置即可
                    marker.setPosition(latLng);
                }

                //然后可以移动到定位点,使用animateCamera就有动画效果
//                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();

        if(null != mlocationClient){
            mlocationClient.onDestroy();
        }

        try {
            unregisterReceiver(mGeoFenceReceiver);
        } catch (Throwable e) {
        }
    }

    /**
     * 接收触发围栏后的广播,当添加围栏成功之后，会立即对所有围栏状态进行一次侦测，如果当前状态与用户设置的触发行为相符将会立即触发一次围栏广播；
     * 只有当触发围栏之后才会收到广播,对于同一触发行为只会发送一次广播不会重复发送，除非位置和围栏的关系再次发生了改变。
     */
    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 接收广播
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                Bundle bundle = intent.getExtras();
                String customId = bundle
                        .getString(GeoFence.BUNDLE_KEY_CUSTOMID);
                String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
                //status标识的是当前的围栏状态，不是围栏行为
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
                StringBuffer sb = new StringBuffer();
                switch (status) {
                    case GeoFence.STATUS_LOCFAIL :
                        sb.append("定位失败");
                        break;
                    case GeoFence.STATUS_IN :
                        sb.append("进入围栏 ");
                        break;
                    case GeoFence.STATUS_OUT :
                        sb.append("离开围栏 ");
                        break;
                    case GeoFence.STATUS_STAYED :
                        sb.append("停留在围栏内 ");
                        break;
                    default :
                        break;
                }
                if(status != GeoFence.STATUS_LOCFAIL){
                    if(!TextUtils.isEmpty(customId)){
                        sb.append(" customId: " + customId);
                    }
                    sb.append(" fenceId: " + fenceId);
                }
                String str = sb.toString();
                Message msg = Message.obtain();
                msg.obj = str;
                msg.what = 2;
                handler.sendMessage(msg);
            }
        }
    };

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2 :
                    String statusStr = (String) msg.obj;
                    Toast.makeText(MainActivity.this, statusStr, Toast.LENGTH_LONG).show();
                    break;
                default :
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        if(null != mMapView)
            mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        if(null != mMapView)
            mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        if(null != mMapView)
            mMapView.onSaveInstanceState(outState);
    }

    /** Called when the user clicks the Send button */
//    public void sendMessage(View view) {
//        // Do something in response to button
//        Intent intent = new Intent(this, DisplayMessageActivity.class);
//        EditText editText = (EditText) findViewById(R.id.edit_message);
//        String message = editText.getText().toString();
//        intent.putExtra(EXTRA_MESSAGE, message);
//        startActivity(intent);
//
//        Animation anim = null;
//        anim=new RotateAnimation(0.0f,+360.0f);
//        anim.setInterpolator(new AccelerateDecelerateInterpolator());
//        anim.setDuration(3000);
//        anim.setFillBefore(true);
//
//        TextView textView = new TextView(this);
//        textView.setTextSize(40);
//        textView.setText("dddddddddd");
//
//        findViewById(R.id.text1).startAnimation(anim);
//    }
}
