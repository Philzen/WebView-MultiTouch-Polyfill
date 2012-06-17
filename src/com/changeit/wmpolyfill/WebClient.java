/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.changeit.wmpolyfill;

import android.graphics.Point;
import android.view.WindowManager;
import android.view.Display;
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
	private StringBuilder movedBuffer;

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


		if (Build.VERSION.SDK_INT <= 10) {
			movedBuffer = new StringBuilder();
			view.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View arg0, MotionEvent arg1) {
					WebView view = (WebView) arg0;
					checkMoved(view, arg1);

					int actionCode = arg1.getAction() & MotionEvent.ACTION_MASK;

					if (actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_POINTER_DOWN) {
						view.loadUrl("javascript: incrementTapCount();");
					} else if (actionCode == MotionEvent.ACTION_UP || actionCode == MotionEvent.ACTION_POINTER_UP) {
						view.loadUrl("javascript: decrementTapCount();");
					}

					/* Tracking each and every move would be total javascript runtime overkill,
					* therefore only changes by at least one pixel will be tracked
					*/
					if (arg1.getAction() != MotionEvent.ACTION_MOVE ||  movedBuffer.length() > 0) {
						String EventJSON = getEvent(arg1);
						view.loadUrl("javascript: debug('" + EventJSON + "');");
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
	}

	/**
	 * Function to check if coordinates between two moves have changed by at least one pixel
	 * @return
	 */
	private boolean checkMoved(WebView view, MotionEvent event) {
		int actionCode = event.getAction() & MotionEvent.ACTION_MASK;
		if (actionCode == MotionEvent.ACTION_UP)
			return false;

		movedBuffer.setLength(0);

		if (lastMotionEvent == null) {
			addAllMovesToBuffer(event);
			lastMotionEvent = MotionEvent.obtain(event);
			return true;
		}

		for (int i = 0; i < event.getPointerCount(); i++)
		{
			if ( (int)lastMotionEvent.getX(i) == (int)event.getX(i)
				&& (int)lastMotionEvent.getY(i) == (int)event.getY(i)
				// Ignore Events outside of viewport
				|| (int)event.getX(i) > view.getWidth()
				|| (int)event.getY(i) > view.getHeight())
				continue;

			addMoveToBuffer(event, i);
		}
		if (movedBuffer.length() > 0) {
			lastMotionEvent = MotionEvent.obtain(event);
			return true;
		}

		return false;
	}

	/**
	 * Add a JSON representation of the pointer information to the JSON Move Buffer
	 * @param event A motion Event
	 * @return
	 */
	private void addMoveToBuffer(MotionEvent event, int pointerIndex) {
		if (movedBuffer.length() > 0) {
			movedBuffer.append(",");
		}

		StringBuilder sb = new StringBuilder();
			sb.append("{").append(event.getPointerId(pointerIndex))
					.append(":[")
					.append((int)event.getX(pointerIndex)).append(",")
					.append((int) event.getY(pointerIndex))
					.append("]")
					.append("}");
		movedBuffer.append(sb.toString());
	}

	private void addAllMovesToBuffer(MotionEvent event) {
		if (movedBuffer.length() > 0) {
			movedBuffer.append(",");
		}
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < event.getPointerCount(); i++) {
				sb.append("{").append(event.getPointerId(i))
						.append(": [")
						.append((int)event.getX(i)).append(", ")
						.append((int) event.getY(i))
						.append("]")
						.append("}");
			if (i+1 < event.getPointerCount())
				sb.append(",");
		}
		movedBuffer.append( sb.toString() );
	}

	private String getEvent(MotionEvent event) {
		String names[] = {"DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
			"POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"};

		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		//sb.append("code").append( actionCode );
		if (actionCode == MotionEvent.ACTION_MOVE) {
			sb.append("{move:[").append(movedBuffer).append("]}");
		} else if (actionCode == MotionEvent.ACTION_POINTER_DOWN
			|| actionCode == MotionEvent.ACTION_DOWN) {
			sb.append("{down:").append(event.getPointerId(event.getActionIndex())).append(movedBuffer).append("}");
		} else if (actionCode == MotionEvent.ACTION_POINTER_UP
			|| actionCode == MotionEvent.ACTION_UP) {
			sb.append("{up:").append(event.getPointerId(event.getActionIndex())).append("}");
		} else if (actionCode == MotionEvent.ACTION_CANCEL) {
			sb.append("{cancel:").append(event.getPointerId(event.getActionIndex()));
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
