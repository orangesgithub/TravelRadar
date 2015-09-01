package com.smart.travel;


import android.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smart.travel.model.RadarItem;
import com.smart.travel.model.SearchItem;
import com.smart.travel.utils.DensityUtil;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private List<SearchItem> searchItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        searchItems = new ArrayList<>();

        SearchItem item = new SearchItem();
        item.setClassify("最近检索");

        item.addItem("长三角");
        item.addItem("华中");
        item.addItem("天津");

        searchItems.add(item);

        item = new SearchItem();
        item.setClassify("出发地");

        item.addItem("长三角");
        item.addItem("珠三角");
        item.addItem("京津冀");
        item.addItem("华中");
        item.addItem("西南");
        item.addItem("天津");
        item.addItem("成都");
        item.addItem("重庆");
        item.addItem("武汉");
        item.addItem("长沙");

        searchItems.add(item);

        item = new SearchItem();
        item.setClassify("节假日");

        item.addItem("春节");
        item.addItem("国庆中秋");
        item.addItem("学生假期");

        searchItems.add(item);

        item = new SearchItem();
        item.setClassify("热门旅游城市");

        item.addItem("欧洲");
        item.addItem("美国");
        item.addItem("澳新");
        item.addItem("澳新");
        item.addItem("澳新");
        item.addItem("澳新");
        item.addItem("澳新");

        searchItems.add(item);

        item = new SearchItem();
        item.setClassify("旅游主题");

        item.addItem("意大利记忆");
        item.addItem("蓝色爱情海");
        item.addItem("浪漫法兰西");
        item.addItem("浪漫法兰西");
        item.addItem("浪漫法兰西");
        item.addItem("浪漫法兰西");
        item.addItem("浪漫法兰西");
        item.addItem("浪漫法兰");
        item.addItem("浪漫法兰");
        item.addItem("浪漫法兰");
        item.addItem("浪漫法兰");
        item.addItem("浪漫法兰西");
        item.addItem("浪漫法兰");

        searchItems.add(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.search_fragment, container, false);

        LinearLayout linearLayout = (LinearLayout) content.findViewById(R.id.search_container);
        linearLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.color_white));

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

            linearLayout.addView(flowLayoutTitle, layoutParamsTitle);

            int dividerPadding = DensityUtil.dip2px(getActivity(), 6);

            View divider = new View(getActivity());
            divider.setBackgroundColor(getActivity().getResources().getColor(R.color.color_setting_sep));
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(FlowLayout.LayoutParams.MATCH_PARENT, 2);
            dividerParams.bottomMargin = dividerPadding;
            linearLayout.addView(divider, dividerParams);

            FlowLayout flowLayoutItems = new FlowLayout(getActivity());
            FlowLayout.LayoutParams layoutParamsItems = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.MATCH_PARENT, FlowLayout.LayoutParams.WRAP_CONTENT);

            int paddintFlawLayoutItems = DensityUtil.dip2px(getActivity(), 6);
            flowLayoutItems.setPadding(paddintFlawLayoutItems, 0, paddintFlawLayoutItems, 0);

            int paddingLeft = DensityUtil.dip2px(getActivity(), 6);
            int paddingRight = DensityUtil.dip2px(getActivity(), 6);
            paddingTB = DensityUtil.dip2px(getActivity(), 4);

            for (int j = 0; j < item.getItems().size(); j++) {
                final TextView textView2 = new TextView(getActivity());
                textView2.setText(item.getItems().get(j));
                textView2.setPadding(paddingLeft, paddingTB, paddingRight, paddingTB);

                textView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textView2.setBackgroundResource(R.drawable.search_item_click);
                        Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                        TextView tv = (TextView) v;
                        intent.putExtra("keyword", tv.getText());
                        startActivity(intent);
                    }
                });

                flowLayoutItems.addView(textView2);
            }

            linearLayout.addView(flowLayoutItems, layoutParamsItems);

            View divider2 = new View(getActivity());
            divider2.setBackgroundColor(getActivity().getResources().getColor(R.color.color_setting_sep));
            LinearLayout.LayoutParams dividerParams2 = null;
            dividerParams2 = new LinearLayout.LayoutParams(FlowLayout.LayoutParams.MATCH_PARENT, 2);
            dividerParams2.topMargin = dividerPadding;
            linearLayout.addView(divider2, dividerParams2);
        }

        return content;
    }
}
