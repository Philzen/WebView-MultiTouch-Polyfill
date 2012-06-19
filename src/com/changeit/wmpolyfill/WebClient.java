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

	public static final String VERSION = "0.2beta";

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
		if (Build.VERSION.SDK_INT <= 10) {
			movedBuffer = new StringBuilder();
			injectPolyfillJs(view);
			view.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View arg0, MotionEvent arg1) {
					WebView view = (WebView) arg0;
					checkMoved(view, arg1);

					if (polyfillAlltouches || arg1.getPointerCount() > maxNativeTouches ) {
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
		String wmpJs = "(function(){var h=window,f=[],g=null,a=false,d=function(j){this.clientX=j.clientX;this.clientY=j.clientY;this.pageX=j.pageX;this.pageY=j.pageY;this.screenX=j.screenX;this.screenY=j.screenY;if(j.identifier){this.identifier=j.identifier}else{this.identifier=0}if(j.target){this.target=j.target}else{this.target=h.document.elementFromPoint(this.pageX,this.pageY)}};function c(m){var j=m.length,k;for(k=0;k<j;k++){this[k]=m[k]}this.length=j;function l(o){self.item=function n(){return this.item.id};return this[o]}}c.prototype.item=function e(j){return this[j]};var b={currentTouch:null,knowsTouchAPI:null,mapPolyfillToTouch:{down:\"touchstart\",move:\"touchmove\",up:\"touchend\",cancel:\"touchcancel\"},checkTouchDevice:function(){try{var j=document.createEvent(\"TouchEvent\");return(typeof j.initTouchEvent===\"function\"&&typeof h.document.createTouchList===\"function\")}catch(k){return false}},checkMouseDevice:function(){try{document.createEvent(\"MouseEvent\");return true}catch(j){return false}},polyfill:function(k){var j=b._getTouchesFromPolyfillData(k);g=j[0];for(action in k){if(action==\"move\"){for(i in j){b._updateTouchMap(j[i])}}else{if(action==\"down\"){b._updateTouchMap(g)}else{if(action==\"up\"||action==\"cancel\"){b._removeFromTouchMap(g)}}}}b._raiseTouch(g,b.mapPolyfillToTouch[action]);return true},nativeTouchListener:function(j){if(a){a=false;return}g=b._getTouchFromEvent(j.changedTouches[0]);if(j.type==\"touchmove\"||j.type==\"touchstart\"){b._updateTouchMap(g)}else{if(j.type==\"touchend\"||j.type==\"touchcancel\"){b._removeFromTouchMap(g)}}},_raiseTouch:function(m,k){var j=m,l=this.getCleanedTouchMap(k);if(true==false){j=h.document.createEvent(\"TouchEvent\");j.initTouchEvent(this._callCreateTouchList(l),this._callCreateTouchList(this.extractTargetTouches(l,m.target)),this._callCreateTouchList([g]),k,h,m.screenX,m.screenY,m.clientX,m.clientY,false,false,false,false);console.log(j.touches)}else{j=h.document.createEvent(\"Event\");j.pageX=m.pageX;j.pageY=m.pageY;j.initEvent(k,true,true,h,0);j.changedTouches=new c([g]);j.touches=new c(l);j.targetTouches=new c(this.extractTargetTouches(l,m.target));j.target=m.target;j.identifier=(m.identifier?m.identifier:0);this._fillUpEventData(j);j.altKey=false;j.ctrlKey=false;j.metaKey=false;j.shiftKey=false}el=m.target;if(el){el=h.document.elementFromPoint(m.clientX,m.clientY)}a=true;if(el){el.dispatchEvent(j)}else{document.dispatchEvent(j)}},_getTouchesFromPolyfillData:function(n){var j=[],m=function(){return{identifier:undefined,pageX:undefined,pageY:undefined}},l,k;for(action in n){if(action==\"move\"){for(l=0;l<n[action].length;l++){for(touchId in n[action][l]){k=m();k.identifier=parseInt(touchId);k.pageX=n[action][l][touchId][0];k.pageY=n[action][l][touchId][1];this._fillUpEventData(k);j.push(b._getTouchFromEvent(k))}}}else{k=m();if(action==\"down\"){for(touchId in n[action]){k.identifier=parseInt(touchId);k.pageX=n[action][touchId][0];k.pageY=n[action][touchId][1]}}else{if(action==\"up\"||action==\"cancel\"){k.identifier=parseInt(n[action]);k.pageX=g.pageX;k.pageY=g.pageY}}this._fillUpEventData(k);j.push(b._getTouchFromEvent(k))}}return j},_fillUpEventData:function(j){if(!j.target){j.target=h.document.elementFromPoint(j.pageX,j.pageY)}j.screenX=j.pageX;j.screenY=j.pageY;j.clientX=j.pageX;j.clientY=j.pageY;return j},_getTouchFromEvent:function(j){if(this.knowsTouchAPI){return h.document.createTouch(h,j.target,(j.identifier?j.identifier:0),j.pageX,j.pageY,j.screenX,j.screenY)}else{return new d(j)}},getTouchList:function(j){if(this.knowsTouchAPI){return this._callCreateTouchList(cleanedArray)}return new c(j)},getCleanedTouchMap:function(k){var l,m,j=[];for(l=0;l<f.length;l++){if(f[l]){j.push(f[l])}}return j},_updateTouchMap:function(j){f[j.identifier]=j},_removeFromTouchMap:function(j){delete f[j.identifier]},_callCreateTouchList:function(j){debug(\"createTouchList \"+j.length);switch(j.length){case 1:return h.document.createTouchList(j[0]);case 2:return h.document.createTouchList(j[0],j[1]);case 3:return h.document.createTouchList(j[0],j[1],j[2]);case 4:return h.document.createTouchList(j[0],j[1],j[2],j[3]);case 5:return h.document.createTouchList(j[0],j[1],j[2],j[3],j[4]);default:return h.document.createTouchList()}},extractTargetTouches:function(m,j){var k,n,l=[];for(k=0;k<f.length;k++){if((n=f[k])&&n.target==j){l.push(n)}}return l}};b.knowsTouchAPI=b.checkTouchDevice();h.document.addEventListener(\"touchstart\",b.nativeTouchListener,true);h.document.addEventListener(\"touchend\",b.nativeTouchListener,true);h.document.addEventListener(\"touchcancel\",b.nativeTouchListener,true);h.document.addEventListener(\"touchmove\",b.nativeTouchListener,true);h.WMP={polyfill:b.polyfill,Version:\"0.2beta\"}})();";
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
