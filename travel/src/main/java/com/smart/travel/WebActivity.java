package com.smart.travel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smart.travel.utils.UMSocialHelper;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.UMImage;

public class WebActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private ImageButton shareButton;
    private ImageButton backButton;
    private ImageButton forwardButton;
    private ImageButton refreshButton;

    private static final String TAG = "RadarWebView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        String url = getIntent().getStringExtra("url");
        String title = getIntent().getStringExtra("title");
        String image = getIntent().getStringExtra("image");
        String content = getIntent().getStringExtra("content");

        Log.d(TAG, "url: " + url + " Image:" + image);

        webView = (WebView) findViewById(R.id.radar_webview);
        progressBar = (ProgressBar) findViewById(R.id.radar_webview_pb);

        TextView textTitle = (TextView) findViewById(R.id.title_text);
        textTitle.setText(title);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                backButton.setEnabled(webView.canGoBack());
                forwardButton.setEnabled(webView.canGoForward());
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                backButton.setEnabled(webView.canGoBack());
                forwardButton.setEnabled(webView.canGoForward());
            }
        });
        webView.setWebChromeClient(new CustomWebViewClient());

        webView.loadUrl(url);


        shareButton = (ImageButton) findViewById(R.id.btn_url_share);
        backButton = (ImageButton) findViewById(R.id.btn_url_back);
        forwardButton = (ImageButton) findViewById(R.id.btn_url_forward);
        refreshButton = (ImageButton) findViewById(R.id.btn_url_refresh);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goBack();
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goForward();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

        UMSocialHelper.getInstance().setUp(this);
        UMSocialHelper.getInstance().setShareContent(title, content, url, new UMImage(this, image));
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UMSocialHelper.getInstance().openShare(false);
            }
        });
    }

    private class CustomWebViewClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
            if (newProgress == 100) {
                progressBar.setVisibility(View.GONE);
            } else if (newProgress != 100 && progressBar.getVisibility() == View.GONE) {
                progressBar.setVisibility(View.VISIBLE);
            }
            super.onProgressChanged(view, newProgress);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        UMSocialHelper.getInstance().onActivityResult(requestCode, resultCode, data);
    }
}
