package com.smart.travel.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smart.travel.R;
import com.smart.travel.model.RadarItem;

import java.util.ArrayList;
import java.util.List;

public class AdviceListViewAdapter extends BaseAdapter {
    private static final String TAG = "AdviceListViewAdapter";

    private Context context;
    private List<RadarItem> listItems = new ArrayList<RadarItem>();
    private LayoutInflater listContainer;

    public AdviceListViewAdapter(Context context) {
        this.context = context;
        this.listContainer = LayoutInflater.from(context);
    }

    public void addData(List<RadarItem> items) {
        this.listItems.addAll(items);
    }

    public void addDataBegin(List<RadarItem> items) {
        this.listItems.addAll(0, items);
    }

    public List<RadarItem> getAllData() {
        return this.listItems;
    }

    @Override
    public int getCount() {
        return (listItems.size());
    }

    @Override
    public Object getItem(int index) {
        return listItems.get(index);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RadarItem item = listItems.get(position);

        if (convertView == null) {
            convertView = listContainer.inflate(R.layout.list_advice_item, null);
        }

        final ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
        TextView fromTextView = (TextView) convertView.findViewById(R.id.from);
        TextView dateTextView = (TextView) convertView.findViewById(R.id.date);
        TextView briefTextView = (TextView) convertView.findViewById(R.id.brief);

        fromTextView.setText(item.getAuthor());
        dateTextView.setText(item.getPubdate());
        briefTextView.setText(item.getTitle());


        ImageLoader.getInstance().displayImage(item.getImage(), imageView);

        return convertView;
    }
}