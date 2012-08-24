/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

package com.changeit.wmpolyfill;

import android.webkit.WebView;
import org.apache.cordova.CordovaChromeClient;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
import org.apache.cordova.DroidGap;
import org.apache.cordova.api.CordovaInterface;

/**
* Phonegap 1.9+ compatible version of the WMP (won't compile before 1.9!)
* To use it, call the constructor of this class like this
* <pre>
* {@code
* PhonegapWebClient wmp = new PhonegapWebClient(this, appView);
* appView.setWebViewClient(wmp);
* }
* </pre>
*	@author philzen
*/
public class CordovaWebClient extends CordovaWebViewClient {

	WebClient wmp;

	/*
	 *
	 * @param cordova
	 * @param view
	 */
	public CordovaWebClient(CordovaInterface cordova, CordovaWebView view) {
		super(cordova, view);

		DroidGap droidGap = (DroidGap)cordova;

		if (view == null) {
// 			android.util.Log.d("polyfill", "CordovaWebClient: Creating View!");
			view = new CordovaWebView(droidGap);
			droidGap.init( view, this, new CordovaChromeClient(cordova) );
		}

		wmp = new WebClient(view);
		view.setWebViewClient(this);
	}


	@Override
	public void onLoadResource(WebView view, String url) {
		super.onLoadResource(view, url);
		wmp.onLoadResource(view, url);
	}

	/**
	 * Event Handler called after page has been loaded.
	 * If the API level is less than 11, then multitouch polyfill javascript is injected into DOM and eventHandler for Touches is registered
	 *
	 * @param view
	 * @param url
	 */
	@Override
	public void onPageFinished(WebView view, String url)
	{
		super.onPageFinished(view, url);
		wmp.onPageFinished(view, url);
	}

	/**
	 * Whether to polyfill all touches registered by the phone (true) or leave the already
	 * working touches on the WebView through (false)
	 *
	 * TODO		if false, WMP doesn't interfere with the first native touch, but still polyfills all others
	 *			as there isn't no current way to detect the number of native touches
	 *			(thus it's currently fixed to one)
	 *			see https://github.com/Philzen/WebView-MultiTouch-Polyfill/issues/9
	 *
	 * @param polyfillAllTouches
	 * @return Fluid Interface
	 */
	public CordovaWebClient setPolyfillAllTouches(boolean polyfillAllTouches) {
		wmp.setPolyfillAllTouches(polyfillAllTouches);
		return this;
	}

}


