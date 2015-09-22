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
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_SPECIAL = 1;

    private List<RadarItem> listItems = new ArrayList<RadarItem>();
    private LayoutInflater listContainer;

    public AdviceListViewAdapter(Context context) {
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

    public int getViewTypeCount() {
        return 2;
    }

    public int getItemViewType(int position) {
        RadarItem item = listItems.get(position);
        if (item.getShowType() == 3) {
            return TYPE_SPECIAL;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RadarItem item = listItems.get(position);
        int viewType = getItemViewType(position);

        ViewHolder viewHolder;
        ViewHolder2 viewHolder2;

        switch (viewType) {
            case TYPE_SPECIAL:
                if (convertView == null) {
                    viewHolder2 = new ViewHolder2();
                    convertView = listContainer.inflate(R.layout.list_view_item_2, null);
                    viewHolder2.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                    viewHolder2.fromTextView = (TextView) convertView.findViewById(R.id.from);
                    viewHolder2.dateTextView = (TextView) convertView.findViewById(R.id.date);
                    viewHolder2.briefTextView = (TextView) convertView.findViewById(R.id.brief);
                    convertView.setTag(viewHolder2);
                } else {
                    viewHolder2 = (ViewHolder2)convertView.getTag();
                }

                viewHolder2.fromTextView.setText(item.getAuthor());
                viewHolder2.dateTextView.setText(item.getPubdate());
                viewHolder2.briefTextView.setText(item.getTitle());
                ImageLoader.getInstance().displayImage(item.getImage(), viewHolder2.imageView);
                break;
            case TYPE_NORMAL:
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    convertView = listContainer.inflate(R.layout.list_view_item, null);
                    viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                    viewHolder.fromTextView = (TextView) convertView.findViewById(R.id.from);
                    viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.date);
                    viewHolder.briefTextView = (TextView) convertView.findViewById(R.id.brief);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder)convertView.getTag();
                }

                viewHolder.fromTextView.setText(item.getAuthor());
                viewHolder.dateTextView.setText(item.getPubdate());
                viewHolder.briefTextView.setText(item.getTitle());
                ImageLoader.getInstance().displayImage(item.getImage(), viewHolder.imageView);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView fromTextView;
        TextView dateTextView;
        TextView briefTextView;
    }

    static class ViewHolder2 {
        ImageView imageView;
        TextView fromTextView;
        TextView dateTextView;
        TextView briefTextView;
    }
}