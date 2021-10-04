package com.getstream.sdk.chat.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.logger.TaggedLogger;
import io.getstream.chat.android.ui.common.R;

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

    private String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * Load document as url
     *
     * @param url Document url.
     */
    public void loadDocument(String url) {
        progressBar.setVisibility(View.VISIBLE);
        if (ChatClient.instance().isSocketConnected()) {
            webView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + encodeUrl(url));
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
                    String errorMsg = AttachmentDocumentActivity.this.getString(R.string.stream_ui_message_list_attachment_load_failed);
                    Toast.makeText(AttachmentDocumentActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
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

            Toast.makeText(AttachmentDocumentActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
