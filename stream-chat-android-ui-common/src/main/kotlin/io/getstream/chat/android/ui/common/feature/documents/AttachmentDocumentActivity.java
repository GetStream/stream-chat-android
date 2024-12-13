package io.getstream.chat.android.ui.common.feature.documents;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.ui.common.R;
import io.getstream.log.StreamLog;
import io.getstream.log.TaggedLogger;

/**
 * An Activity showing attachments such as PDF and Office documents.
 */
public class AttachmentDocumentActivity extends AppCompatActivity {

    private static final String KEY_URL = "url";

    View rootView;
    WebView webView;
    ProgressBar progressBar;

    int reloadCount = 0;
    final int maxReloadCount = 5;

    private final TaggedLogger logger = StreamLog.getLogger("Chat:AttachmentDocumentActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ChatClient.isInitialized()) {
            finish();
            return;
        }

        setContentView(R.layout.stream_activity_attachment_document);
        rootView = findViewById(R.id.rootView);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        setupEdgeToEdge();
        configUIs();
        init();
    }

    private void init() {
        Intent intent = getIntent();
        String filePath = intent.getStringExtra(KEY_URL);
        loadDocument(filePath);
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(rootView, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBarInsets.left, systemBarInsets.top, systemBarInsets.right, systemBarInsets.bottom);
                return WindowInsetsCompat.CONSUMED;
            }
        });
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
     * Encodes the given URL in a format that can be opened in the browser.
     *
     * @param url The URL of the file to load
     * @return String representation of the encoded URL.
     */
    private String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * Load document as url.
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
            logger.e(() -> "The load failed due to an unknown error: " + error);
            if (error == null) {
                return;
            }

            Toast.makeText(AttachmentDocumentActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public static Intent getIntent(Context context, String url) {
        Intent intent = new Intent(context, AttachmentDocumentActivity.class);
        intent.putExtra(KEY_URL, url);
        return intent;
    }
}
