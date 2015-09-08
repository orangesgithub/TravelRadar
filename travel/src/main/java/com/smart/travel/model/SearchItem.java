package com.smart.travel.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yfan10x on 2015/8/29.
 */
public class SearchItem {

    private String classify;
    private List<Item> items;

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setNames(List<String> names) {
        this.items = items;
    }

    public void addItem(String strItem, String keyword) {
        if (items == null) {
            items = new ArrayList<>();
        }

        Item item = new Item();
        item.name = strItem;
        item.keyword = keyword;

        items.add(item);
    }

    public static class Item {
        public String name;
        public String keyword;
    }

}
