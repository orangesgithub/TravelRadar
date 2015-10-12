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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.travel.adapter.AdviceListViewAdapter;
import com.smart.travel.model.RadarItem;
import com.smart.travel.net.AdviceLoader;
import com.smart.travel.utils.FileUtils;
import com.yalantis.taurus.PullToRefreshView;

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
    private boolean footerViewLoadingVisible = false;

    // 第一次进入页面的时候，会首先显示本地数据，当网络数据下载完毕的时候，会删除ListView中的本地数据并显示最新的数据
    private boolean firstDoRefresh = true;

    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new UiHandler(this);
        listViewAdapter = new AdviceListViewAdapter(getActivity());
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
                if (listViewAdapter.getCount() == 0) {
                    return;
                }
                Intent intent = new Intent(getActivity(), WebActivity.class);
                RadarItem radarItem = (RadarItem) listViewAdapter.getItem(position);
                intent.putExtra("url", radarItem.getUrl());
                intent.putExtra("title", radarItem.getAuthor());
                startActivity(intent);
            }
        });

        createFooterView();
        adviceListView.addFooterView(footerViewLoading);
        footerViewLoading.setVisibility(View.GONE);

        return content;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adviceListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItem = firstVisibleItem + visibleItemCount;
                if (!footerViewLoadingVisible && totalItemCount > visibleItemCount) {
                    footerViewLoading.setVisibility(View.VISIBLE);
                    adviceListView.setFooterDividersEnabled(false);
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

    public void onDestroy() {
        // remove all messages, in case fragment or activity is destroyed
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private static class UiHandler extends WeakReferenceHandler<AdviceFragment> {

        public UiHandler(AdviceFragment fragment) {
            super(fragment);
        }

        @Override
        protected void handleMessage(AdviceFragment fragment, Message msg) {
            if (fragment != null) {
                switch (msg.what) {
                    case MESSAGE_REFRESH:
                        fragment.handleRefreshMsg();
                        break;
                    case MESSAGE_CLEAR_AND_REFRESH:
                        fragment.handleCleanRefreshMsg();
                        break;
                    case MESSAGE_LOAD_MORE:
                        fragment.handleLoadMoreMsg();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void handleRefreshMsg() {
        Log.d(TAG, "message do refresh, updateItems size: " + updateItems.size());
        listViewAdapter.addData(updateItems);
        listViewAdapter.notifyDataSetChanged();
        pullToRefreshView.setRefreshing(false);

        if (firstDoRefresh) {
            pullToRefreshView.setRefreshing(true);
            doRefresh();
        }
    }

    private void handleCleanRefreshMsg() {
        Log.d(TAG, "message do refresh, updateItems size: " + updateItems.size());
        if (updateItems.size() > 0) {
            listViewAdapter.getAllData().clear();
        }
        listViewAdapter.addData(updateItems);
        listViewAdapter.notifyDataSetChanged();
        pullToRefreshView.setRefreshing(false);
        firstDoRefresh = false;
        currPage = 1;
    }

    private void handleLoadMoreMsg() {
        footerViewLoading.setVisibility(View.GONE);
//        adviceListView.setFooterDividersEnabled(false);
        footerViewLoadingVisible = false;
        isLoadingData = false;
        listViewAdapter.addData(updateItems);
        listViewAdapter.notifyDataSetChanged();
    }

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
                try {
                    updateItems = AdviceLoader.load(getActivity(), 1);


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
                    if (listViewAdapter.getCount() == 0 && updateItems.size() == 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                pullToRefreshView.setRefreshing(false);
                                footerViewLoading.findViewById(R.id.listview_footer_progressbar).setVisibility(View.GONE);
                                footerViewLoading.findViewById(R.id.listview_footer_loading_tip).setVisibility(View.GONE);
                                footerViewLoading.findViewById(R.id.listview_footer_reload).setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        handler.sendEmptyMessage(MESSAGE_CLEAR_AND_REFRESH);
                    }
                }
            }
        }.start();
    }

    private void createFooterView() {
        footerViewLoading = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.listview_loading_footer, null);
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
