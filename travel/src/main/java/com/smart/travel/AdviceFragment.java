package com.smart.travel;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import com.smart.travel.net.AdviceLoader;
import com.smart.travel.net.TicketLoader;
import com.smart.travel.utils.FileUtils;
import com.yalantis.phoenix.PullToRefreshView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdviceFragment extends Fragment {
    private static final String TAG = "AdviceFragment";

    public static final String ADVICE_LISTVIEW_HISTORY_FILE = "advice_listview_history.dat";

    private static final int MESSAGE_LOAD_MORE = 1;
    private static final int MESSAGE_REFRESH = 2;
    private static final int MESSAGE_CLEAR_AND_REFRESH = 3;

    private PullToRefreshView pullToRefreshView;
    private ListView adviceListView;
    private AdviceListViewAdapter listViewAdapter;

    private int currPage = 0;

    private LinearLayout footerViewLoading;
    private List<RadarItem> updateItems;
    private int lastItem;
    private boolean isLoadingData = false;
    private boolean footerViewLoadingVisiable = false;

    // 第一次进入页面的时候，会首先显示本地数据，当网络数据下载完毕的时候，会删除ListView中的本地数据并显示最新的数据
    private boolean firstDoRefresh = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        listViewAdapter = new AdviceListViewAdapter(getActivity());
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.advice_fragment, container, false);
        pullToRefreshView = (PullToRefreshView) content.findViewById(R.id.pull_to_refresh);
        pullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        adviceListView = (ListView) pullToRefreshView.findViewById(R.id.finder_list_view);
        adviceListView.setAdapter(listViewAdapter);

        adviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), WebActivity.class);
                RadarItem radarItem = (RadarItem) listViewAdapter.getItem(position);
                intent.putExtra("url", radarItem.getUrl());
                intent.putExtra("title", radarItem.getAuthor());
                startActivity(intent);
            }
        });

        createFooterView();

        return content;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adviceListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItem = firstVisibleItem + visibleItemCount;
                if (!footerViewLoadingVisiable && totalItemCount > visibleItemCount) {
                    adviceListView.addFooterView(footerViewLoading);
                    adviceListView.setFooterDividersEnabled(false);
                    footerViewLoadingVisiable = true;
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (lastItem >= listViewAdapter.getCount() && footerViewLoadingVisiable
                        && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && !isLoadingData) {
                    isLoadingData = true;
                    Log.d(TAG, "start to pull new list");
                    listViewAdapter.notifyDataSetChanged();
                    loadMore();
                }
            }
        });

        try {
            if (FileUtils.fileExists(getActivity(), ADVICE_LISTVIEW_HISTORY_FILE)) {
                updateItems = AdviceLoader.parse(FileUtils.readFile(getActivity(), ADVICE_LISTVIEW_HISTORY_FILE));
            } else {
                updateItems = new ArrayList<>();
            }
            handler.sendEmptyMessage(MESSAGE_REFRESH);
        } catch (Exception e) {
            Log.e(TAG, "Radar fragment load local json file failed.", e);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH:
                    Log.d(TAG, "message do refresh, updateItems size: " + updateItems.size());
                    listViewAdapter.addData(updateItems);
                    listViewAdapter.notifyDataSetChanged();
                    pullToRefreshView.setRefreshing(false);

                    if (firstDoRefresh) {
                        pullToRefreshView.setRefreshing(true);
                        doRefresh();
                    }
                    break;
                case MESSAGE_CLEAR_AND_REFRESH:
                    Log.d(TAG, "message do refresh, updateItems size: " + updateItems.size());
                    if (updateItems.size() > 0) {
                        listViewAdapter.getAllData().clear();
                    }
                    listViewAdapter.addData(updateItems);
                    listViewAdapter.notifyDataSetChanged();
                    pullToRefreshView.setRefreshing(false);
                    firstDoRefresh = false;
                    currPage = 1;
                    break;
                case MESSAGE_LOAD_MORE:
                    adviceListView.removeFooterView(footerViewLoading);
                    adviceListView.setFooterDividersEnabled(true);
                    footerViewLoadingVisiable = false;
                    isLoadingData = false;
                    listViewAdapter.addData(updateItems);
                    listViewAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }

    };

    private void loadMore() {
        new Thread() {
            @Override
            public void run() {
                updateItems = new ArrayList<>(24);
                Set<Integer> idSet = new HashSet<>(listViewAdapter.getAllData().size() * 2);
                try {
                    for (RadarItem item : listViewAdapter.getAllData()) {
                        idSet.add(item.getId());
                    }
                    while (true) {
                        List<RadarItem> items = AdviceLoader.load(getActivity(), currPage + 1);
                        for (RadarItem item : items) {
                            if (!idSet.contains(item.getId())) {
                                updateItems.add(item);
                            }
                        }
                        // 如果服务器上没有数据，或者有当前列表里没有的新数据被Load进来，则停止
                        if (items.size() == 0 || updateItems.size() > 0) {
                            break;
                        }
                    }

                    currPage++;
                } catch (Exception e) {
                    Log.e(TAG, "Radar Http Exception", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "加载数据失败", Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    handler.sendEmptyMessage(MESSAGE_LOAD_MORE);
                }
            }
        }.start();
    }


    private void doRefresh() {
        Log.d(TAG, "doRefresh");
        new Thread() {
            @Override
            public void run() {
                long tStart = System.currentTimeMillis();
                int page = 0;
                boolean loadFinished = false;
                updateItems = new ArrayList<>(16);
                Set<Integer> idSet = new HashSet<>(listViewAdapter.getAllData().size() * 2);
                try {
                    for (RadarItem item : listViewAdapter.getAllData()) {
                        idSet.add(item.getId());
                    }
                    while (!loadFinished) {
                        List<RadarItem> items = AdviceLoader.load(getActivity(), page + 1);
                        for (RadarItem item : items) {
                            if (!idSet.contains(item.getId()) || firstDoRefresh) {
                                updateItems.add(item);
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
                } catch (Exception e) {
                    Log.e(TAG, "Radar Http Exception", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "加载数据失败", Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    long tEnd = System.currentTimeMillis();
                    if (tEnd - tStart < 500) {
                        try {
                            Thread.sleep(500 + tStart - tEnd);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (firstDoRefresh) {
                        handler.sendEmptyMessage(MESSAGE_CLEAR_AND_REFRESH);
                    } else {
                        handler.sendEmptyMessage(MESSAGE_REFRESH);
                    }
                }
            }
        }.start();
    }

    private void createFooterView() {
        footerViewLoading = new LinearLayout(getActivity());
        footerViewLoading.setOrientation(LinearLayout.HORIZONTAL);
        footerViewLoading.setGravity(Gravity.CENTER);
        ProgressBar bar = new ProgressBar(getActivity());
        TextView textView = new TextView(getActivity());
        textView.setText("加载中...");
        footerViewLoading.addView(bar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        footerViewLoading.addView(textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

}
