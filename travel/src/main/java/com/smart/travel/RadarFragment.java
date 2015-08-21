package com.smart.travel;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import static android.R.layout.simple_list_item_1;


public class RadarFragment extends Fragment {
    private ListView drawerList;
    private String[] drawerListItems;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        drawerListItems = getResources().getStringArray(R.array.drawer_list);

        Log.e("YangJun", drawerListItems[0] + drawerListItems[1]);
        // Inflate the layout for this fragment
        View content = inflater.inflate(R.layout.radar_fragment, container, false);

        drawerList = (ListView)content.findViewById(R.id.right_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(getActivity(), simple_list_item_1, drawerListItems));
        return content;
    }
}
