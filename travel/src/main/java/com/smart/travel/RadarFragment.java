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
import android.widget.ViewFlipper;

import com.smart.travel.adapter.TravelItemListViewAdapter;
import com.smart.travel.model.TravelItem;
import com.smart.travel.utils.Format;
import com.smart.travel.view.ScrollViewFlipper;
import com.smart.travel.view.XListView;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.layout.simple_list_item_1;


public class RadarFragment extends Fragment implements View.OnClickListener, XListView.IXListViewListener {
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private String[] drawerListItems;

    private static final String TAG = "RadarFragment";

    private TravelItemListViewAdapter ticketItemAdapter;
    private TravelItemListViewAdapter travelItemAdapter;
    private TravelItemListViewAdapter overseasItemAdapter;

    private ViewPager viewPager;

    private View container1;
    private XListView listView1;
    private View container2;
    private XListView listView2;
    private View container3;
    private XListView listView3;

    private int selectIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ticketItemAdapter = new TravelItemListViewAdapter(getActivity());
        List<TravelItem> listItems = new ArrayList<TravelItem>();
        for (int i = 0; i < 15; i++) {
            TravelItem travelItem = new TravelItem();
            if (i % 2 == 0) {
                travelItem.setImage("http://img1.3lian.com/img2011/w1/108/49/11.jpg");
            } else {
                travelItem.setImage("http://pic29.nipic.com/20130524/12724384_214225181194_2.jpg");
            }
            travelItem.setId(i);
            travelItem.setUrl("http://www.163.com");
            travelItem.setFrom("旅行雷达");
            travelItem.setDate(Format.formatDate(new Date(), "yyyy-MM-dd"));
            travelItem.setBrief("【国庆出行】5799元，普吉岛7天5晚自由行 - " + i);
            listItems.add(travelItem);
        }
        ticketItemAdapter.addData(listItems);

        travelItemAdapter = new TravelItemListViewAdapter(getActivity());
        listItems = new ArrayList<TravelItem>();
        for (int i = 0; i < 15; i++) {
            TravelItem travelItem = new TravelItem();
            if (i % 2 == 0) {
                travelItem.setImage("http://f.hiphotos.baidu.com/exp/whcrop=160,120/sign=d2660476b48f8c54e386936d555910c4/0df431adcbef760936015b072edda3cc7cd99e24.jpg");
            } else {
                travelItem.setImage("http://www.66666333.com/upload/original/20139251133364.jpg");
            }
            travelItem.setId(i);
            travelItem.setUrl("http://www.163.com");
            travelItem.setFrom("旅行雷达");
            travelItem.setDate(Format.formatDate(new Date(), "yyyy-MM-dd"));
            travelItem.setBrief("【国庆出行】5799元，普吉岛7天5晚自由行 - " + i);
            listItems.add(travelItem);
        }
        travelItemAdapter.addData(listItems);

        overseasItemAdapter = new TravelItemListViewAdapter(getActivity());
        listItems = new ArrayList<TravelItem>();
        for (int i = 0; i < 15; i++) {
            TravelItem travelItem = new TravelItem();
            if (i % 2 == 0) {
                travelItem.setImage("http://jingdian.landtu.com/uploadimg/destinations/000100160056.jpg");
            } else {
                travelItem.setImage("http://pic8.nipic.com/20100629/3015544_135545034988_2.jpg");
            }
            travelItem.setId(i);
            travelItem.setUrl("http://www.163.com");
            travelItem.setFrom("旅行雷达");
            travelItem.setDate(Format.formatDate(new Date(), "yyyy-MM-dd"));
            travelItem.setBrief("【国庆出行】5799元，普吉岛7天5晚自由行 - " + i);
            listItems.add(travelItem);
        }

