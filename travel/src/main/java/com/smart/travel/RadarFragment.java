package com.smart.travel;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.travel.adapter.TravelListViewAdapter;
import com.smart.travel.model.RadarItem;
import com.smart.travel.net.SearchLoader;
import com.smart.travel.net.TicketLoader;
import com.smart.travel.utils.FileUtils;
import com.smart.travel.utils.NetworkUtils;
import com.yalantis.taurus.PullToRefreshView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.R.layout.simple_list_item_1;


public class RadarFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "RadarFragment";

    public static final String RADAR_LISTVIEW_HISTORY_FILE = "radar_listview_history.dat";

    private static final int MESSAGE_LOAD_MORE = 1;
    private static final int MESSAGE_REFRESH = 2;
    private static final int MESSAGE_CLEAR_AND_REFRESH = 3;

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private String[] drawerListItems;

    private TravelListViewAdapter listViewAdapter;

    private PullToRefreshView pullToRefreshView;
    private ListView ticketListView;

    private int currPage = 0;

    private LinearLayout footerViewLoading;
    private List<RadarItem> updateItems;
    private int lastItem;
    private boolean isLoadingData = false;
    private boolean footerViewLoadingVisible = false;

    private RelativeLayout networkErrBar;

    // 第一次进入页面的时候，会首先显示本地数据，当网络数据下载完毕的时候，会删除ListView中的本地数据并显示最新的数据
    private boolean firstDoRefresh = true;
    private String keyword;

    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new UiHandler(this);
        listViewAdapter = new TravelListViewAdapter(getActivity());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        drawerListItems = getResources().getStringArray(R.array.city_list);

        // Inflate the layout for this fragment
        View content = inflater.inflate(R.layout.radar_fragment, container, false);

        networkErrBar = (RelativeLayout) content.findViewById(R.id.network_err_bar);

        drawerLayout = (DrawerLayout) content.findViewById(R.id.drawer_layout);
        drawerList = (ListView) content.findViewById(R.id.right_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(getActivity(), simple_list_item_1, drawerListItems));

        // set click listener for the drawerList
        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawerLayout.closeDrawers();

                String title = drawerListItems[position];

                TextView tv = (TextView) getActivity().findViewById(R.id.title_text);
                if ("全部".equals(title)) {
                    keyword = null;
                    tv.setText(getResources().getString(R.string.app_name));
                } else if ("长三角".equals(title)) {
                    keyword = "华东|上海|杭州|南京|宁波|江浙";
                    tv.setText("长三角");
                } else if ("珠三角".equals(title)) {
                    keyword = "广州|深圳|香港|珠三角";
                    tv.setText("珠三角");
                } else if ("京津冀".equals(title)) {
                    keyword = "北京|天津";
                    tv.setText("京津冀");
                } else if ("华中".equals(title)) {
                    keyword = "武汉|长沙";
                    tv.setText("华中");
                } else if ("西南".equals(title)) {
                    keyword = "成都|重庆|昆明|四川";
                    tv.setText("西南");
                }

                firstDoRefresh = true;
                currPage = 0;
                handler.sendEmptyMessage(MESSAGE_REFRESH);

                Log.d(TAG, "drawerLayout: " + keyword);
            }
        });

        pullToRefreshView = (PullToRefreshView) content.findViewById(R.id.pull_to_refresh);
        pullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        ticketListView = (ListView) pullToRefreshView.findViewById(R.id.radar_list_view);
        ticketListView.setAdapter(listViewAdapter);

        ticketListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listViewAdapter.getCount() == 0) {
                    return;
                }
                Intent intent = new Intent(getActivity(), WebActivity.class);
                RadarItem radarItem = (RadarItem) listViewAdapter.getItem(position);
                intent.putExtra("url", radarItem.getUrl());
                intent.putExtra("title", radarItem.getAuthor());
                intent.putExtra("content", radarItem.getTitle());
                intent.putExtra("image", radarItem.getImage());
                startActivity(intent);
            }
        });

        createFooterView();
        ticketListView.addFooterView(footerViewLoading);
        footerViewLoading.setVisibility(View.GONE);

        return content;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ticketListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItem = firstVisibleItem + visibleItemCount;
                if (!footerViewLoadingVisible && totalItemCount > visibleItemCount) {
                    footerViewLoading.setVisibility(View.VISIBLE);
                    ticketListView.setFooterDividersEnabled(false);
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
            if (FileUtils.fileExists(getActivity(), RADAR_LISTVIEW_HISTORY_FILE)) {
                updateItems = TicketLoader.parse(FileUtils.readFile(getActivity(), RADAR_LISTVIEW_HISTORY_FILE));
            } else {
                updateItems = new ArrayList<>();
            }
            handler.sendEmptyMessage(MESSAGE_REFRESH);
        } catch (Exception e) {
            Log.e(TAG, "Radar fragment load local json file failed.", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(networkStateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(networkStateReceiver);
    }

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkUtils.isNetworkConnected(getActivity())) {
                Log.d(TAG, "Network available");
                networkErrBar.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "Network unavailable");
                networkErrBar.setVisibility(View.VISIBLE);
            }
        }
    };

    public void onDestroy() {
        // remove all messages, in case fragment or activity is destroyed
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private static class UiHandler extends WeakReferenceHandler<RadarFragment> {

        public UiHandler(RadarFragment fragment) {
            super(fragment);
        }

        @Override
        protected void handleMessage(RadarFragment fragment, Message msg) {
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
        Log.d(TAG, "message do clear and refresh, updateItems size: " + updateItems.size());
        if (listViewAdapter.getAllData().size() > 0) {
            ticketListView.setSelection(0);
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (updateItems.size() > 0) {
                    listViewAdapter.getAllData().clear();
                }
                listViewAdapter.addData(updateItems);
                listViewAdapter.notifyDataSetChanged();
                pullToRefreshView.setRefreshing(false);
                firstDoRefresh = false;
                currPage = 1;
            }
        }, 100);
    }

    private void handleLoadMoreMsg() {
        Log.d(TAG, "message load more, updateItems size: " + updateItems.size());
        footerViewLoading.setVisibility(View.GONE);
        ticketListView.setFooterDividersEnabled(true);
        footerViewLoadingVisible = false;
        isLoadingData = false;
        listViewAdapter.addData(updateItems);
        listViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (drawerLayout.isDrawerOpen(drawerList)) {
            drawerLayout.closeDrawers();
        } else {
            drawerLayout.openDrawer(drawerList);
        }
    }

    private void loadMore() {
        Log.d(TAG, "doLoadMore");
        new Thread() {
            @Override
            public void run() {
                updateItems = new ArrayList<>(32);
                Set<Integer> idSet = new HashSet<>(listViewAdapter.getAllData().size() * 2);
                try {
                    for (RadarItem item : listViewAdapter.getAllData()) {
                        idSet.add(item.getId());
                    }
                    while (true) {
                        List<RadarItem> items = null;
                        if (keyword == null) {
                            items = TicketLoader.load(getActivity(), currPage + 1);
                        } else {
                            items = SearchLoader.load(currPage + 1, keyword);
                        }
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
                updateItems = new ArrayList<>(32);
                Set<Integer> idSet = new HashSet<>(listViewAdapter.getAllData().size() * 2);
                try {
                    for (RadarItem item : listViewAdapter.getAllData()) {
                        idSet.add(item.getId());
                    }
                    while (!loadFinished) {
                        List<RadarItem> items = null;
                        if (keyword == null) {
                            items = TicketLoader.load(getActivity(), page + 1);
                        } else {
                            items = SearchLoader.load(page + 1, keyword);
                        }
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
                    } else {
                        handler.sendEmptyMessage(MESSAGE_REFRESH);
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
