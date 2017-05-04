package com.byv7.scef;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MusicActivity extends Activity {
    MediaPlayer mp = null;
    TextView tv = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);


        tv = (TextView) findViewById(R.id.nav_title);

        File[] files = new File("/storage/sdcard1/netease/cloudmusic/Music/").listFiles();

//        for (File file : files) {
//            result += file.getPath() + "\n";
//        }
        int index = ((int)SystemClock.elapsedRealtime())%files.length;
        String song = files[index].toString().split("/storage/sdcard1/netease/cloudmusic/Music/")[1];
        //Log.i("music----file-list", files.length + "--" + index + "--" + files[index]);
        //tv.setText(files[index].toString().split("/storage/sdcard1/netease/cloudmusic/Music/")[1]);
        mp = new MediaPlayer();
        try {
            mp.setDataSource(files[index].toString());
            mp.prepare();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //判断当前播放模式是否为单曲循环
                    mp.stop();
                    mp.release();
                    //将线程销毁掉
                    handler.removeCallbacks(updateThread);
                    finish();
                }
            });
            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        AppCompatImageView close = (AppCompatImageView)findViewById(R.id.nav_left_prev);
        close.setOnClickListener(new View.OnClickListener(){
            public void onClick(View source){
                //获取启动前Activity的上一个Intent
//                Intent intent = new Intent(DisplayMessageActivity.this,MainActivity.class);
                //启动intent对应的Activity
//                startActivity(intent);
                mp.stop();
                //将线程销毁掉
                handler.removeCallbacks(updateThread);
                finish();
            }
        });

        new Thread(updateThread).start();

        String message = song;//intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        TextView textView = new TextView(MusicActivity.this);
        textView.setTextSize(40);
        textView.setText(message);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_music);
        layout.addView(textView);
    }

    Handler handler = new Handler();
    Runnable updateThread = new Runnable() {
        public void run() {
            // 获得歌曲现在播放位置并设置成播放进度条的值
            if (mp != null) {
                tv.setText(String.valueOf(mp.getCurrentPosition()));
                // 每次延迟100毫秒再启动线程
                handler.postDelayed(updateThread, 100);
            }
        }
    };

    private long exitTime;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

            if((System.currentTimeMillis() - exitTime) > 2000) { //System.currentTimeMillis()无论何时调用，肯定大于2000
                Toast.makeText(getApplicationContext(), "再按一次退出程序",Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
