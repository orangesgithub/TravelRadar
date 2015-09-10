package com.smart.travel;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.update.UmengUpdateAgent;

import java.io.File;
import java.text.DecimalFormat;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    private TextView versionCode;
    private TextView cacheSize;

    private LinearLayout versionItem;
    private LinearLayout cacheItem;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.settings_fragment, container, false);
        versionCode = (TextView)content.findViewById(R.id.version_name);
        cacheSize = (TextView)content.findViewById(R.id.cache_size);

        versionItem = (LinearLayout)content.findViewById(R.id.version_item);
        cacheItem = (LinearLayout)content.findViewById(R.id.cache_item);
        return content;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = getActivity();
        PackageManager manager = activity.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(activity.getPackageName(), 0);
            String version = info.versionName;
            versionCode.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        File cache = ImageLoader.getInstance().getDiskCache().getDirectory();
        String size = formatCacheResult(getFileSize(cache));
        cacheSize.setText(size);

        versionItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkApkUpdate();
            }
        });

        cacheItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cleanCache();
            }
        });
    }

    public long getFileSize(File f) {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSize(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }
        return size;
    }

    public String formatCacheResult(long size) {
        Log.e(TAG, "size:" + size);
        double sizeM = size / (1024 * 1024 * 1.0);
        // Keep 2 decimal
        double result = (double)Math.round(sizeM*100)/100;
        return result + "M";
    }

    public void checkApkUpdate() {
        UmengUpdateAgent.forceUpdate(getActivity());
    }

    private void cleanCache() {
        ImageLoader.getInstance().clearDiskCache();
    }
}
