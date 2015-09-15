package com.smart.travel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by young on 2015/9/13.
 */
public class AboutActivity extends AppCompatActivity {
    private TextView versionText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String version = getIntent().getStringExtra("version");
        setContentView(R.layout.activity_about);

        versionText = (TextView)findViewById(R.id.about_version);
        versionText.setText(getResources().getString(R.string.app_name) + " :" + version);
    }
}
