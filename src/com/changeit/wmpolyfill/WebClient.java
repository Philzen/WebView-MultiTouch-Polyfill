/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.changeit.wmpolyfill;

import android.os.Build;
import android.util.Log;
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

	public static final String VERSION = "0.3";

	/** a compressed version of the javascript that will be injected
	 * and then called in order to generate our polyfill touches */
	public static final String _WMPJS_ = "(function(){function h(a){var e=a.length,c;for(c=0;c<e;c++)this[c]=a[c];this.length=e}var b=window,g=[],f=null,k=!0,l=function(a){this.clientX=a.clientX;this.clientY=a.clientY;this.pageX=a.pageX;this.pageY=a.pageY;this.screenX=a.screenX;this.screenY=a.screenY;this.identifier=a.identifier?a.identifier:0;this.target=a.target?a.target:b.document.elementFromPoint(this.pageX,this.pageY)};h.prototype.item=function(a){return this[a]};var d={currentTouch:null,knowsTouchAPI:null,mapPolyfillToTouch:{down:\"touchstart\", move:\"touchmove\",up:\"touchend\",cancel:\"touchcancel\"},checkTouchDevice:function(){try{return\"function\"===typeof document.createEvent(\"TouchEvent\").initTouchEvent&&\"function\"===typeof b.document.createTouchList}catch(a){return!1}},checkMouseDevice:function(){try{return document.createEvent(\"MouseEvent\"),!0}catch(a){return!1}},polyfill:function(a){var e=d._getTouchesFromPolyfillData(a);f=e[0];for(action in a)if(\"move\"==action)for(i in e)d._updateTouchMap(e[i]);else\"down\"==action?d._updateTouchMap(f): (\"up\"==action||\"cancel\"==action)&&d._removeFromTouchMap(f);d._raiseTouch(f,d.mapPolyfillToTouch[action]);return!0},nativeTouchListener:function(a){a.isPolyfilled||(f=d._getTouchFromEvent(a.changedTouches[0]),\"touchmove\"==a.type||\"touchstart\"==a.type?d._updateTouchMap(f):(\"touchend\"==a.type||\"touchcancel\"==a.type)&&d._removeFromTouchMap(f))},_raiseTouch:function(a,e){var c=a,j=a.target,d=this.getCleanedTouchMap(e),c=b.document.createEvent(\"Event\");c.initEvent(e,!0,!0,document.body,0);c.changedTouches= new h([f]);c.touches=new h(d);c.targetTouches=new h(this.getTargetTouches(a.target));this._fillUpEventData(c);c.altKey=!1;c.ctrlKey=!1;c.metaKey=!1;c.shiftKey=!1;c.isPolyfilled=!0;j||(j=b.document.elementFromPoint(a.clientX,a.clientY));j?j.dispatchEvent(c):document.dispatchEvent(c)},_getTouchesFromPolyfillData:function(a){var e=[],c,b;for(action in a)if(\"move\"==action)for(c=0;c<a[action].length;c++)for(touchId in a[action][c])b={identifier:parseInt(touchId),clientX:a[action][c][touchId][0]/window.devicePixelRatio, clientY:a[action][c][touchId][1]/window.devicePixelRatio},this._fillUpEventData(b),e.push(d._getTouchFromEvent(b));else{b={};if(\"down\"==action)for(touchId in a[action])b.identifier=parseInt(touchId),b.clientX=a[action][touchId][0]/window.devicePixelRatio,b.clientY=a[action][touchId][1]/window.devicePixelRatio;else if(\"up\"==action||\"cancel\"==action)b.identifier=parseInt(a[action]),b.clientX=f.clientX/window.devicePixelRatio,b.clientY=f.clientY/window.devicePixelRatio;this._fillUpEventData(b);e.push(d._getTouchFromEvent(b))}return e}, _fillUpEventData:function(a){a.target=g[a.identifier]?g[a.identifier].target:b.document.elementFromPoint(a.clientX,a.clientY);a.screenX=a.clientX;a.screenY=a.clientY;a.pageX=a.clientX+b.pageXOffset;a.pageY=a.clientY+b.pageYOffset;return a},_getTouchFromEvent:function(a){return this.knowsTouchAPI?b.document.createTouch(b,a.target,a.identifier?a.identifier:0,a.pageX,a.pageY,a.screenX,a.screenY):new l(a)},getTouchList:function(a){return this.knowsTouchAPI?this._callCreateTouchList(cleanedArray):new h(a)}, getCleanedTouchMap:function(){var a,b=[];for(a=0;a<g.length;a++)g[a]&&b.push(g[a]);return b},_updateTouchMap:function(a){g[a.identifier]=a},_removeFromTouchMap:function(a){delete g[a.identifier]},_callCreateTouchList:function(a){switch(a.length){case 1:return b.document.createTouchList(a[0]);case 2:return b.document.createTouchList(a[0],a[1]);case 3:return b.document.createTouchList(a[0],a[1],a[2]);case 4:return b.document.createTouchList(a[0],a[1],a[2],a[3]);case 5:return b.document.createTouchList(a[0], a[1],a[2],a[3],a[4]);default:return b.document.createTouchList()}},getTargetTouches:function(a){var b,c,d=[];for(b=0;b<g.length;b++)(c=g[b])&&c.target==a&&d.push(c);return d},registerNativeTouchListener:function(a){var e=a&&!k?\"removeEventListener\":!a&&k?\"addEventListener\":!1;e&&(b.document[e](\"touchstart\",d.nativeTouchListener,!0),b.document[e](\"touchend\",d.nativeTouchListener,!0),b.document[e](\"touchcancel\",d.nativeTouchListener,!0),b.document[e](\"touchmove\",d.nativeTouchListener,!0));k=a}};d.knowsTouchAPI= d.checkTouchDevice();b.WMP={polyfill:d.polyfill,setPolyfillAllTouches:d.registerNativeTouchListener,Version:\"0.3\"}})();";

	/** @see setPolyFillAllTouches() */
	protected Boolean polyfillAllTouches = false;

	/** obsolete? */
	//protected int moveThreshold = 1;

	/** The number of touches already working out-of-the-box (we'll assume at least one for all devices) */
	protected int maxNativeTouches = 1;

	/** A copy of the last Motion Event */
	private MotionEvent lastMotionEvent = null;

	/** True after our fundamendal Object Variables have been defined */
	private boolean isInitialised = false;

	/** True after injectWMPJs() was called */
	private boolean isJsInjected = false;

	/** A String to store only the current changed event info  **/
	private StringBuilder moveBuffer;

	/** Maintains a reference to the webview object for coding convenience reasons */
	private WebView view;

	/**
	 * Constructor
	 * Enables Javascript2Java an vice versa.
	 * Provides a javascript Object "wmpjs".
	 * @param view
	 */
	public WebClient(WebView view){
		super();
		if (Build.VERSION.SDK_INT <= 10) {
			this.view = view;
			this.view.getSettings().setJavaScriptEnabled(true);
			this.view.addJavascriptInterface(this.new jsInterface(), "wmpjs");
			this.view.setWebViewClient(this);
		}
		moveBuffer = new StringBuilder();
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
//		android.util.Log.v("console", "OVERRIDEURLLOADING to " + url);
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
			//this.view = view;
			injectWMPJs();
			view.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View arg0, MotionEvent arg1) {
					WebView view = (WebView) arg0;
					if (polyfillAllTouches || arg1.getPointerCount() > maxNativeTouches || arg1.getPointerId( arg1.getActionIndex() ) + 1 > maxNativeTouches ) {
						updateMoveBuffer(view, arg1);
						/* Tracking each and every move would be total javascript runtime overkill,
						* therefore only changes by at least one pixel will be tracked
						*/
						if (moveBuffer.length() > 0 || arg1.getAction() != MotionEvent.ACTION_MOVE) {
							String EventJSON = getEvent(arg1);
							view.loadUrl("javascript: WMP.polyfill(" + EventJSON + ");");

//							android.util.Log.d("debug-console", EventJSON);
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
	 * Update this.moveBuffer with any new touches of concern
	 *
	 * @return whether a new action has been added to the moveBuffer
	 */
	private boolean updateMoveBuffer(WebView view, MotionEvent event) {
		int actionCode = event.getAction() & MotionEvent.ACTION_MASK;
		if (actionCode == MotionEvent.ACTION_MOVE ) {
			moveBuffer.setLength(0);
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
			if (moveBuffer.length() > 0) {
				lastMotionEvent = MotionEvent.obtain(event);
				return true;
			}
		} else if (actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_POINTER_DOWN) {
			moveBuffer.setLength(0);
			addMoveToBuffer(event, event.getActionIndex());
			lastMotionEvent = MotionEvent.obtain(event);
		}
		return false;
	}

	/**
	 * Append a JSON representation of a pointers' position to this.moveBuffer
	 *
	 * @param event A motion Event
	 * @param pointerIndex The index of the pointer in the collection that we want to extract
	 */
	private void addMoveToBuffer(MotionEvent event, int pointerIndex) {

		if (moveBuffer.length() > 0) {
			moveBuffer.append(",");
		}

		StringBuilder sb = new StringBuilder();
			sb.append("{").append(event.getPointerId(pointerIndex))
					.append(":[")
					.append((int)event.getX(pointerIndex)).append(",")
					.append((int) event.getY(pointerIndex))
					.append("]")
					.append("}");
		moveBuffer.append(sb.toString());
	}

	/**
	 * Append a JSON representation of all pointer positions to this.moveBuffer
	 *
	 * @param event A motion Event
	 */
	private void addAllMovesToBuffer(MotionEvent event) {

		for (int i=0; i < event.getPointerCount(); i++) {
			addMoveToBuffer(event, i);
		}
	}

	private String getEvent(MotionEvent event) {

		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		//sb.append("code").append( actionCode );
		if (actionCode == MotionEvent.ACTION_MOVE) {
			sb.append("{move:[").append(moveBuffer).append("]}");
		} else if (actionCode == MotionEvent.ACTION_POINTER_DOWN
			|| actionCode == MotionEvent.ACTION_DOWN) {
			sb.append("{down:").append(moveBuffer).append("}");
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
		Log.d ("wmp.console","Setting polyfill");
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
	 * This nested class provides getter and setter for the javascript Interface to WebClient.
	 * Issue: https://github.com/Philzen/WebView-MultiTouch-Polyfill/issues/1
	 * Fell free to implement wanted Bridge functionality.
	 *
	 * @author fastr
	 *
	 */
	class jsInterface{
		/** Version **/
		public String getVersion(){
			return WebClient.VERSION;
		}
		/** PolyfillAllTouches **/
		public void setPolyfillAllTouches(boolean value){
			WebClient.this.setPolyfillAllTouches(value);
		}
		public boolean getPolyfillAllTouches(){
			return WebClient.this.getPolyfillAllTouches();
		}

		/** PolyfillAllTouches **/
		public void setMaxNativeTouches(int value){
			if (value > 0){
				WebClient.this.maxNativeTouches = value;
			}
		}
		public int getMaxNativeTouches(){
			return WebClient.this.maxNativeTouches;
		}

		/** isJsInjected **/
		public boolean isJsInjected(){
			return WebClient.this.isJsInjected;
		}

		/** return JSON String of the current configuration.
		 *  You have to JSON.parse it on javascript-side
		 */
		public String getConfig(){
			String str =
					"{" +
					"\"VERSION\":"				+"\""+WebClient.VERSION+ "\""+		"," +
					"\"polyfillAllTouches\":" 	+WebClient.this.polyfillAllTouches+ "," +
					"\"maxNativeTouches\":" 	+WebClient.this.maxNativeTouches+ 	"," +
					"\"isJsInjected\":" 		+WebClient.this.isJsInjected+
					"}";
			return str;
		}
	}
}
