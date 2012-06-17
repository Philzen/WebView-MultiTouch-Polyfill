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

	/** If TRUE, all touch events will be stopped and replaced by polyfills	 */
	protected Boolean polyfillAlltouches = false;

	/** The number of touches already working out-of-the-box (-1 = Unknown) */
	protected int maxNativeTouches = -1;

	/** The desired number of touches you'd need in your application (-1 = ALL) */
	protected int maxTouches = -1;

	private MotionEvent lastMotionEvent = null;

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

				String EventJSON = new String(getEvent(arg1));
				if (EventJSON.length() > 0)
//				if (arg1.getAction() != MotionEvent.ACTION_MOVE) {
					view.loadUrl("javascript: debug('" + getEvent(arg1) + "');");
//				}

				lastMotionEvent = MotionEvent.obtain(arg1);

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
	 * Function to check if coordinates between two moves have changed by at least one pixel
	 * @return
	 */
	private boolean hasMoved(MotionEvent event)	{

		/**
		 * In almost all cases the moving finger triggered the motion event
		 * therefore there is no need to loop through all Pointer indexes here (which would be the thorough way)
		 */
		if ( lastMotionEvent == null
			||  (int)lastMotionEvent.getX() != (int)event.getX()
			|| (int)lastMotionEvent.getY() != (int)event.getY() )
			return true;

		return false;
	}

	/**
	 * Return a JSON representation of the pointer information that is intented to be sent to the javascript runtime
	 * @param event A motion Event
	 * @return
	 */
	private String getMoveJSON(MotionEvent event) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < event.getPointerCount(); i++) {
			sb.append("{").append(event.getPointerId(i))
					.append(": [")
					.append((int)event.getX(i)).append(", ")
					.append((int) event.getY(i)).append(", ")
					.append("]")
					.append("}");
			if (i + 1 < event.getPointerCount()) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	private String getEvent(MotionEvent event) {
		String names[] = {"DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
			"POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"};

		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		//sb.append("code").append( actionCode );
		if (actionCode == MotionEvent.ACTION_MOVE) {
			/* Tracking each and every move would be total javascript runtime overkill,
			 * therefore only changes by at least one pixel will be tracked
			 */
			if (hasMoved(event)) {
				sb.append("\"move\" ").append(getMoveJSON(event));
				// At least one touch event has changed
			}
		} else if (actionCode == MotionEvent.ACTION_POINTER_DOWN
			|| actionCode == MotionEvent.ACTION_DOWN) {
			sb.append("\"down\", ").append(event.getPointerId(event.getActionIndex()));
		} else if (actionCode == MotionEvent.ACTION_POINTER_UP
			|| actionCode == MotionEvent.ACTION_UP) {
			sb.append("\"up\", ").append(event.getPointerId(event.getActionIndex()));
		} else if (actionCode == MotionEvent.ACTION_CANCEL) {
			sb.append("\"cancel\", ").append(event.getPointerId(event.getActionIndex()));
		}


//		sb.append("ACTION_").append(names[actionCode]);
//		sb.append(" [");
//		for (int i = 0; i < event.getPointerCount(); i++) {
//			sb.append("#").append(i);
//			sb.append("(pid_").append(event.getPointerId(i));
//			sb.append(")=").append((int) event.getX(i));
//			sb.append(",").append((int) event.getY(i));
//			if (i + 1 < event.getPointerCount()) {
//				sb.append("; ");
//			}
//		}
//		sb.append("]");
		return sb.toString();

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
