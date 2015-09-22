package com.smart.travel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.travel.adapter.AdviceListViewAdapter;
import com.smart.travel.model.RadarItem;
import com.smart.travel.net.SearchLoader;
import com.yalantis.taurus.PullToRefreshView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchResultActivity extends AppCompatActivity {

    private static final String TAG = "SearchResultActivity";

    private static final int MESSAGE_LOAD_MORE = 1;
    private static final int MESSAGE_REFRESH = 2;

    private PullToRefreshView pullToRefreshView;
    private ListView listView;
    private AdviceListViewAdapter listViewAdapter;

    private String keyword;

    private int currPage = 0;

    private LinearLayout footerViewLoading;
    private int lastItem;
    private boolean isLoadingData = false;
    private boolean footerViewLoadingVisible = false;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new UiHandler(this);
        setContentView(R.layout.activity_search_result);

        String title = getIntent().getStringExtra("title");
        if (getIntent().hasExtra("keyword")) {
            keyword = getIntent().getStringExtra("keyword");
        } else {
            if ("全部".equals(title)) {
                keyword = "全部";
            } else if ("长三角".equals(title)) {
                keyword = "华东|上海|杭州|南京|宁波|江浙";
            } else if ("珠三角".equals(title)) {
                keyword = "广州|深圳|香港|珠三角";
            } else if ("京津冀".equals(title)) {
                keyword = "北京|天津";
            } else if ("华中".equals(title)) {
                keyword = "武汉|长沙";
            } else if ("西南".equals(title)) {
                keyword = "成都|重庆|昆明|四川";
            }
        }


        Log.d(TAG, "SearchResultActivity keyword: " + keyword);

        TextView textTitle = (TextView) findViewById(R.id.title_text);
        textTitle.setText(title);

        listViewAdapter = new AdviceListViewAdapter(this);

        pullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh);
        pullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        listView = (ListView) pullToRefreshView.findViewById(R.id.search_result_list_view);
        listView.setAdapter(listViewAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listViewAdapter.getCount() == 0) {
                    return;
                }
                Intent intent = new Intent(SearchResultActivity.this, WebActivity.class);
                RadarItem radarItem = (RadarItem) listViewAdapter.getItem(position);
                intent.putExtra("url", radarItem.getUrl());
                intent.putExtra("title", radarItem.getAuthor());
                startActivity(intent);
            }
        });


        createFooterView();
        listView.addFooterView(footerViewLoading);
        footerViewLoading.setVisibility(View.GONE);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItem = firstVisibleItem + visibleItemCount;
                Log.d(TAG, "onScroll callback: " + firstVisibleItem + ", " + visibleItemCount + ", " + lastItem);
                if (!footerViewLoadingVisible && totalItemCount > visibleItemCount) {
                    footerViewLoading.setVisibility(View.VISIBLE);
                    listView.setFooterDividersEnabled(false);
                    footerViewLoadingVisible = true;
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (lastItem >= listViewAdapter.getCount() && footerViewLoadingVisible
                        && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && !isLoadingData) {
                    isLoadingData = true;
                    Log.d(TAG, "start to pull new list");
                    listViewAdapter.notifyDataSetChanged();
                    loadMore();
                }
            }
        });

        loadMore();
    }

    public void onDestroy() {
        // remove all messages, in case fragment or activity is destroyed
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private static class UiHandler extends WeakReferenceHandler<SearchResultActivity> {
        public UiHandler(SearchResultActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(SearchResultActivity activity, Message msg) {
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_REFRESH:
                        activity.handleRefresh();
                        break;
                    case MESSAGE_LOAD_MORE:
                        activity.handleLoadMore();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void handleRefresh() {
        listViewAdapter.notifyDataSetChanged();
        pullToRefreshView.setRefreshing(false);
    }

    private void handleLoadMore() {
        footerViewLoading.setVisibility(View.GONE);
        listView.setFooterDividersEnabled(true);
        footerViewLoadingVisible = false;
        isLoadingData = false;
        listViewAdapter.notifyDataSetChanged();
    }

    private void loadMore() {
        new Thread() {
            @Override
            public void run() {
                try {
                    List<RadarItem> listItems = SearchLoader.load(currPage + 1, keyword);
                    listViewAdapter.addData(listItems);
                    handler.sendEmptyMessage(MESSAGE_LOAD_MORE);
                    currPage++;
                } catch (Exception e) {
                    Log.e(TAG, "Radar Http Exception", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SearchResultActivity.this, "加载数据失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }.start();
    }

    private void doRefresh() {
        Log.d(TAG, "doRefresh");
        new Thread() {
            @Override
            public void run() {
                int page = 0;
                boolean loadFinished = false;
                List<RadarItem> newItems = new ArrayList<>(16);
                Set<Integer> idSet = new HashSet<>(listViewAdapter.getAllData().size() * 2);
                try {
                    for (RadarItem item : listViewAdapter.getAllData()) {
                        idSet.add(item.getId());
                    }
                    while (!loadFinished) {
                        List<RadarItem> items = SearchLoader.load(page + 1, keyword);
                        for (RadarItem item : items) {
                            if (!idSet.contains(item.getId())) {
                                newItems.add(item);
                            } else {
                                loadFinished = true;
                            }
                        }
                        page++;
                        Log.d(TAG, "loading page: " + page);

                        if (currPage == 0) {
                            loadFinished = true;
                        }
                    }

                    Log.d(TAG, "load finished");

                    listViewAdapter.addDataBegin(newItems);
                    handler.sendEmptyMessage(MESSAGE_REFRESH);
                } catch (Exception e) {
                    Log.e(TAG, "Radar Http Exception", e);
                    handler.sendEmptyMessage(MESSAGE_REFRESH);
                }
            }
        }.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createFooterView() {
        footerViewLoading = new LinearLayout(this);
        footerViewLoading.setOrientation(LinearLayout.HORIZONTAL);
        footerViewLoading.setGravity(Gravity.CENTER);
        ProgressBar bar = new ProgressBar(this);
        TextView textView = new TextView(this);
        textView.setText("加载中...");
        footerViewLoading.addView(bar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        footerViewLoading.addView(textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

}
