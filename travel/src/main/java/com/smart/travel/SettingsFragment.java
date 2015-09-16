package com.smart.travel;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.fb.SyncListener;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Reply;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

import java.io.File;
import java.util.List;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    private TextView versionCode;
    private TextView cacheSize;

    private ImageView imgQrcode;
    private ImageView msgReminder;

    private LinearLayout versionItem;
    private LinearLayout cacheItem;

    private LinearLayout feedbackItem;
    private LinearLayout aboutUsItem;

    private Dialog qrCodeDialog;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.settings_fragment, container, false);
        versionCode = (TextView) content.findViewById(R.id.version_name);
        cacheSize = (TextView) content.findViewById(R.id.cache_size);
        imgQrcode = (ImageView) content.findViewById(R.id.radar_qrcode);
        msgReminder = (ImageView) content.findViewById(R.id.msg_reminder);

        versionItem = (LinearLayout) content.findViewById(R.id.version_item);
        cacheItem = (LinearLayout) content.findViewById(R.id.cache_item);

        feedbackItem = (LinearLayout) content.findViewById(R.id.feedback_item);
        aboutUsItem = (LinearLayout) content.findViewById(R.id.about_item);

        imgQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrCodeDialog = new Dialog(getActivity(), R.style.FullHeightDialog);
                qrCodeDialog.setContentView(R.layout.qrcode_dialog);

                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        qrCodeDialog.dismiss();
                    }
                };

                qrCodeDialog.findViewById(R.id.wx_public_layout).setOnClickListener(listener);

                qrCodeDialog.show();
            }
        });

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
                File cache = ImageLoader.getInstance().getDiskCache().getDirectory();
                String size = formatCacheResult(getFileSize(cache));
                cacheSize.setText(size);
            }
        });

        feedbackItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FeedbackAgent agent = new FeedbackAgent(getActivity());
                agent.startFeedbackActivity();
            }
        });

        aboutUsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                intent.putExtra("version", versionCode.getText());
                startActivity(intent);
            }
        });
    }

    public void onResume() {
        FeedbackAgent agent = new FeedbackAgent(getActivity());
        Conversation conversation = agent.getDefaultConversation();
        conversation.sync(new SyncListener() {
            @Override
            public void onReceiveDevReply(List<Reply> list) {
                if (list.size() > 0) {
                    msgReminder.setVisibility(View.VISIBLE);
                } else {
                    msgReminder.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onSendUserReply(List<Reply> list) {

            }
        });
        super.onResume();
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
        Log.d(TAG, "size:" + size);
        double sizeM = size / (1024 * 1024 * 1.0);
        // Keep 2 decimal
        double result = (double) Math.round(sizeM * 100) / 100;
        return result + "M";
    }

    public void checkApkUpdate() {
        UmengUpdateAgent.forceUpdate(getActivity());
    }

    private void cleanCache() {
        ImageLoader.getInstance().clearDiskCache();

    }

}
