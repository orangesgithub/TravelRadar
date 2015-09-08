package com.smart.travel;

import android.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smart.travel.model.SearchItem;
import com.smart.travel.net.SearchListLoader;
import com.smart.travel.utils.DensityUtil;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";

    private LinearLayout keywordLayout;
    private List<SearchItem> searchItems;

    private Map<TextView, SearchItem.Item> tvItemMap = new HashMap<>(64);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.search_fragment, container, false);

        final EditText searchText = (EditText) content.findViewById(R.id.search_text);

        keywordLayout = (LinearLayout) content.findViewById(R.id.search_container);
        LinearLayout parentLayout = (LinearLayout) keywordLayout.getParent();
        parentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
                return false;
            }
        });

        keywordLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.color_white));

        new Thread() {
            @Override
            public void run() {
                try {
                    searchItems = SearchListLoader.load();

                    SearchItem item = new SearchItem();
                    item.setClassify("最近检索");
                    item.addItem("长三角", "华东|上海|杭州|南京|宁波|江浙");
                    searchItems.add(0, item);
                } catch (Exception e) {
                    Log.e(TAG, "Load search list failed.", e);
                }

                if (searchItems != null) {
                    keywordLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            fillSearchKeywords();
                        }
                    });
                }
            }
        }.start();

        return content;
    }

    private void fillSearchKeywords() {
        for (int i = 0; i < searchItems.size(); i++) {
            SearchItem item = searchItems.get(i);
            FlowLayout flowLayoutTitle = new FlowLayout(getActivity());
            FlowLayout.LayoutParams layoutParamsTitle = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.MATCH_PARENT, FlowLayout.LayoutParams.WRAP_CONTENT);
            flowLayoutTitle.setBackgroundColor(getActivity().getResources().getColor(R.color.color_search_classify));

            TextView textView = new TextView(getActivity());
            textView.setText(item.getClassify());
            textView.setTextColor(getResources().getColor(R.color.color_text_search_title));

            int paddingLR = DensityUtil.dip2px(getActivity(), 8);
            int paddingTB = DensityUtil.dip2px(getActivity(), 5);

            textView.setPadding(paddingLR, paddingTB, paddingLR, paddingTB);
            flowLayoutTitle.addView(textView);

            keywordLayout.addView(flowLayoutTitle, layoutParamsTitle);

            int dividerPadding = DensityUtil.dip2px(getActivity(), 6);

            View divider = new View(getActivity());
            divider.setBackgroundColor(getActivity().getResources().getColor(R.color.color_setting_sep));
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(FlowLayout.LayoutParams.MATCH_PARENT, 2);
            dividerParams.bottomMargin = dividerPadding;
            keywordLayout.addView(divider, dividerParams);

            FlowLayout flowLayoutItems = new FlowLayout(getActivity());
            FlowLayout.LayoutParams layoutParamsItems = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.MATCH_PARENT, FlowLayout.LayoutParams.WRAP_CONTENT);

            int paddingFlawLayoutItems = DensityUtil.dip2px(getActivity(), 6);
            flowLayoutItems.setPadding(paddingFlawLayoutItems, 0, paddingFlawLayoutItems, 0);

            int paddingLeft = DensityUtil.dip2px(getActivity(), 6);
            int paddingRight = DensityUtil.dip2px(getActivity(), 6);
            paddingTB = DensityUtil.dip2px(getActivity(), 4);

            for (int j = 0; j < item.getItems().size(); j++) {
                final TextView textView2 = new TextView(getActivity());
                textView2.setText(item.getItems().get(j).name);
                textView2.setPadding(paddingLeft, paddingTB, paddingRight, paddingTB);
                tvItemMap.put(textView2, item.getItems().get(j));

                textView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                        SearchItem.Item si = tvItemMap.get(v);
                        intent.putExtra("title", si.name);
                        intent.putExtra("keyword", si.keyword);
                        startActivity(intent);
                    }
                });
                textView2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                textView2.setBackgroundResource(R.drawable.search_item_click);
                                break;
                        }
                        return false;
                    }
                });

                flowLayoutItems.addView(textView2);
            }

            keywordLayout.addView(flowLayoutItems, layoutParamsItems);

            View divider2 = new View(getActivity());
            divider2.setBackgroundColor(getActivity().getResources().getColor(R.color.color_setting_sep));
            LinearLayout.LayoutParams dividerParams2 = null;
            dividerParams2 = new LinearLayout.LayoutParams(FlowLayout.LayoutParams.MATCH_PARENT, 2);
            dividerParams2.topMargin = dividerPadding;
            keywordLayout.addView(divider2, dividerParams2);
        }
    }

}
