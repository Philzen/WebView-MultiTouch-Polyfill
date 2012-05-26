package com.changeit.wmpolyfill;

import android.app.Activity;
import android.webkit.WebView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

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

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


/**
 * Possible test pages to call instead of assets/www/index.html:
 * http://inserthtml.com/demo/mobile-touch/
 */

		setContentView(webview);
    }
}
