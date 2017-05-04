package com.byv7.scef;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

//        Intent intent = getIntent();
        String message = "dm";//intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_display_parent_message);
        layout.addView(textView);


        AppCompatImageView close = (AppCompatImageView) findViewById(R.id.nav_left_prev);
        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View source) {
                //获取启动前Activity的上一个Intent
//                Intent intent = new Intent(DisplayMessageActivity.this,MainActivity.class);
                //启动intent对应的Activity
//                startActivity(intent);
                finish();
            }
        });
    }
}
