package com.example.Codeforces_Progress.Fragment2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.Codeforces_Progress.R;

/**
 * 4 methods
 * overridden methods:
 * {@link #onCreate(Bundle)
 * @link #onBackPressed()}
 * <p>
 * normal methods:
 * {@link #setWebViewAttribute()
 * @link #getScale()}
 */
public class ProblemActivity extends AppCompatActivity {
    private static final int PIC_WIDTH = 900;
    private WebView webView;
    private ProgressBar progressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem);

        webView = findViewById(R.id.webViewId);
        progressBar = findViewById(R.id.progressBarId);
        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });

        setWebViewAttribute();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String url = bundle.getString("tag");
            webView.loadUrl(url);
        }

    }

    private void setWebViewAttribute() {
        // to fit the webpage on device
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setInitialScale(getScale());

        // to add zoom
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
    }

    private int getScale() {
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        Double val = new Double(width) / new Double(PIC_WIDTH);
        val = val * 100d;
        return val.intValue();
    }

    // to go back nearest parent on website
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}