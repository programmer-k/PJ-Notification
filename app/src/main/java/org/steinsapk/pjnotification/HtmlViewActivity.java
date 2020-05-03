package org.steinsapk.pjnotification;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public class HtmlViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html_view);

        WebView webView = findViewById(R.id.webView);
        webView.clearCache(true);
        webView.loadUrl("http://kvpn.asuscomm.com:8000/PJNotification/README.html");
    }
}
