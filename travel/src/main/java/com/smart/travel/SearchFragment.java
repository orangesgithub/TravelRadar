package com.smart.travel;

import android.app.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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


    private FrameLayout searchFrame;
    private LinearLayout searchInner;
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
        searchFrame = (FrameLayout) content.findViewById(R.id.search_frame);

        searchInner = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.search_fragment_inner, searchFrame, false);
        final EditText searchText = (EditText) searchInner.findViewById(R.id.search_text);

        keywordLayout = (LinearLayout) searchInner.findViewById(R.id.search_container);
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

        return content;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadSearchKeywords();
    }

    private void loadSearchKeywords() {
        new Thread() {
            @Override
            public void run() {
                try {
                    searchItems = SearchListLoader.load(getActivity());

                    SearchItem item = new SearchItem();
                    item.setClassify("最近检索");
                    item.addItem("长三角", "华东|上海|杭州|南京|宁波|江浙");
                    searchItems.add(0, item);
                } catch (Exception e) {
                    Log.e(TAG, "Load search list failed.", e);
                    searchFrame.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "加载数据失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                if (searchItems != null) {
                    searchFrame.post(new Runnable() {
                        @Override
                        public void run() {
                            searchFrame.removeAllViews();
                            searchFrame.addView(searchInner);
                            fillSearchKeywords();
                        }
                    });
                }
            }
        }.start();
    }

    private void fillSearchKeywords() {
        for (int i = 0; i < searchItems.size(); i++) {
            SearchItem item = searchItems.get(i);
            FlowLayout flowLayoutTitle = new FlowLayout(getActivity());
            FlowLayout.LayoutParams layoutParamsTitle = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.MATCH_PARENT, FlowLayout.LayoutParams.WRAP_CONTENT);
            flowLayoutTitle.setBackgroundColor(getActivity().getResources().getColor(R.color.color_search_classify));

            TextView textViewClassify = new TextView(getActivity());
            textViewClassify.setText(item.getClassify());
            textViewClassify.setTextColor(getResources().getColor(R.color.color_text_search_title));

            int paddingLR = DensityUtil.dip2px(getActivity(), 8);
            int paddingTB = DensityUtil.dip2px(getActivity(), 5);

            textViewClassify.setPadding(paddingLR, paddingTB, paddingLR, paddingTB);
            flowLayoutTitle.addView(textViewClassify);

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
                final TextView textViewSearch = new TextView(getActivity());
                textViewSearch.setText(item.getItems().get(j).name);
                textViewSearch.setPadding(paddingLeft, paddingTB, paddingRight, paddingTB);
                tvItemMap.put(textViewSearch, item.getItems().get(j));

                textViewSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                        SearchItem.Item si = tvItemMap.get(v);
                        intent.putExtra("title", si.name);
                        intent.putExtra("keyword", si.keyword);
                        startActivity(intent);
                    }
                });
                textViewSearch.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                textViewSearch.setBackgroundResource(R.drawable.search_item_click);
                                break;
                        }
                        return false;
                    }
                });

                flowLayoutItems.addView(textViewSearch);
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
