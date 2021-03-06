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
import android.widget.Button;
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

    private boolean load2LastItem = false; //ListView中的需要显示的数据是否已经加载到末尾

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new UiHandler(this);
        setContentView(R.layout.activity_search_result);

        String title = getIntent().getStringExtra("title");
        keyword = getIntent().getStringExtra("keyword");

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
        listView.setFooterDividersEnabled(false);
        footerViewLoading.setVisibility(View.GONE);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItem = firstVisibleItem + visibleItemCount;
                if (!footerViewLoadingVisible && totalItemCount > visibleItemCount) {
                    if (!load2LastItem) {
                        footerViewLoading.setVisibility(View.VISIBLE);
                        listView.setFooterDividersEnabled(false);
                    }
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
        currPage = 1;
    }

    private void handleLoadMore() {
        footerViewLoading.setVisibility(View.GONE);
//        listView.setFooterDividersEnabled(true);
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
                    Set<Integer> idSet = new HashSet<>(listViewAdapter.getCount() * 2);
                    for (RadarItem item : listViewAdapter.getAllData()) {
                        idSet.add(item.getId());
                    }
                    // 后台服务器搜索页超出总数据条数也会有数据过来，这个做一个判断以回避此bug
                    for (int i = listItems.size() - 1; i > -1; i--) {
                        if (idSet.contains(listItems.get(i).getId())) {
                            listItems.remove(i);
                            load2LastItem = true; //加载到重复数据，已经加载到
                        }
                    }
                    listViewAdapter.addData(listItems);
                    handler.sendEmptyMessage(MESSAGE_LOAD_MORE);
                    if (listItems.size() > 0) {
                        currPage++;
                    }
                    // ugly code, if possible, should optimize
                    if (listItems.size() == 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SearchResultActivity.this, "没有更多数据", Toast.LENGTH_SHORT).show();
                                footerViewLoading.setPadding(0, -1 * footerViewLoading.getHeight(), 0, 0);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Radar Http Exception", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listViewAdapter.getCount() == 0) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        pullToRefreshView.setRefreshing(false);
                                        footerViewLoading.findViewById(R.id.listview_footer_progressbar).setVisibility(View.GONE);
                                        footerViewLoading.findViewById(R.id.listview_footer_loading_tip).setVisibility(View.GONE);
                                        footerViewLoading.findViewById(R.id.listview_footer_reload).setVisibility(View.VISIBLE);
                                    }
                                });
                            }
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
                try {
                    List<RadarItem> updateItems = SearchLoader.load(1, keyword);
                    Log.d(TAG, "load finished");

                    listViewAdapter.getAllData().clear();
                    listViewAdapter.addData(updateItems);
                    handler.sendEmptyMessage(MESSAGE_REFRESH);
                } catch (Exception e) {
                    Log.e(TAG, "Radar Http Exception", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listViewAdapter.getCount() == 0) {
                                pullToRefreshView.setRefreshing(false);
                                footerViewLoading.findViewById(R.id.listview_footer_progressbar).setVisibility(View.GONE);
                                footerViewLoading.findViewById(R.id.listview_footer_loading_tip).setVisibility(View.GONE);
                                footerViewLoading.findViewById(R.id.listview_footer_reload).setVisibility(View.VISIBLE);
                            }
                            Toast.makeText(SearchResultActivity.this, "加载数据失败", Toast.LENGTH_LONG).show();
                            handler.sendEmptyMessage(MESSAGE_REFRESH);
                        }
                    });
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
        footerViewLoading = (LinearLayout) getLayoutInflater().inflate(R.layout.listview_loading_footer, null);
        Button btnReload = (Button) footerViewLoading.findViewById(R.id.listview_footer_reload);
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                footerViewLoading.findViewById(R.id.listview_footer_progressbar).setVisibility(View.VISIBLE);
                footerViewLoading.findViewById(R.id.listview_footer_loading_tip).setVisibility(View.VISIBLE);
                footerViewLoading.findViewById(R.id.listview_footer_reload).setVisibility(View.GONE);
                pullToRefreshView.setRefreshing(true);
                doRefresh();
            }
        });
    }

}
