/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.changeit.wmpolyfill;

import android.view.MotionEvent;
import android.view.View;
import android.os.Build;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * The idea for this class originates (with many thanks!) from
 * http://stackoverflow.com/questions/2219074/in-android-webview-am-i-able-to-modify-a-webpages-dom
 *
 * @author philzen
 */
public class WebClient extends WebViewClient {

	/**
	 * If TRUE, all touch events will be stopped and replaced by polyfills
	 */
	protected Boolean polyfillAlltouches = false;
	protected int maxNativeTouches = -1;
	protected int maxTouches = -1;

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		view.loadUrl(url);
		return true;
	}

	@Override
	public void onPageFinished(WebView view, String url)
	{
		String deviceInfo = Build.MODEL + " (" + Build.DEVICE +  ", " + Build.PRODUCT + ")";
		String androidVersion = "Android "+ Build.VERSION.RELEASE +" (API Level " + Build.VERSION.SDK + ")";
		view.loadUrl("javascript: tellInjectionWorking('" + deviceInfo +"', '"+ androidVersion +"');");

		view.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View arg0, MotionEvent arg1) {
				WebView view = (WebView) arg0;
				int actionCode = arg1.getAction() & MotionEvent.ACTION_MASK;

				if (actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_POINTER_DOWN) {
					view.loadUrl("javascript: incrementTapCount();");
				} else if (actionCode == MotionEvent.ACTION_UP || actionCode == MotionEvent.ACTION_POINTER_UP) {
					view.loadUrl("javascript: decrementTapCount();");
				}

				if (arg1.getAction() != MotionEvent.ACTION_MOVE) {
					view.loadUrl("javascript: debug('" + dumpEvent(arg1) + "');");
				}

				if (polyfillAlltouches || (maxTouches > 0 && arg1.getPointerCount() > maxNativeTouches) ) {
					return true;
				}

				/**
				 * FALSE : let other handlers do their work (good if we want to test for already working touchevents)
				 * TRUE : stop propagating / bubbling event to other handlers (good if we don't want selection or zoom handlers to happen in webview)
				 */
				return false;
			}
		});
	}

	/**
	 * Taken with a lot of appreciation from
	 * http://www.zdnet.com/blog/burnette/how-to-use-multi-touch-in-android-2-part-3-understanding-touch-events/1775
	 *
	 * @param event
	 * @return String Some information on the MotionEvent
	 */
	private String dumpEvent(MotionEvent event) {

		String names[] = {"DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
			"POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"};
		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;

		if (actionCode == MotionEvent.ACTION_POINTER_DOWN
				|| actionCode == MotionEvent.ACTION_POINTER_UP
				|| actionCode == MotionEvent.ACTION_DOWN
				|| actionCode == MotionEvent.ACTION_UP) {
			sb.append("FINGER ").append(
					(action >> MotionEvent.ACTION_POINTER_ID_SHIFT) + 1);
			sb.append(": ");
		}

		sb.append("ACTION_").append(names[actionCode]);
		sb.append(" [");
		for (int i = 0; i < event.getPointerCount(); i++) {
			sb.append("#").append(i);
			sb.append("(pid_").append(event.getPointerId(i));
			sb.append(")=").append((int) event.getX(i));
			sb.append(",").append((int) event.getY(i));
			if (i + 1 < event.getPointerCount()) {
				sb.append("; ");
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
