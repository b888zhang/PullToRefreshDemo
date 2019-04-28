package com.example.zb.pulltorefreshdemo;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshAdapterViewBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<String> list = new ArrayList<String>();
    private MyAdapter adapter;
    private PullToRefreshListView refreshListView;
    public Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();

        refreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        refreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ListView> refreshView) {
                if (refreshView.getCurrentMode() == PullToRefreshBase.Mode.PULL_FROM_START) {// 下拉刷新
                    //方法一：正确：
                    new Handler().post(new Runnable() {

                        @Override
                        public void run() {
                            refreshView.onRefreshComplete();// 刷新完成
                        }
                    });

                    /**
                     * pulltorefresh的监听中调用activity的runOnUiThread(..)是无效的，图标不会缩回，
                     * 但是先开子线程，在子线程中调用runOnUiThread(..)是可以的，即下面情况。
                     */
                    //错误：图标不会缩回，headView依然存在。
//					((Activity) context).runOnUiThread(new Runnable() {
//
//						@Override
//						public void run() {
//							refreshView.onRefreshComplete();// 刷新完成
//						}
//					});

                    //方法二：正确
//					new  Thread(new Runnable() {
//						@Override
//						public void run() {
//							runOnUiThread(new Runnable() {
//								public void run() {
//									refreshView.onRefreshComplete();// 刷新完成
//								}
//							});
//						}
//					}).start();

                } else {// 上拉加载数据
                    loadData();
                }
            }
        });

        ListView listView = refreshListView.getRefreshableView();

        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.ic_launcher_background);
        listView.addHeaderView(iv);

        adapter = new MyAdapter();
        listView.setAdapter(adapter);
    }

    private void initData() {
        for (int i = 0; i < 10; i++) {
            list.add("item---" + i);
        }
    }

    // 下拉加载数据
    protected void loadData() {
        new Thread() {
            public void run() {
                SystemClock.sleep(1000);
                for (int i = 0; i < 10; i++) {
                    list.add("item---" + list.size());
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        adapter.notifyDataSetChanged();
                        refreshListView.onRefreshComplete();
                    }
                });
            };
        }.start();

    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = new TextView(MainActivity.this);
            tv.setText(list.get(position));
            tv.setHeight((int) (MainActivity.this).getResources().getDimension(
                    R.dimen.main_activity_item_height));
            return tv;
        }

    }
}
