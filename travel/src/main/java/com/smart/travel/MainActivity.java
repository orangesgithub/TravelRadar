package com.smart.travel;


import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final int RADAR_INDEX = 0;
    private static final int ADVICE_INDEX = 1;
    private static final int SEARCH_INDEX = 2;
    private static final int SETTINGS_INDEX = 3;

    private RadarFragment radarFragment;
    private AdviceFragment adviceFragment;
    private SearchFragment searchFragment;
    private SettingsFragment settingsFragment;

    // click the following button, switch fragment
    private ImageButton radarButton;
    private ImageButton adviceButton;
    private ImageButton searchButton;
    private ImageButton settingsButton;
    private TextView radarText;
    private TextView adviceText;
    private TextView searchText;
    private TextView settingsText;

    private int lastSelectionTab = -1;

    private Button titleRightButton;
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(3).threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCache(new UsingFreqLimitedMemoryCache(20 * 1024 * 1024))
                .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000)).build();
        ImageLoader.getInstance().init(configuration);

        radarButton = (ImageButton) findViewById(R.id.btm_radar_btn);
        adviceButton = (ImageButton) findViewById(R.id.btm_tips_btn);
        searchButton = (ImageButton) findViewById(R.id.btm_search_btn);
        settingsButton = (ImageButton) findViewById(R.id.btm_info_btn);
        radarText = (TextView) findViewById(R.id.btm_radar_text);
        adviceText = (TextView) findViewById(R.id.btm_tips_text);
        searchText = (TextView) findViewById(R.id.btm_search_text);
        settingsText = (TextView) findViewById(R.id.btm_info_text);

        LinearLayout radarParent = (LinearLayout) radarButton.getParent();
        radarParent.setOnClickListener(this);
        LinearLayout tipsParent = (LinearLayout) adviceButton.getParent();
        tipsParent.setOnClickListener(this);
        LinearLayout searchParent = (LinearLayout) searchButton.getParent();
        searchParent.setOnClickListener(this);
        LinearLayout infoParent = (LinearLayout) settingsButton.getParent();
        infoParent.setOnClickListener(this);

        radarButton.setOnClickListener(this);
        adviceButton.setOnClickListener(this);
        searchButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        radarText.setOnClickListener(this);
        adviceText.setOnClickListener(this);
        searchText.setOnClickListener(this);
        settingsText.setOnClickListener(this);
        // show the first page
        setTabSelection(RADAR_INDEX);

        titleRightButton = (Button) findViewById(R.id.title_right_btn);
        titleRightButton.setOnClickListener(radarFragment);

        titleText = (TextView) findViewById(R.id.title_text);

        MobclickAgent.updateOnlineConfig(this);
    }

    @Override
    public void onClick(View v) {
        if (v == radarButton || v == radarButton.getParent() || v == radarText) {
            setTabSelection(RADAR_INDEX);
            changeTitle(RADAR_INDEX);
        } else if (v == adviceButton || v == adviceButton.getParent() || v == adviceText) {
            setTabSelection(ADVICE_INDEX);
            changeTitle(ADVICE_INDEX);
        } else if (v == searchButton || v == searchButton.getParent() || v == searchText) {
            setTabSelection(SEARCH_INDEX);
            changeTitle(SEARCH_INDEX);
        } else if (v == settingsButton || v == settingsButton.getParent() || v == settingsText) {
            setTabSelection(SETTINGS_INDEX);
            changeTitle(SETTINGS_INDEX);
        }
    }

    private void setTabSelection(int index) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        if (lastSelectionTab == RADAR_INDEX) {
            radarButton.setImageDrawable(getResources().getDrawable(R.drawable.tab_radar));
        } else if (lastSelectionTab == ADVICE_INDEX) {
            adviceButton.setImageDrawable(getResources().getDrawable(R.drawable.tab_advice));
        } else if (lastSelectionTab == SEARCH_INDEX) {
            searchButton.setImageDrawable(getResources().getDrawable(R.drawable.tab_search));
        } else if (lastSelectionTab == SETTINGS_INDEX) {
            settingsButton.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings));
        }

        if (index == RADAR_INDEX) {
            lastSelectionTab = RADAR_INDEX;
            radarButton.setImageDrawable(getResources().getDrawable(R.drawable.tab_radar_pressed));
        } else if (index == ADVICE_INDEX) {
            lastSelectionTab = ADVICE_INDEX;
            adviceButton.setImageDrawable(getResources().getDrawable(R.drawable.tab_advice_pressed));
        } else if (index == SEARCH_INDEX) {
            lastSelectionTab = SEARCH_INDEX;
            searchButton.setImageDrawable(getResources().getDrawable(R.drawable.tab_search_pressed));
        } else if (index == SETTINGS_INDEX) {
            lastSelectionTab = SETTINGS_INDEX;
            settingsButton.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_pressed));
        }

        hideFragments(transaction);
        switch (index) {
            case RADAR_INDEX:
                if (radarFragment == null) {
                    radarFragment = new RadarFragment();
                    transaction.add(R.id.fragment_container, radarFragment);
                } else {
                    transaction.show(radarFragment);
                }
                break;
            case ADVICE_INDEX:
                if (adviceFragment == null) {
                    adviceFragment = new AdviceFragment();
                    transaction.add(R.id.fragment_container, adviceFragment);
                } else {
                    transaction.show(adviceFragment);
                }
                break;
            case SEARCH_INDEX:
                if (searchFragment == null) {
                    searchFragment = new SearchFragment();
                    transaction.add(R.id.fragment_container, searchFragment);
                } else {
                    transaction.show(searchFragment);
                }
                break;
            case SETTINGS_INDEX:
                if (settingsFragment == null) {
                    settingsFragment = new SettingsFragment();
                    transaction.add(R.id.fragment_container, settingsFragment);
                } else {
                    transaction.show(settingsFragment);
                }
                break;
        }
        transaction.commit();
    }

    private void changeTitle(int index) {
        switch (index) {
            case RADAR_INDEX:
                titleRightButton.setVisibility(View.VISIBLE);
                titleText.setText(R.string.app_name);
                break;
            case ADVICE_INDEX:
                titleRightButton.setVisibility(View.INVISIBLE);
                titleText.setText(R.string.second_title);
                break;
            case SEARCH_INDEX:
                titleRightButton.setVisibility(View.INVISIBLE);
                titleText.setText(R.string.radar_search);
                break;
            case SETTINGS_INDEX:
                titleRightButton.setVisibility(View.INVISIBLE);
                titleText.setText(R.string.fourth_page);
                break;
        }
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (radarFragment != null) transaction.hide(radarFragment);
        if (adviceFragment != null) transaction.hide(adviceFragment);
        if (searchFragment != null) transaction.hide(searchFragment);
        if (settingsFragment != null) transaction.hide(settingsFragment);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
