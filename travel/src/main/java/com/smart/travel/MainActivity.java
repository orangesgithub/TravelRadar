package com.smart.travel;


import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RADAR_INDEX = 0;
    private static final int TIPS_INDEX = 1;
    private static final int SEARCH_INDEX = 2;
    private static final int INFO_INDEX = 3;

    private RadarFragment radarFragment;
    private TipsFragment tipsFragment;
    private SearchFragment searchFragment;
    private InfoFragment infoFragment;

    // click the following button, switch fragment
    private ImageButton radarButton;
    private ImageButton tipsButton;
    private ImageButton searchButton;
    private ImageButton infoButton;

    private Button titleRightButton;
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(3).threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
                .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000)).build();
        ImageLoader.getInstance().init(configuration);

        radarButton = (ImageButton)findViewById(R.id.btm_radar_btn);
        tipsButton = (ImageButton)findViewById(R.id.btm_tips_btn);
        searchButton = (ImageButton)findViewById(R.id.btm_search_btn);
        infoButton = (ImageButton) findViewById(R.id.btm_info_btn);

        radarButton.setOnClickListener(this);
        tipsButton.setOnClickListener(this);
        searchButton.setOnClickListener(this);
        infoButton.setOnClickListener(this);
        // show the first page
        setTabSelection(RADAR_INDEX);

        titleRightButton = (Button)findViewById(R.id.title_right_btn);
        titleRightButton.setOnClickListener(radarFragment);

        titleText = (TextView)findViewById(R.id.title_text);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btm_radar_btn:
                setTabSelection(RADAR_INDEX);
                changeTitle(RADAR_INDEX);
                break;
            case R.id.btm_tips_btn:
                setTabSelection(TIPS_INDEX);
                changeTitle(TIPS_INDEX);
                break;
            case R.id.btm_search_btn:
                setTabSelection(SEARCH_INDEX);
                changeTitle(SEARCH_INDEX);
                break;
            case R.id.btm_info_btn:
                setTabSelection(INFO_INDEX);
                changeTitle(INFO_INDEX);
                break;
        }
    }

    private void setTabSelection(int index) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        hideFragments(transaction);
        switch (index) {
            case RADAR_INDEX:
                if(radarFragment == null) {
                    radarFragment = new RadarFragment();
                    transaction.add(R.id.fragment_container, radarFragment);
                } else {
                    transaction.show(radarFragment);
                }
                break;
            case TIPS_INDEX:
                if(tipsFragment == null) {
                    tipsFragment = new TipsFragment();
                    transaction.add(R.id.fragment_container, tipsFragment);
                } else {
                    transaction.show(tipsFragment);
                }
                break;
            case SEARCH_INDEX:
                if(searchFragment == null) {
                    searchFragment = new SearchFragment();
                    transaction.add(R.id.fragment_container, searchFragment);
                } else {
                    transaction.show(searchFragment);
                }
                break;
            case INFO_INDEX:
                if(infoFragment == null) {
                    infoFragment = new InfoFragment();
                    transaction.add(R.id.fragment_container, infoFragment);
                } else {
                    transaction.show(infoFragment);
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
            case TIPS_INDEX:
                titleRightButton.setVisibility(View.INVISIBLE);
                break;
            case SEARCH_INDEX:
                titleRightButton.setVisibility(View.INVISIBLE);
                titleText.setText(R.string.radar_search);
                break;
            case INFO_INDEX:
                titleRightButton.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (radarFragment != null) transaction.hide(radarFragment);
        if (tipsFragment != null) transaction.hide(tipsFragment);
        if (searchFragment != null) transaction.hide(searchFragment);
        if (infoFragment != null) transaction.hide(infoFragment);
    }
}
