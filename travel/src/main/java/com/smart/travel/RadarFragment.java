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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.travel.adapter.TravelListViewAdapter;
import com.smart.travel.model.RadarItem;
import com.smart.travel.net.TicketLoader;
import com.smart.travel.utils.FileUtils;
import com.smart.travel.utils.NetworkUtils;
import com.yalantis.phoenix.PullToRefreshView;

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
    private boolean footerViewLoadingVisiable = false;

    private RelativeLayout networkErrBar;

    // 第一次进入页面的时候，会首先显示本地数据，当网络数据下载完毕的时候，会删除ListView中的本地数据并显示最新的数据
    private boolean firstDoRefresh = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                intent.putExtra("title", drawerListItems[position]);
                startActivity(intent);
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

        return content;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ticketListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItem = firstVisibleItem + visibleItemCount;
                if (!footerViewLoadingVisiable && totalItemCount > visibleItemCount) {
                    ticketListView.addFooterView(footerViewLoading);
                    ticketListView.setFooterDividersEnabled(false);
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
                    Log.d(TAG, "message do clear and refresh, updateItems size: " + updateItems.size());
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
                    Log.d(TAG, "message load more, updateItems size: " + updateItems.size());
                    ticketListView.removeFooterView(footerViewLoading);
                    ticketListView.setFooterDividersEnabled(true);
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

    @Override
    public void onClick(View v) {
        if (drawerLayout.isDrawerOpen(drawerList)) {
            drawerLayout.closeDrawers();
        } else {
            drawerLayout.openDrawer(drawerList);
        }
    }

    private void loadMore() {
        Log.d(TAG, "doRefresh");
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
                        List<RadarItem> items = TicketLoader.load(getActivity(), currPage + 1);
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
                        List<RadarItem> items = TicketLoader.load(getActivity(), page + 1);
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
