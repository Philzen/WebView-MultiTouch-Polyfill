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
	public static final String _WMPJS_ = "(function(){function j(a){var c=a.length,b;for(b=0;b<c;b++)this[b]=a[b];this.length=c}var d=window,h=[],g=null,k=!1,l=!0,m=function(a){this.clientX=a.clientX;this.clientY=a.clientY;this.pageX=a.pageX;this.pageY=a.pageY;this.screenX=a.screenX;this.screenY=a.screenY;this.identifier=a.identifier?a.identifier:0;this.target=a.target?a.target:d.document.elementFromPoint(this.pageX,this.pageY)};j.prototype.item=function(a){return this[a]};var f={currentTouch:null,knowsTouchAPI:null,mapPolyfillToTouch:{down:\"touchstart\", move:\"touchmove\",up:\"touchend\",cancel:\"touchcancel\"},checkTouchDevice:function(){try{return\"function\"===typeof document.createEvent(\"TouchEvent\").initTouchEvent&&\"function\"===typeof d.document.createTouchList}catch(a){return!1}},checkMouseDevice:function(){try{return document.createEvent(\"MouseEvent\"),!0}catch(a){return!1}},polyfill:function(a){var c=f._getTouchesFromPolyfillData(a);g=c[0];for(action in a)if(\"move\"==action)for(i in c)f._updateTouchMap(c[i]);else\"down\"==action?f._updateTouchMap(g): (\"up\"==action||\"cancel\"==action)&&f._removeFromTouchMap(g);f._raiseTouch(g,f.mapPolyfillToTouch[action]);return!0},nativeTouchListener:function(a){k?k=!1:(g=f._getTouchFromEvent(a.changedTouches[0]),\"touchmove\"==a.type||\"touchstart\"==a.type?f._updateTouchMap(g):(\"touchend\"==a.type||\"touchcancel\"==a.type)&&f._removeFromTouchMap(g))},_raiseTouch:function(a,c){var b=a,e=a.target,f=this.getCleanedTouchMap(c),b=d.document.createEvent(\"Event\");b.initEvent(c,!0,!0,document.body,0);b.changedTouches=new j([g]); b.touches=new j(f);b.targetTouches=new j(this.extractTargetTouches(f,a.target));this._fillUpEventData(b);b.altKey=!1;b.ctrlKey=!1;b.metaKey=!1;b.shiftKey=!1;e||(e=d.document.elementFromPoint(a.clientX,a.clientY));k=!0;e?e.dispatchEvent(b):document.dispatchEvent(b)},_getTouchesFromPolyfillData:function(a){var c=[],b,e;for(action in a)if(\"move\"==action)for(b=0;b<a[action].length;b++)for(touchId in a[action][b])e={identifier:parseInt(touchId),clientX:a[action][b][touchId][0],clientY:a[action][b][touchId][1]}, this._fillUpEventData(e),c.push(f._getTouchFromEvent(e));else{e={};if(\"down\"==action)for(touchId in a[action])e.identifier=parseInt(touchId),e.clientX=a[action][touchId][0],e.clientY=a[action][touchId][1];else if(\"up\"==action||\"cancel\"==action)e.identifier=parseInt(a[action]),e.clientX=g.clientX,e.clientY=g.clientY;this._fillUpEventData(e);c.push(f._getTouchFromEvent(e))}return c},_fillUpEventData:function(a){a.target=h[a.identifier]?h[a.identifier].target:d.document.elementFromPoint(a.clientX,a.clientY); a.screenX=a.clientX;a.screenY=a.clientY;a.pageX=a.clientX+d.pageXOffset;a.pageY=a.clientY+d.pageYOffset;return a},_getTouchFromEvent:function(a){return this.knowsTouchAPI?d.document.createTouch(d,a.target,a.identifier?a.identifier:0,a.pageX,a.pageY,a.screenX,a.screenY):new m(a)},getTouchList:function(a){return this.knowsTouchAPI?this._callCreateTouchList(cleanedArray):new j(a)},getCleanedTouchMap:function(){var a,c=[];for(a=0;a<h.length;a++)h[a]&&c.push(h[a]);return c},_updateTouchMap:function(a){h[a.identifier]= a},_removeFromTouchMap:function(a){delete h[a.identifier]},_callCreateTouchList:function(a){switch(a.length){case 1:return d.document.createTouchList(a[0]);case 2:return d.document.createTouchList(a[0],a[1]);case 3:return d.document.createTouchList(a[0],a[1],a[2]);case 4:return d.document.createTouchList(a[0],a[1],a[2],a[3]);case 5:return d.document.createTouchList(a[0],a[1],a[2],a[3],a[4]);default:return d.document.createTouchList()}},extractTargetTouches:function(a,c){var b,e,d=[];for(b=0;b<h.length;b++)(e= h[b])&&e.target==c&&d.push(e);return d},registerNativeTouchListener:function(a){var c=a&&!l?\"removeEventListener\":!a&&l?\"addEventListener\":!1;c&&(d.document[c](\"touchstart\",this.nativeTouchListener,!0),d.document[c](\"touchend\",this.nativeTouchListener,!0),d.document[c](\"touchcancel\",this.nativeTouchListener,!0),d.document[c](\"touchmove\",this.nativeTouchListener,!0));l=a}};f.knowsTouchAPI=f.checkTouchDevice();d.WMP={polyfill:f.polyfill,setPolyfillAllTouches:f.registerNativeTouchListener,Version:\"0.2\"}})();";

	protected Boolean polyfillAllTouches = true;

	protected int moveThreshold = 1;

	/** The number of touches already working out-of-the-box (we'll assume at least one for all devices) */
	protected int maxNativeTouches = 1;

	/** A copy of the last Motion Event */
	private MotionEvent lastMotionEvent = null;

	/** True after injectWMPJs() was called */
	private boolean isJsInjected = false;

	/** A String to store only the current changed event info  **/
	private StringBuilder movedBuffer;
	private WebView view;

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		android.util.Log.v("console", "OVERRIDEURLLOADING to " + url);
		view.loadUrl(url);
		return true;
	}

	@Override
	public void onLoadResource(WebView view, String url) {
//		android.util.Log.v("console", "loadresource_" + url);

		if (url.indexOf(".html") > 0)				// Stop listening to touches when loading a new page,
			view.setOnTouchListener(null);			// as injected functions are not available during load

		isJsInjected = false;
	}

	@Override
	public void onPageFinished(WebView view, String url)
	{
//		android.util.Log.v("console", "pagefinished_" + url);

		if (Build.VERSION.SDK_INT <= 10) {
			this.view = view;
			injectWMPJs();
			movedBuffer = new StringBuilder();
			view.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View arg0, MotionEvent arg1) {
					WebView view = (WebView) arg0;
					checkMoved(view, arg1);

					if (polyfillAllTouches || arg1.getPointerCount() > maxNativeTouches ) {
						/* Tracking each and every move would be total javascript runtime overkill,
						* therefore only changes by at least one pixel will be tracked
						*/
						if (movedBuffer.length() > 0 || arg1.getAction() != MotionEvent.ACTION_MOVE) {
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

	public boolean getPolyfillAllTouches() {
		return polyfillAllTouches;
	}

	/**
	 * @param polyfillAllTouches If set to TRUE, all touch events will be stopped and replaced by polyfills
	 * @return WebClient (Fluent Interface)
	 */
	public WebClient setPolyfillAllTouches(boolean polyfillAllTouches)  {
		this.polyfillAllTouches = polyfillAllTouches;
		if (isJsInjected)
			this.view.loadUrl("javascript:" + getCurrentSettingsInjectionJs());
		return this;
	}

	private void injectWMPJs()
	{
		StringBuilder wmpJs = new StringBuilder();
		wmpJs.append("javascript: if (!window.WMP || WMP.Version != '" )
				.append( WebClient.VERSION)
				.append("')")
				.append(_WMPJS_)
				.append( getCurrentSettingsInjectionJs() );

		view.loadUrl(wmpJs.toString());
//		android.util.Log.v("console", "injecting: WMP-Script (plus: '" + getCurrentSettingsInjectionJs() + "')");
		isJsInjected = true;
	}

	private String getCurrentSettingsInjectionJs() {

		if (polyfillAllTouches != true || isJsInjected) // only needed if not true (default) or if setting was changed after initialisation
		{
			StringBuilder wmpJs = new StringBuilder();
			wmpJs.append("window.WMP.setPolyfillAllTouches( ")
					.append( polyfillAllTouches.toString() )
					.append(" );");
			return wmpJs.toString();
		}

		return "";
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
