package com.changeit.dtpolyfill;

import android.app.Activity;
import android.webkit.WebView;
import android.os.Bundle;
import com.changeit.dtpolyfill.WebClient;

public class MainActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		WebView webview = (WebView) this.findViewById(R.id.webView);
//		webview.getSettings().setSupportZoom(true);

		webview.setWebViewClient(new WebClient());
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setBuiltInZoomControls(true);
		webview.loadUrl("file:///android_asset/www/index.html");
    }
}
