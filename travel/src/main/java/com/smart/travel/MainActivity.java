package com.smart.travel;


import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RADAR_INDEX = 0;
    private static final int TIPS_INDEX = 1;
    private static final int SEARCH_INDEX = 2;
    private static final int INFO_INDEX = 3;

    private FrameLayout fragmentContainer;

    private RadarFragment radarFragment;
    private TipsFragment tipsFragment;
    private SearchFragment searchFragment;
    private InfoFragment infoFragment;

    // click the following button, switch fragment
    private ImageButton radarButton;
    private ImageButton tipsButton;
    private ImageButton searchButton;
    private ImageButton infoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentContainer = (FrameLayout)findViewById(R.id.fragment_container);

        radarButton = (ImageButton)findViewById(R.id.btm_radar_btn);
        tipsButton = (ImageButton)findViewById(R.id.btm_tips_btn);
        searchButton = (ImageButton)findViewById(R.id.btm_search_btn);
        infoButton = (ImageButton) findViewById(R.id.btm_info_btn);

        radarButton.setOnClickListener(this);
        tipsButton.setOnClickListener(this);
        searchButton.setOnClickListener(this);
        infoButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btm_radar_btn:
                setTabSelection(RADAR_INDEX);
                break;
            case R.id.btm_tips_btn:
                setTabSelection(TIPS_INDEX);
                break;
            case R.id.btm_search_btn:
                setTabSelection(SEARCH_INDEX);
                break;
            case R.id.btm_info_btn:
                setTabSelection(INFO_INDEX);
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

    private void hideFragments(FragmentTransaction transaction) {
        if (radarFragment != null) transaction.hide(radarFragment);
        if (tipsFragment != null) transaction.hide(tipsFragment);
        if (searchFragment != null) transaction.hide(searchFragment);
        if (infoFragment != null) transaction.hide(infoFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
