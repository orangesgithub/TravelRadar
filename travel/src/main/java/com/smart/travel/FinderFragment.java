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

import com.smart.travel.adapter.FinderListViewAdapter;
import com.smart.travel.model.RadarItem;
import com.smart.travel.net.FinderLoader;
import com.yalantis.phoenix.PullToRefreshView;

import java.util.List;

public class FinderFragment extends Fragment {
    private static final String TAG = "FinderFragment";

    private static final int MESSAGE_LOADMORE = 1;
    private static final int MESSAGE_REFRESH = 2;

    private ListView finderListView;
    private FinderListViewAdapter finderListViewAdapter;

    private int currPage = 0;

    private List<RadarItem> finderListItems;


    private LinearLayout footerViewLoading;
    private int lastItem;
    private boolean isLoadingData = false;
    private boolean footerViewLoadingVisiable = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        finderListViewAdapter = new FinderListViewAdapter(getActivity());
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.finder_fragment, container, false);
        final PullToRefreshView pullToRefreshView = (PullToRefreshView) content.findViewById(R.id.pull_to_refresh);
        pullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullToRefreshView.setRefreshing(false);
                    }
                }, 1500);
            }
        });

        finderListView = (ListView) pullToRefreshView.findViewById(R.id.finder_list_view);
        finderListView.setAdapter(finderListViewAdapter);

        finderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), WebActivity.class);
                RadarItem radarItem = (RadarItem) finderListViewAdapter.getItem(position);
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
        finderListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItem = firstVisibleItem + visibleItemCount;
                Log.d(TAG, "onScroll callback: " + firstVisibleItem + ", " + visibleItemCount + ", " + lastItem);
                if (!footerViewLoadingVisiable) {
                    finderListView.addFooterView(footerViewLoading);
                    finderListView.setFooterDividersEnabled(false);
                    footerViewLoadingVisiable = true;
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (lastItem >= finderListViewAdapter.getCount()
                        && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && !isLoadingData) {
                    isLoadingData = true;
                    Log.d(TAG, "start to pull new list");
                    finderListViewAdapter.notifyDataSetChanged();
                    loadMore();
                }
            }
        });
        loadMore();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH:
                    break;
                case MESSAGE_LOADMORE:
                    finderListView.removeFooterView(footerViewLoading);
                    finderListView.setFooterDividersEnabled(true);
                    footerViewLoadingVisiable = false;
                    isLoadingData = false;
                    finderListViewAdapter.addData(finderListItems);
                    finderListItems = null;
                    finderListViewAdapter.notifyDataSetChanged();
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
                try {
                    finderListItems = FinderLoader.load(currPage++);
                    handler.sendEmptyMessage(MESSAGE_LOADMORE);
                } catch (Exception e) {
                    Log.e(TAG, "Radar Http Exception", e);
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