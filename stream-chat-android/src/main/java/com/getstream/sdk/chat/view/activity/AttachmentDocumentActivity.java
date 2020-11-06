package com.getstream.sdk.chat.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.utils.Utils;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.logger.TaggedLogger;

/**
 * An Activity showing attachments such as PDF and Office documents.
 */
public class AttachmentDocumentActivity extends AppCompatActivity {
    private final static String TAG = AttachmentDocumentActivity.class.getSimpleName();

    WebView webView;
    ProgressBar progressBar;

    int reloadCount = 0;
    final int maxReloadCount = 5;

    private TaggedLogger logger = ChatLogger.Companion.get("AttachmentDocumentActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stream_activity_attachment_document);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        configUIs();
        init();
    }

    private void init() {
        Intent intent = getIntent();
        String filePath = intent.getStringExtra("url");
        loadDocument(filePath);
    }

    private void configUIs() {
        // WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setWebViewClient(new AppWebViewClients());
    }

    /**
     * Load document as url
     *
     * @param url document url
     */
    public void loadDocument(String url) {
        progressBar.setVisibility(View.VISIBLE);

        if (ChatClient.instance().isSocketConnected()) {
            //TODO: llc: add signing
            webView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + url);
        } else {
            finish();
        }
    }

    private class AppWebViewClients extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //TODO: llc: add signing
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (view.getTitle().equals("")) {
                if (reloadCount < maxReloadCount) {
                    view.reload();
                    reloadCount++;
                } else {
                    progressBar.setVisibility(View.GONE);
                    String errorMsg = AttachmentDocumentActivity.this.getString(R.string.stream_attachment_load_failed_unknown);
                    Utils.showMessage(AttachmentDocumentActivity.this, errorMsg);
                }
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            logger.logE("The load failed due to an unknown error: " + error);
            if (error == null) {
                return;
            }

            Utils.showMessage(AttachmentDocumentActivity.this, error.toString());
        }
    }
}
