package com.smart.travel.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yfan10x on 2015/8/29.
 */
public class SearchItem {

    private String classify;
    private List<String> items;

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    public List<String> getItems() {
        return items;
    }

    public void setNames(List<String> names) {
        this.items = items;
    }

    public void addItem(String item) {
        if (items == null) {
            items = new ArrayList<>();
        }

        items.add(item);
    }

}
