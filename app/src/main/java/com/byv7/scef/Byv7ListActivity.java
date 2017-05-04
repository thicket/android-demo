package com.byv7.scef;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

class DrawView extends View {
    public DrawView(Context context) {
        super(context);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*
         * 方法 说明 drawRect 绘制矩形 drawCircle 绘制圆形 drawOval 绘制椭圆 drawPath 绘制任意多边形
         * drawLine 绘制直线 drawPoin 绘制点
         */
        // 创建画笔
        Paint p = new Paint();
        p.setColor(Color.RED);// 设置红色

        canvas.drawText("画圆：", 10, 20, p);// 画文本
        canvas.drawCircle(60, 20, 10, p);// 小圆
        p.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
        canvas.drawCircle(120, 20, 20, p);// 大圆
    }
}
public class Byv7ListActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{

    ListView layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_list);

        new Thread(runnable).start();

//        final DrawView view = new DrawView(this);
//        //通知view组件重绘
//        view.invalidate();
//        layout.addView(view);

        String message = "theme list"; //intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);

        layout = (ListView) findViewById(R.id.activity_theme_list);
//        layout.addView(textView);

        AppCompatImageView close = (AppCompatImageView)findViewById(R.id.nav_left_prev);
        close.setOnClickListener(new View.OnClickListener(){
            public void onClick(View source){
                //获取启动前Activity的上一个Intent
//                Intent intent = new Intent(DisplayMessageActivity.this,MainActivity.class);
                //启动intent对应的Activity
//                startActivity(intent);
                finish();
            }
        });
    }

    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            //
            // TODO: http request.
            //
            String msg = "ok";
            int duration = Snackbar.LENGTH_SHORT;
            try {
                URL url = new URL("http://byv7.com/pokemongo/news");
//                URL url = new URL("https://rili.jin10.com/datas/2010/1001/economics.json");
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(2500);
                connection.setReadTimeout(2500);
                connection.setRequestProperty("THK-AJAX", "android");
                connection.setInstanceFollowRedirects(true);

                if( connection.getResponseCode() == HttpsURLConnection.HTTP_OK ) {
                    InputStream in = connection.getInputStream();
                    //下面对获取到的输入流进行读取
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    in.close();


                    // 取得網頁內容類型
                    String  mime = connection.getContentType();
//                    boolean isMediaStream = false;
                    // 判斷是否為串流檔案
                    if( mime.indexOf("audio") == 0 ||  mime.indexOf("video") == 0 ){
//                        isMediaStream = true;
                        System.exit(0);
                    }
                    Log.i("builder--", String.valueOf(response.toString().length()));
                    connection.disconnect();


                    JSONObject json = new JSONObject(response.toString());
                    final JSONObject items = json.getJSONObject("item");
                    final TextView nt = (TextView) findViewById(R.id.nav_title);
                    nt.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                nt.setText(items.getString("name"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    JSONArray lists = json.getJSONArray("list");

                    List<HashMap<String , String>> list = new ArrayList<>();
                    for (int i = 0; i < lists.length(); i++) {
                        JSONObject item = lists.getJSONObject(i);

                        HashMap<String , String> hashMap = new HashMap<>();
                        //把title , text存入HashMap之中
                        hashMap.put("title" , item.getString("fcCategory"));
                        hashMap.put("text" , item.getString("fcTitle"));
                        //把HashMap存入list之中
                        list.add(hashMap);

                        final ListAdapter listAdapter = new SimpleAdapter(
                                Byv7ListActivity.this,
                                list,
                                android.R.layout.simple_list_item_2 ,
                                new String[]{"title" , "text"} ,
                                new int[]{android.R.id.text1 , android.R.id.text2});

                        layout.post(new Runnable() {
                            public void run() {
                                layout.setAdapter(listAdapter);
                            }
                        });

//                        Log.i("json-data", "" + item.getString("id") + "" + item.getString("fcAppID") + "" + item.getString("fcCategory") + "" + item.getString("fcTitle") + "" + item.getString("fcReproducedLink"));
                    }
                }
            } catch (SocketTimeoutException e) {//e.printStackTrace();
                msg = "网络不顺畅 CODE:01";
                duration = Snackbar.LENGTH_INDEFINITE;
            } catch (JSONException e) {
                msg = "网络不顺畅 CODE:02";
                duration = Snackbar.LENGTH_INDEFINITE;
            } catch (ProtocolException e) {
                msg = "网络不顺畅 CODE:03";
                duration = Snackbar.LENGTH_INDEFINITE;
            } catch (MalformedURLException e) {
                msg = "网络不顺畅 CODE:04";
                duration = Snackbar.LENGTH_INDEFINITE;
            } catch (IOException e) {
                msg = "网络不顺畅 CODE:05";
                duration = Snackbar.LENGTH_INDEFINITE;
            } finally {
                final int finalDuration = duration;
                if(finalDuration == Snackbar.LENGTH_INDEFINITE) {
                    Snackbar.make(Byv7ListActivity.this.getWindow().getDecorView(), msg, finalDuration).setAction("刷新", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Thread(runnable).start();
                            Toast.makeText(Byv7ListActivity.this, "正在刷新", Toast.LENGTH_LONG).show();
                        }
                    }).setActionTextColor(Color.YELLOW).show();
                }
            }
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
