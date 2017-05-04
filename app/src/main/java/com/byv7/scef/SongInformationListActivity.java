package com.byv7.scef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import static com.amap.api.mapcore.util.cs.i;

public class SongInformationListActivity extends Activity {
    // All static variables
    static final String URL = "http://api.androidhive.info/music/music.xml";
    // XML node keys
    static final String KEY_SONG = "song"; // parent node
    static final String KEY_ID = "id";
    static final String KEY_TITLE = "title";
    static final String KEY_ARTIST = "artist";
    static final String KEY_DURATION = "duration";
    static final String KEY_THUMB_URL = "thumb_url";

    boolean isLoadRefreshing = false;
    ListView lv;
    LazyAdapter adapter;
    ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    SwipeRefreshLayout swipeRefreshView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_song);
        swipeRefreshView = (SwipeRefreshLayout) findViewById(R.id.swipe_song);
        new Thread(runnable).start();
        top();
    }

    private boolean isBottom(AbsListView mView) {
        if (mView != null && mView.getAdapter() != null) {
            return mView.getLastVisiblePosition() == (mView.getAdapter().getCount() - 1);
        }
        return false;
    }

    private void top() {

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
                if (false == isLoadRefreshing) {
                    isLoadRefreshing = true;
                    // 开始刷新，设置当前为刷新状态
                    //swipeRefreshLayout.setRefreshing(true);

                    // 这里是主线程
                    // 一些比较耗时的操作，比如联网获取数据，需要放到子线程去执行
                    // TODO 获取数据
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(KEY_ID, "999");
                            map.put(KEY_TITLE, "title");
                            map.put(KEY_ARTIST, "artist");
                            map.put(KEY_DURATION, "duration");
                            map.put(KEY_THUMB_URL, "http://api.androidhive.info/music/images/adele.png");
                            songsList.add(map);
                            adapter.notifyDataSetChanged();

                            Toast.makeText(SongInformationListActivity.this, "刷新了一条数据", Toast.LENGTH_SHORT).show();

                            // 加载完数据设置为不刷新状态，将下拉进度收起来
                            swipeRefreshView.setRefreshing(false);
                            isLoadRefreshing = false;
                        }
                    }, 1200);
                }
                // System.out.println(Thread.currentThread().getName());

                // 这个不能写在外边，不然会直接收起来
                //swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            XMLParser parser = new XMLParser();
            String xml = parser.getXmlFromUrl(URL); // getting XML from URL
            Document doc = parser.getDomElement(xml); // getting DOM element
            Log.i("song", String.valueOf(xml.length()));

            NodeList nl = doc.getElementsByTagName(KEY_SONG);
            // looping through all song nodes <song>
            for (int i = 0; i < nl.getLength(); i++) {
                Log.i("song++", "" + i);
                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();
                Element e = (Element) nl.item(i);
                // adding each child node to HashMap key => value
                map.put(KEY_ID, parser.getValue(e, KEY_ID));
                map.put(KEY_TITLE, parser.getValue(e, KEY_TITLE));
                map.put(KEY_ARTIST, parser.getValue(e, KEY_ARTIST));
                map.put(KEY_DURATION, parser.getValue(e, KEY_DURATION));
                map.put(KEY_THUMB_URL, parser.getValue(e, KEY_THUMB_URL));

                // adding HashList to ArrayList
                songsList.add(map);
            }

            lv = (ListView) findViewById(R.id.song_list);
            Log.i("view", "ok!!!");

            // Getting adapter by passing xml data ArrayList
            adapter = new LazyAdapter(SongInformationListActivity.this, songsList);
            lv.post(new Runnable() {
                @Override
                public void run() {
                    lv.setAdapter(adapter);
                }
            });

            // Click event for single list row
            lv.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });

            lv.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(final AbsListView absListView, int i) {
                    Log.i("lv.onScrollStateChanged", String.valueOf(isBottom(absListView)));
                    if (isLoadRefreshing == false && isBottom(absListView)) {
                        isLoadRefreshing = true;
                        swipeRefreshView.setRefreshing(true);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(KEY_ID, "999");
                                map.put(KEY_TITLE, "title");
                                map.put(KEY_ARTIST, "artist");
                                map.put(KEY_DURATION, "duration");
                                map.put(KEY_THUMB_URL, "http://api.androidhive.info/music/images/adele.png");
                                songsList.add(map);
                                adapter.notifyDataSetChanged();

                                Toast.makeText(SongInformationListActivity.this, "刷新了一条数据", Toast.LENGTH_SHORT).show();
                                // 加载完数据设置为不刷新状态，将下拉进度收起来
                                swipeRefreshView.setRefreshing(false);
                                isLoadRefreshing = false;
                            }
                        }, 1200);
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {
//                Log.i("lv.onScroll", String.valueOf(isBottom(absListView)));
                }
            });

        }
    };
}