/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.changeit.wmpolyfill;

import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * The idea for this class originates (with many thanks!) from
 * http://stackoverflow.com/questions/2219074/in-android-webview-am-i-able-to-modify-a-webpages-dom
 *
 * @author philzen
 */
public class WebClient extends WebViewClient {

	public static final String VERSION = "0.2";

	/** If TRUE, all touch events will be stopped and replaced by polyfills	 */
	protected Boolean polyfillAllTouches = true;

	/** The number of touches already working out-of-the-box (we'll assume 1 for all devices) */
	protected int maxNativeTouches = 1;

	/** A copy of the last Motion Event */
	private MotionEvent lastMotionEvent = null;

	/** A String to store only the current changed event info  **/
	private StringBuilder movedBuffer;

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		view.loadUrl(url);
		return true;
	}

	@Override
	public void onPageFinished(WebView view, String url)
	{
		if (Build.VERSION.SDK_INT <= 10) {
			movedBuffer = new StringBuilder();
			injectPolyfillJs(view);
			view.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View arg0, MotionEvent arg1) {
					WebView view = (WebView) arg0;
					checkMoved(view, arg1);

					if (polyfillAllTouches || arg1.getPointerCount() > maxNativeTouches ) {
						/* Tracking each and every move would be total javascript runtime overkill,
						* therefore only changes by at least one pixel will be tracked
						*/
						if (arg1.getAction() != MotionEvent.ACTION_MOVE ||  movedBuffer.length() > 0) {
							String EventJSON = getEvent(arg1);
							view.loadUrl("javascript: WMP.polyfill(" + EventJSON + ");");
//							view.loadUrl("javascript: debug('" + EventJSON + "');");
						}
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

		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		//sb.append("code").append( actionCode );
		if (actionCode == MotionEvent.ACTION_MOVE) {
			sb.append("{move:[").append(movedBuffer).append("]}");
		} else if (actionCode == MotionEvent.ACTION_POINTER_DOWN
			|| actionCode == MotionEvent.ACTION_DOWN) {
			sb.append("{down:").append(movedBuffer).append("}");
		} else if (actionCode == MotionEvent.ACTION_POINTER_UP
			|| actionCode == MotionEvent.ACTION_UP) {
			sb.append("{up:").append(event.getPointerId(event.getActionIndex())).append("}");
		} else if (actionCode == MotionEvent.ACTION_CANCEL) {
			sb.append("{cancel:").append(event.getPointerId(event.getActionIndex()));
		}

		return sb.toString();
	}

	private void injectPolyfillJs(WebView view)
	{
		String wmpJs = "(function(){function h(a){var b=a.length,c;for(c=0;c<b;c++)this[c]=a[c];this.length=b}var b=window,g=[],f=null,j=!1,k=function(a){this.clientX=a.clientX;this.clientY=a.clientY;this.pageX=a.pageX;this.pageY=a.pageY;this.screenX=a.screenX;this.screenY=a.screenY;this.identifier=a.identifier?a.identifier:0;this.target=a.target?a.target:b.document.elementFromPoint(this.pageX,this.pageY)};h.prototype.item=function(a){return this[a]};var d={currentTouch:null,knowsTouchAPI:null,mapPolyfillToTouch:{down:\"touchstart\", move:\"touchmove\",up:\"touchend\",cancel:\"touchcancel\"},checkTouchDevice:function(){try{return\"function\"===typeof document.createEvent(\"TouchEvent\").initTouchEvent&&\"function\"===typeof b.document.createTouchList}catch(a){return!1}},checkMouseDevice:function(){try{return document.createEvent(\"MouseEvent\"),!0}catch(a){return!1}},polyfill:function(a){var b=d._getTouchesFromPolyfillData(a);f=b[0];for(action in a)if(\"move\"==action)for(i in b)d._updateTouchMap(b[i]);else\"down\"==action?d._updateTouchMap(f): (\"up\"==action||\"cancel\"==action)&&d._removeFromTouchMap(f);d._raiseTouch(f,d.mapPolyfillToTouch[action]);return!0},nativeTouchListener:function(a){j?j=!1:(f=d._getTouchFromEvent(a.changedTouches[0]),\"touchmove\"==a.type||\"touchstart\"==a.type?d._updateTouchMap(f):(\"touchend\"==a.type||\"touchcancel\"==a.type)&&d._removeFromTouchMap(f))},_raiseTouch:function(a,d){var c=a,e=a.target,g=this.getCleanedTouchMap(d),c=b.document.createEvent(\"Event\");c.initEvent(d,!0,!0,document.body,0);c.changedTouches=new h([f]); c.touches=new h(g);c.targetTouches=new h(this.extractTargetTouches(g,a.target));this._fillUpEventData(c);c.altKey=!1;c.ctrlKey=!1;c.metaKey=!1;c.shiftKey=!1;e||(e=b.document.elementFromPoint(a.clientX,a.clientY));j=!0;e?e.dispatchEvent(c):document.dispatchEvent(c)},_getTouchesFromPolyfillData:function(a){var b=[],c,e;for(action in a)if(\"move\"==action)for(c=0;c<a[action].length;c++)for(touchId in a[action][c])e={identifier:parseInt(touchId),clientX:a[action][c][touchId][0],clientY:a[action][c][touchId][1]}, this._fillUpEventData(e),b.push(d._getTouchFromEvent(e));else{e={};if(\"down\"==action)for(touchId in a[action])e.identifier=parseInt(touchId),e.clientX=a[action][touchId][0],e.clientY=a[action][touchId][1];else if(\"up\"==action||\"cancel\"==action)e.identifier=parseInt(a[action]),e.clientX=f.clientX,e.clientY=f.clientY;this._fillUpEventData(e);b.push(d._getTouchFromEvent(e))}return b},_fillUpEventData:function(a){a.target=g[a.identifier]?g[a.identifier].target:b.document.elementFromPoint(a.clientX,a.clientY); a.screenX=a.clientX;a.screenY=a.clientY;a.pageX=a.clientX+b.pageXOffset;a.pageY=a.clientY+b.pageYOffset;return a},_getTouchFromEvent:function(a){return this.knowsTouchAPI?b.document.createTouch(b,a.target,a.identifier?a.identifier:0,a.pageX,a.pageY,a.screenX,a.screenY):new k(a)},getTouchList:function(a){return this.knowsTouchAPI?this._callCreateTouchList(cleanedArray):new h(a)},getCleanedTouchMap:function(){var a,b=[];for(a=0;a<g.length;a++)g[a]&&b.push(g[a]);return b},_updateTouchMap:function(a){g[a.identifier]= a},_removeFromTouchMap:function(a){delete g[a.identifier]},_callCreateTouchList:function(a){switch(a.length){case 1:return b.document.createTouchList(a[0]);case 2:return b.document.createTouchList(a[0],a[1]);case 3:return b.document.createTouchList(a[0],a[1],a[2]);case 4:return b.document.createTouchList(a[0],a[1],a[2],a[3]);case 5:return b.document.createTouchList(a[0],a[1],a[2],a[3],a[4]);default:return b.document.createTouchList()}},extractTargetTouches:function(a,b){var c,d,f=[];for(c=0;c<g.length;c++)(d= g[c])&&d.target==b&&f.push(d);return f}};d.knowsTouchAPI=d.checkTouchDevice();b.document.addEventListener(\"touchstart\",d.nativeTouchListener,!0);b.document.addEventListener(\"touchend\",d.nativeTouchListener,!0);b.document.addEventListener(\"touchcancel\",d.nativeTouchListener,!0);b.document.addEventListener(\"touchmove\",d.nativeTouchListener,!0);b.WMP={polyfill:d.polyfill,Version:\"0.2\"}})();";
		view.loadUrl("javascript: if (!window.WMP || WMP.Version != '" + WebClient.VERSION +"' ) " + wmpJs);
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
