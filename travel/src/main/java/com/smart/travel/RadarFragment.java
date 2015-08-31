package com.smart.travel;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.smart.travel.adapter.TravelItemListViewAdapter;
import com.smart.travel.model.TravelItem;
import com.smart.travel.net.AdviceLoader;
import com.smart.travel.net.TicketLoader;
import com.smart.travel.view.ScrollViewFlipper;
import com.smart.travel.view.XListView;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

import static android.R.layout.simple_list_item_1;


public class RadarFragment extends Fragment implements View.OnClickListener, XListView.IXListViewListener {
    private static final int MESSAGE_TICKET_LOADMORE = 1;
    private static final int MESSAGE_TICKET_REFRESH = 2;
    private static final int MESSAGE_ADVICE_LOADMORE = 3;
    private static final int MESSAGE_ADVICE_REFRESH = 4;

    private static final int VIEW_PAGE_TICKET = 0;
    private static final int VIEW_PAGE_ADVICE = 1;

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private String[] drawerListItems;

    private static final String TAG = "RadarFragment";

    private TravelItemListViewAdapter ticketItemAdapter;
    private TravelItemListViewAdapter adviceItemAdapter;

    private ViewPager viewPager;

    private View ticketContainer;
    private XListView ticketListView;
    private View adviceContainer;
    private XListView adviceListView;

    private int ticketCurrPage = 0;
    private int adviceCurrPage = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ticketItemAdapter = new TravelItemListViewAdapter(getActivity());
        adviceItemAdapter = new TravelItemListViewAdapter(getActivity());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        drawerListItems = getResources().getStringArray(R.array.city_list);

        // Inflate the layout for this fragment
        View content = inflater.inflate(R.layout.radar_fragment, container, false);
        drawerLayout = (DrawerLayout) content.findViewById(R.id.drawer_layout);
        drawerList = (ListView) content.findViewById(R.id.right_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(getActivity(), simple_list_item_1, drawerListItems));

        // set click listener for the drawerList
        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawerLayout.closeDrawers();
            }
        });

        viewPager = (ViewPager) content.findViewById(R.id.radar_pager);
        MyAdapter adapter = new MyAdapter();
        // Replace with your own listView
        ticketContainer = getActivity().getLayoutInflater().inflate(R.layout.radar_view_1, null);
        ticketListView = (XListView) ticketContainer.findViewById(R.id.radar_list_view1);
        ticketListView.setAdapter(ticketItemAdapter);
        ticketListView.setPullLoadEnable(true);
        ticketListView.setXListViewListener(this);

        ticketListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), WebActivity.class);
                TravelItem travelItem = (TravelItem) ticketItemAdapter.getItem(position);
                intent.putExtra("url", travelItem.getUrl());
                startActivity(intent);
            }
        });

        adviceContainer = getActivity().getLayoutInflater().inflate(R.layout.radar_view_1, null);
        adviceListView = (XListView) adviceContainer.findViewById(R.id.radar_list_view1);
        adviceListView.setAdapter(adviceItemAdapter);
        adviceListView.setPullLoadEnable(true);
        adviceListView.setXListViewListener(this);

        adviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), WebActivity.class);
                TravelItem travelItem = (TravelItem) adviceItemAdapter.getItem(position);
                intent.putExtra("url", travelItem.getUrl());
                startActivity(intent);
            }
        });

        adapter.add(ticketContainer);
        adapter.add(adviceContainer);
        viewPager.setAdapter(adapter);

        TabPageIndicator indicator = (TabPageIndicator) content.findViewById(R.id.tab_indicator);
        indicator.setViewPager(viewPager);

        LinearLayout flipperIndicator = (LinearLayout) content.findViewById(R.id.flipper_indicator);
        ScrollViewFlipper imageFlipper = (ScrollViewFlipper) content.findViewById(R.id.image_flipper);
        imageFlipper.setIndicator(flipperIndicator);
        return content;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_TICKET_REFRESH:
                    ticketListView.stopRefresh();
                    ticketListView.setRefreshTime("刚刚");
                    break;
                case MESSAGE_ADVICE_REFRESH:
                    adviceListView.stopRefresh();
                    adviceListView.setRefreshTime("刚刚");
                    break;
                case MESSAGE_TICKET_LOADMORE:
                    ticketItemAdapter.notifyDataSetChanged();
                    ticketListView.stopLoadMore();
                    break;
                case MESSAGE_ADVICE_LOADMORE:
                    adviceItemAdapter.notifyDataSetChanged();
                    adviceListView.stopLoadMore();
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

    @Override
    public void onRefresh() {
        new Thread() {
            @Override
            public void run() {
                if (viewPager.getCurrentItem() == VIEW_PAGE_TICKET) {
                    handler.sendEmptyMessage(MESSAGE_TICKET_REFRESH);
                } else if (viewPager.getCurrentItem() == VIEW_PAGE_ADVICE) {
                    handler.sendEmptyMessage(MESSAGE_ADVICE_REFRESH);
                }
            }
        }.start();
    }

    @Override
    public void onLoadMore() {
        loadMore();
    }

    private void loadMore() {
        new Thread() {
            @Override
            public void run() {
                try {
                    if (viewPager.getCurrentItem() == VIEW_PAGE_TICKET) {
                        List<TravelItem> ticketListItems = TicketLoader.load(ticketCurrPage++);
                        ticketItemAdapter.addData(ticketListItems);
                        handler.sendEmptyMessage(MESSAGE_TICKET_LOADMORE);
                    } else if (viewPager.getCurrentItem() == VIEW_PAGE_ADVICE) {
                        List<TravelItem> adviceListItems = AdviceLoader.load(adviceCurrPage++);
                        adviceItemAdapter.addData(adviceListItems);
                        handler.sendEmptyMessage(MESSAGE_ADVICE_LOADMORE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Radar Http Exception", e);
                }
            }
        }.start();
    }

    public class MyAdapter extends PagerAdapter {
        private List<View> views = new ArrayList<View>();
        private String[] titleContainer;

        public MyAdapter() {
            titleContainer = getResources().getStringArray(R.array.tab_items);
        }

        public void add(View view) {
            views.add(view);
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }

        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleContainer[position];
        }
    }
}
