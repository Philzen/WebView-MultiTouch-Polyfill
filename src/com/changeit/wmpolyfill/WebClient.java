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

		String deviceInfo = Build.MODEL + " (" + Build.DEVICE +  ", " + Build.PRODUCT + ")";
		String androidVersion = "Android "+ Build.VERSION.RELEASE +" (API Level " + Build.VERSION.SDK + ")";
		view.loadUrl("javascript: tellInjectionWorking('" + deviceInfo +"', '"+ androidVersion +"');");

		if (Build.VERSION.SDK_INT <= 10) {
			movedBuffer = new StringBuilder();
			injectPolyfillJs(view);
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


					if (polyfillAlltouches || arg1.getPointerCount() > maxNativeTouches ) {
						/* Tracking each and every move would be total javascript runtime overkill,
						* therefore only changes by at least one pixel will be tracked
						*/
						if (arg1.getAction() != MotionEvent.ACTION_MOVE ||  movedBuffer.length() > 0) {
							String EventJSON = getEvent(arg1);
							view.loadUrl("javascript: wmp.polyfill(" + EventJSON + ");");
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
		view.loadUrl("javascript: (function(){function e(a){function d(a){self.item=function(){return this.item.id};return this[a]}var b=a.length;for(var c=0;c<b;c++)this[c]=a[c];this.length=b}var a=window,b=[],c=null;justRaisedAnEvent=false;var d=function(a){this.clientX=a.clientX;this.clientY=a.clientY;this.pageX=a.pageX;this.pageY=a.pageY;this.screenX=a.screenX;this.screenY=a.screenY;if(a.identifier)this.identifier=a.identifier;else this.identifier=0;this.target=a.target};e.prototype.item=function(b){return this[b]};var f={currentTouch:null,knowsTouchAPI:null,mapPolyfillToTouch:{down:'touchstart',move:'touchmove',up:'touchend',cancel:'touchcancel'},checkTouchDevice:function(){try{var b=document.createEvent('TouchEvent');return b.initTouchEvent&&a.document.createTouchList}catch(c){return false}},checkMouseDevice:function(){try{document.createEvent('MouseEvent');return true}catch(a){return false}},polyfill:function(a){var b=f._getTouchesFromPolyfillData(a);c=b[0];for(action in a){if(action=='move'){for(i in b)f._updateTouchMap(b[i])}else{if(action=='down')f._updateTouchMap(c);else if(action=='up'||action=='cancel')f._removeFromTouchMap(c)}}f._raiseTouch(c,f.mapPolyfillToTouch[action]);return true},nativeTouchListener:function(a){if(justRaisedAnEvent)return justRaisedAnEvent=false;c=f._getTouchFromEvent(a);if(a.type=='touchmove'||a.type=='touchstart'){f._updateTouchMap(c)}else if(a.type=='touchend'||a.type=='touchcancel'){f._removeFromTouchMap(c)}},_raiseTouch:function(b,d){var f=b;var g=this.getCleanedTouchMap();if(!debug)alert(g.length);if(true==false){f=a.document.createEvent('TouchEvent');f.initTouchEvent(this._callCreateTouchList(g),this._callCreateTouchList(this.extractTargetTouches(g,b.target)),this._callCreateTouchList([c]),d,a,b.screenX,b.screenY,b.clientX,b.clientY,false,false,false,false);console.log(f.touches)}else{f=a.document.createEvent('Event');f.pageX=b.pageX;f.pageY=b.pageY;f.initEvent(d,true,true,a,0);f.changedTouches=new e([c]);f.touches=new e(g);f.targetTouches=new e(this.extractTargetTouches(g,b.target));f.target=b.target;f.identifier=b.identifier?b.identifier:0;this._fillUpEventData(f);f.altKey=false;f.ctrlKey=false;f.metaKey=false;f.shiftKey=false}el=b.target;if(el==undefined)el=a.document.elementFromPoint(b.clientX,b.clientY);justRaisedAnEvent=true;if(el!=undefined)el.dispatchEvent(f);else document.dispatchEvent(f)},_getTouchesFromPolyfillData:function(a){var b=[];var d=function(){return{identifier:undefined,pageX:undefined,pageY:undefined}};var e;for(action in a){if(action=='move'){for(var g=0;g<a[action].length;g++){for(touchId in a[action][g]){e=d();e.identifier=parseInt(touchId);e.pageX=a[action][g][touchId][0];e.pageY=a[action][g][touchId][1];this._fillUpEventData(e);b.push(f._getTouchFromEvent(e))}}}else{e=d();if(action=='down'){for(touchId in a[action]){e.identifier=parseInt(touchId);e.pageX=a[action][touchId][0];e.pageY=a[action][touchId][1]}}else if(action=='up'||action=='cancel'){e.identifier=parseInt(a[action]);e.pageX=c.pageX;e.pageY=c.pageY;console.log(e.identifier)}this._fillUpEventData(e);b.push(f._getTouchFromEvent(e))}}return b},_fillUpEventData:function(b){if(!b.target)b.target=a.document.elementFromPoint(b.pageX,b.pageY);b.screenX=b.pageX;b.screenY=b.pageY;b.clientX=b.pageX;b.clientY=b.pageY;return b},_getTouchFromEvent:function(b){if(this.knowsTouchAPI){return a.document.createTouch(a,b.target,b.identifier?b.identifier:0,b.pageX,b.pageY,b.screenX,b.screenY)}else return new d(b)},getTouchList:function(a){if(this.knowsTouchAPI)return this._callCreateTouchList(cleanedArray);return new e(a)},getCleanedTouchMap:function(){var a=[c];for(var d=0;d<b.length;d++){if(b[d]!=undefined&&b[d].identifier!=c.identifier)a.push(b[d])}return a},_updateTouchMap:function(a){b[a.identifier]=a},_removeFromTouchMap:function(a){delete b[a.identifier]},_callCreateTouchList:function(b){debug('createTouchList '+b.length);switch(b.length){case 1:return a.document.createTouchList(b[0]);case 2:return a.document.createTouchList(b[0],b[1]);case 3:return a.document.createTouchList(b[0],b[1],b[2]);case 4:return a.document.createTouchList(b[0],b[1],b[2],b[3]);case 5:return a.document.createTouchList(b[0],b[1],b[2],b[3],b[4]);default:return a.document.createTouchList()}},extractTargetTouches:function(a,c){var d;var e=[];for(var f=0;f<b.length;f++){if((d=b[f])&&d.target==c){e.push(d)}}return e}};f.knowsTouchAPI=f.checkTouchDevice();a.document.addEventListener('touchstart',f.nativeTouchListener,true);a.document.addEventListener('touchend',f.nativeTouchListener,true);a.document.addEventListener('touchcancel',f.nativeTouchListener,true);a.document.addEventListener('touchmove',f.nativeTouchListener,true);a.wmp=f;a.wmp.prototype={Touch:d,TouchList:e}})()");
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
