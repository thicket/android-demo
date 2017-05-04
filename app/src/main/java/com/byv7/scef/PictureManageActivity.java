package com.byv7.scef;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by thk on 17-4-29.
 */

public class PictureManageActivity extends Activity {

    List<HashMap<String , String>> mList = new ArrayList<>();
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_picture);
        init();
        top();
    }
    private boolean isBottom(AbsListView mView) {
        if (mView != null && mView.getAdapter() != null) {
            return mView.getLastVisiblePosition() == (mView.getAdapter().getCount() - 1);
        }
        return false;
    }
    private void top(){
        final SwipeRefreshLayout swipeRefreshView = (SwipeRefreshLayout) findViewById(R.id.swipe_picture);
        // 不能在onCreate中设置，这个表示当前是刷新状态，如果一进来就是刷新状态，SwipeRefreshLayout会屏蔽掉下拉事件
        //swipeRefreshLayout.setRefreshing(true);

        // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshView.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        swipeRefreshView.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

        // 下拉时触发SwipeRefreshLayout的下拉动画，动画完毕之后就会回调这个方法
        swipeRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                // 开始刷新，设置当前为刷新状态
                //swipeRefreshView.setRefreshing(true);

                // 这里是主线程
                // 一些比较耗时的操作，比如联网获取数据，需要放到子线程去执行
                // TODO 获取数据
                final Random random = new Random();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<String , String> hashMap = new HashMap<>();
                        hashMap.put("name" , "我是天才" + random.nextInt(100) + "号");
                        mList.add(hashMap);
                        lv.post(new Runnable() {
                            @Override
                            public void run() {
                                final ListAdapter listAdapter = new SimpleAdapter(
                                        PictureManageActivity.this,
                                        mList,
                                        android.R.layout.simple_list_item_1 ,
                                        new String[]{"name"} ,
                                        new int[]{android.R.id.text1});
                                lv.setAdapter(listAdapter);
                            }
                        });

                        Toast.makeText(PictureManageActivity.this, "刷新了一条数据", Toast.LENGTH_SHORT).show();

                        // 加载完数据设置为不刷新状态，将下拉进度收起来
                        swipeRefreshView.setRefreshing(false);
                    }
                }, 1200);

                // System.out.println(Thread.currentThread().getName());

                // 这个不能写在外边，不然会直接收起来
                //swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    private void init() {
        final EditText keywordText = (EditText) this.findViewById(R.id.picture_keyword);
        Button button = (Button) this.findViewById(R.id.picture_button);
        final TextView result = (TextView) this.findViewById(R.id.picture_result);
        lv = (ListView) this.findViewById(R.id.file_list);

        lv.setOnScrollListener(new AbsListView.OnScrollListener(){

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if(isBottom(absListView)){

                }
                Log.i("lv.onScrollStateChanged", String.valueOf(isBottom(absListView)));
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
//                Log.i("lv.onScroll", String.valueOf(isBottom(absListView)));
            }
        });
        getFiles("/", "");
//        result.setText(getFiles("/", ""));
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                String keyword = keywordText.getText().toString();
                if (keyword.equals("")) {
                    result.setText("请勿输入空白的关键词!!");
                } else {
                    result.setText(getFiles("/", keyword));
                }
            }
        });
    }

    private String getFiles(String path, String keyword) {
        String result = "";
        //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        File[] files = new File(path).listFiles();
        Map<String, List<String>> fileList = classify(files, keyword);

        for(Object o : fileList.get("dir")) {
            result += (o.toString()+"\n");
            HashMap<String , String> hashMap = new HashMap<>();
            hashMap.put("name" , o.toString());
            mList.add(hashMap);
        }
        for(Object o : fileList.get("file")) {
            result += (o.toString()+"\n");
        }
        for(Object o : fileList.get("hide")) {
            result += (o.toString()+"\n");
        }

//        final ListAdapter adapter = new ArrayAdapter<>(this , android.R.layout.simple_list_item_1 ,fileList.get("dir"));

        final ListAdapter listAdapter = new SimpleAdapter(
                PictureManageActivity.this,
                mList,
                android.R.layout.simple_list_item_1 ,
                new String[]{"name"} ,
                new int[]{android.R.id.text1});

        lv.post(new Runnable() {
            @Override
            public void run() {
                lv.setAdapter(listAdapter);
            }
        });

        return result;
    }

    private Map<String, List<String>> classify(File[] files, String keyword) {
        Map<String, List<String>> fileClass = new HashMap<String, List<String>>();
        List<String> dirList = new ArrayList();
        List<String> fileList = new ArrayList();
        List<String> hideList = new ArrayList();
        for (File file : files) {
            if (file.isDirectory()) {
//                Log.i("info---dir-", search(file, keyword));
                dirList.add(search(file, keyword));
            } else if (file.isFile()) {
//                Log.i("info---file-", search(file, keyword));
                fileList.add(search(file, keyword));
            } else if (file.isHidden()) {
//                Log.i("info---hide-", search(file, keyword));
                hideList.add(search(file, keyword));
            }
        }
        Collections.sort(dirList);
        Collections.sort(fileList);
        Collections.sort(hideList);
        fileClass.put("dir", dirList);
        fileClass.put("file", fileList);
        fileClass.put("hide", hideList);
        return fileClass;
    }

    private String search(File file, String keyword) {
        if (keyword.equals("") || file.getName().indexOf(keyword) >= 0) {
            return file.getName().toString();
        }
        return "";
    }
}
