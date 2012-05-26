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

		WebView webview = new WebView(this);
		webview.setWebViewClient(new WebClient());
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setVerticalScrollBarEnabled(false);
		webview.setHorizontalScrollBarEnabled(false);
		webview.loadUrl("file:///android_asset/www/index.html");

/**
 * Possible test pages to call instead of assets/www/index.html:
 * http://inserthtml.com/demo/mobile-touch/
 */

		setContentView(webview);
    }
}
