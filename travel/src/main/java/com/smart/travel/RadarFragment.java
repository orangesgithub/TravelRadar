package com.smart.travel;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import static android.R.layout.simple_list_item_1;


public class RadarFragment extends Fragment implements View.OnClickListener {
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private String[] drawerListItems;

    private ViewPager viewPager;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        drawerListItems = getResources().getStringArray(R.array.city_list);

        // Inflate the layout for this fragment
        View content = inflater.inflate(R.layout.radar_fragment, container, false);
        drawerLayout = (DrawerLayout)content.findViewById(R.id.drawer_layout);
        drawerList = (ListView)content.findViewById(R.id.right_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(getActivity(), simple_list_item_1, drawerListItems));

        // set click listener for the drawerList
        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawerLayout.closeDrawers();
            }
        });

        viewPager = (ViewPager)content.findViewById(R.id.radar_pager);
        MyAdapter adapter = new MyAdapter();
        // Replace with your own listView
        ListView listView = new ListView(getActivity());
        listView.setAdapter(new ArrayAdapter<>(getActivity(), simple_list_item_1, drawerListItems));
        ListView listView2 = new ListView(getActivity());
        listView2.setAdapter(new ArrayAdapter<>(getActivity(), simple_list_item_1, drawerListItems));
        ListView listView3 = new ListView(getActivity());
        listView3.setAdapter(new ArrayAdapter<>(getActivity(), simple_list_item_1, drawerListItems));

        adapter.add(listView);
        adapter.add(listView2);
        adapter.add(listView3);
        viewPager.setAdapter(adapter);
        return content;
    }

    @Override
    public void onClick(View v) {
        if (drawerLayout.isDrawerOpen(drawerList)) {
            drawerLayout.closeDrawers();
        } else {
            drawerLayout.openDrawer(drawerList);
        }
    }

    public class MyAdapter extends PagerAdapter {
        private List<ListView> views = new ArrayList<ListView>();
        private String[] titleContainer;

        public MyAdapter() {
            titleContainer = getResources().getStringArray(R.array.tab_items);
        }

        public void add(ListView view) {
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

        public void destroyItem (ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }

        public Object instantiateItem (ViewGroup container, int position) {
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleContainer[position];
        }
    }
}
