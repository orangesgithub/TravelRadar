package com.smart.travel;

import android.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.travel.model.SearchItem;
import com.smart.travel.net.SearchListLoader;
import com.smart.travel.utils.DensityUtil;
import com.smart.travel.utils.FileUtils;

import org.apmem.tools.layouts.FlowLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";

    private static final String SEARCH_HISTORY_FILE = "radar_search_history.dat";
    private static final int MAX_SEARCH_HISTORY_COUNT = 8;

    private FrameLayout searchFrame;
    private LinearLayout searchInner;
    private LinearLayout keywordLayout;
    private List<SearchItem> searchItems;

    private Map<TextView, SearchItem.Item> tvItemMap = new HashMap<>(64);
    private FlowLayout searchHistoryLayout;
    private EditText searchText;

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
        searchText = (EditText) searchInner.findViewById(R.id.search_text);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String keyword = v.getText().toString().trim();
                    if (keyword.length() > 0) {
                        String title = keyword;

                        for (TextView tv : tvItemMap.keySet()) {
                            if (keyword.equals(tv.getText())) {
                                Log.d(TAG, "Find title in textview: " + keyword + ", " + tvItemMap.get(tv).keyword);
                                keyword = tvItemMap.get(tv).keyword;
                                break;
                            }
                        }

                        Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                        intent.putExtra("title", keyword);
                        intent.putExtra("keyword", keyword);
                        startActivity(intent);

                        updateSearchHistory(title, keyword);
                    } else {
                        return true;
                    }
                }
                return false;
            }
        });

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

    private void updateSearchHistory(final String title, final String keyword) {
        boolean layoutContains = false;
        for (int i = 0; i < searchHistoryLayout.getChildCount(); i++) {
            TextView tv = (TextView) searchHistoryLayout.getChildAt(i);
            if (tv.getText().equals(title)) {
                layoutContains = true;
                break;
            }
        }

        if (!layoutContains) {
            final StringBuffer stringBuffer = new StringBuffer(128);
            searchFrame.postDelayed(new Runnable() {
                @Override
                public void run() {
                    searchText.setText("");
                    if (searchHistoryLayout.getChildCount() >= MAX_SEARCH_HISTORY_COUNT) {
                        searchHistoryLayout.removeViewAt(MAX_SEARCH_HISTORY_COUNT - 1);
                    } else if (searchHistoryLayout.getChildCount() == 1) {
                        TextView tv = (TextView) searchHistoryLayout.getChildAt(0);
                        if (tv.getText().toString().length() == 0) {
                            searchHistoryLayout.removeView(tv);
                        }
                    }

                    int paddingLeft = DensityUtil.dip2px(getActivity(), 6);
                    int paddingRight = DensityUtil.dip2px(getActivity(), 6);
                    int paddingTB = DensityUtil.dip2px(getActivity(), 4);

                    SearchItem.Item item = new SearchItem.Item();
                    item.name = title;
                    item.keyword = keyword;

                    TextView textViewSearch = new TextView(getActivity());
                    textViewSearch.setText(title);
                    textViewSearch.setPadding(paddingLeft, paddingTB, paddingRight, paddingTB);
                    tvItemMap.put(textViewSearch, item);

                    textViewSearch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                            SearchItem.Item si = tvItemMap.get(v);
                            intent.putExtra("title", si.name);
                            intent.putExtra("keyword", si.keyword);
                            startActivity(intent);

                            updateSearchHistory(si.name, si.keyword);
                        }
                    });
                    textViewSearch.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    v.setBackgroundResource(R.drawable.search_item_click);
                                    break;
                            }
                            return false;
                        }
                    });

                    searchHistoryLayout.addView(textViewSearch, 0);

                    for (int i = 0; i < searchHistoryLayout.getChildCount(); i++) {
                        TextView tv = (TextView) searchHistoryLayout.getChildAt(i);
                        SearchItem.Item tvItem = tvItemMap.get(tv);
                        stringBuffer.append(tvItem.name + "," + tvItem.keyword + ";");
                    }

                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                FileUtils.writeFile(getActivity(), SEARCH_HISTORY_FILE, stringBuffer.toString().getBytes("utf-8"));
                            } catch (IOException e) {
                                Log.e(TAG, "Write search history data failed.", e);
                            }
                        }
                    }.start();
                }
            }, 200);
        }
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
                    searchItems.add(0, item);

                    if (FileUtils.fileExists(getActivity(), SEARCH_HISTORY_FILE)) {
                        String searchHistory = FileUtils.readFile(getActivity(), SEARCH_HISTORY_FILE);
                        String splits[] = searchHistory.split(";");
                        for (String split : splits) {
                            if (split.trim().length() > 0) {
                                String subSplit[] = split.split(",");
                                if (subSplit.length == 2) {
                                    item.addItem(subSplit[0], subSplit[1]);
                                }
                            }
                        }
                    } else {
                        // 用户第一次使用或数据被清除后，加一个看不到的TextView来填充最近检索的位置栏的位置，不至于UI叠在一起
                        item.addItem("", "北京|天津");
                    }
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
            if (i == 0) {
                searchHistoryLayout = flowLayoutItems;
            }

            int paddingFlawLayoutItems = DensityUtil.dip2px(getActivity(), 6);
            flowLayoutItems.setPadding(paddingFlawLayoutItems, 0, paddingFlawLayoutItems, 0);

            int paddingLeft = DensityUtil.dip2px(getActivity(), 6);
            int paddingRight = DensityUtil.dip2px(getActivity(), 6);
            paddingTB = DensityUtil.dip2px(getActivity(), 4);

            for (int j = 0; j < item.getItems().size(); j++) {
                TextView textViewSearch = new TextView(getActivity());
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

                        updateSearchHistory(si.name, si.keyword);
                    }
                });
                textViewSearch.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                v.setBackgroundResource(R.drawable.search_item_click);
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