        overseasItemAdapter.addData(listItems);
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
        container1 = getActivity().getLayoutInflater().inflate(R.layout.radar_view_1, null);
        listView1 = (XListView) container1.findViewById(R.id.radar_list_view1);
        listView1.setAdapter(ticketItemAdapter);
        listView1.setPullLoadEnable(true);
        listView1.setXListViewListener(this);

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), WebActivity.class);
                TravelItem travelItem = (TravelItem) ticketItemAdapter.getItem(position);
                intent.putExtra("url", travelItem.getUrl());
                startActivity(intent);
            }
        });

        container2 = getActivity().getLayoutInflater().inflate(R.layout.radar_view_1, null);
        listView2 = (XListView) container2.findViewById(R.id.radar_list_view1);
        listView2.setAdapter(travelItemAdapter);
        listView2.setPullLoadEnable(true);
        listView2.setXListViewListener(this);

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), WebActivity.class);
                TravelItem travelItem = (TravelItem) travelItemAdapter.getItem(position);
                intent.putExtra("url", travelItem.getUrl());
                startActivity(intent);
            }
        });

        container3 = getActivity().getLayoutInflater().inflate(R.layout.radar_view_1, null);
        listView3 = (XListView) container3.findViewById(R.id.radar_list_view1);
        listView3.setAdapter(overseasItemAdapter);
        listView3.setPullLoadEnable(true);
        listView3.setXListViewListener(this);

        listView3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), WebActivity.class);
                TravelItem travelItem = (TravelItem) overseasItemAdapter.getItem(position);
                intent.putExtra("url", travelItem.getUrl());
                startActivity(intent);
            }
        });

        adapter.add(container1);
        adapter.add(container2);
        adapter.add(container3);
        viewPager.setAdapter(adapter);

        TabPageIndicator indicator = (TabPageIndicator) content.findViewById(R.id.tab_indicator);
        indicator.setViewPager(viewPager);


        LinearLayout flipperIndicator = (LinearLayout)content.findViewById(R.id.flipper_indicator);
        ScrollViewFlipper imageFlipper = (ScrollViewFlipper)content.findViewById(R.id.image_flipper);
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
                case 1:
                    if (viewPager.getCurrentItem() == 0) {
                        listView1.stopRefresh();
                        listView1.setRefreshTime("刚刚");
                    }else if (viewPager.getCurrentItem() == 1){
                        listView2.stopRefresh();
                        listView2.setRefreshTime("刚刚");
                    }else if (viewPager.getCurrentItem() == 2){
                        listView3.stopRefresh();
                        listView3.setRefreshTime("刚刚");
                    }
                    break;
                case 2:
                    List<TravelItem> listItems = new ArrayList<TravelItem>();
                    for (int i = 0; i < 15; i++) {
                        TravelItem travelItem = new TravelItem();
                        if (i % 2 == 0) {
                            travelItem.setImage("http://img1.3lian.com/img2011/w1/108/49/11.jpg");
                        } else {
                            travelItem.setImage("http://pic8.nipic.com/20100629/3015544_135545034988_2.jpg");
                        }
                        travelItem.setUrl("http://www.baidu.com");
                        travelItem.setId(i);
                        travelItem.setFrom("旅行雷达");
                        travelItem.setDate(Format.formatDate(new Date(), "yyyy-MM-dd"));
                        travelItem.setBrief("【国庆出行】5799元，普吉岛7天5晚自由行 - " + i);
                        listItems.add(travelItem);
                    }

                    if (viewPager.getCurrentItem() == 0) {
                        ticketItemAdapter.addData(listItems);
                        ticketItemAdapter.notifyDataSetChanged();

                        listView1.stopLoadMore();
                    }else if (viewPager.getCurrentItem() == 1){
                        travelItemAdapter.addData(listItems);
                        travelItemAdapter.notifyDataSetChanged();

                        listView2.stopLoadMore();
                    }else if (viewPager.getCurrentItem() == 2){
                        overseasItemAdapter.addData(listItems);
                        overseasItemAdapter.notifyDataSetChanged();

                        listView3.stopLoadMore();
                    }
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
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(1);

                Log.d(TAG, "refresh list =================");
            }
        }.start();
    }

    @Override
    public void onLoadMore() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(2);

                Log.d(TAG, "load more =================");
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
