/**
 * This JavaScript-Class dispatches events which are 
 * triggerd from the Android-Application TouchListener implementation
 * of this Package.
 * 
 * Author: Fabian Strachanski (fastrde)
 */


//// for Closure Compiler support uncomment next line
goog.provide("fastr.android.multitouch"); 

//// for Closure Compiler comment out next 2 lines 


//fastr = {};
//fastr.android = {};

/**
 * @constructor
 */
fastr.android.multitouch = function(){
    console.log("LOADED");
	this.pointer    = [];
	this.androidRes = [];

	//TODO: set multiplicators from phone-app
	this.xMul = 1.03;	// multiplicator for x coordinates.   
	this.yMul = 1.11; // multiplicator for y coordinates. 
}
window['fastrMTfix'] = new fastr.android.multitouch();


/**
 *  Datamodel to describe a single touch.
 *  @constructor
 *	@param {number} id Unique identifier for the touch
 *  @param {number} screenX X-coord of touch
 *  @param {number} screenY Y-coord of touch
 *  @param {number} pageX X-coord of touch
 *  @param {number} pageY Y-coord of touch
 *  @param {HTMLElement|null} target
 */
fastr.android.multitouch.Touch = function(id,screenX,screenY,pageX,pageY,target){ 
	//var dWidth = window['fastrMTfix'].androidRes[0] / window.innerWidth;
	//var dHeight = window['fastrMTfix'].androidRes[1] / window.innerHeight;

	this.identifier = id;	//long
	this.target 		= target  || null; //EventTarget
    
	this.screenX		= screenX ; //long
	this.screenY		= screenY ; //long
	this.pageX			= pageX   ; //long
	this.pageY			= pageY   ; //long
	this.clientX		= screenX ; //long
	this.clientY		= screenY ; //long

	if (this.target == null) this.target = document.elementFromPoint(this.screenX,this.screenY);
}

/**
 * Sets the resolution for the android display.
 * @param {number} width The width of the device display in pixel.
 * @param {number} height The height of the device display in pixel.
 */ 
fastr.android.multitouch.prototype['setDisplay'] = function(width,height){
	this.androidRes = [width, height];
}

/**
 * Implements the W3C TouchList
 * @constructor
 * @param {Object} data Data to fill in the TouchList
 * @extends Array
 */

fastr.android.multitouch.TouchList = function(data,name){
	Array.call(this);
	data = data || [];
	this._fill(data);
}
fastr.android.multitouch.TouchList.prototype = new Array;
fastr.android.multitouch.TouchList.prototype.constructor = fastr.android.multitouch.TouchList;

/**
 * returns the item at position 'index'
 * @param {number} index
 */
fastr.android.multitouch.TouchList.prototype.item = function(index){
	return this[index];
}
/**
 * searches for the item with the given identifier 
 * @param identifier
 */
fastr.android.multitouch.TouchList.prototype.identifiedTouch = function(identifier){
	for(var i = 0; i < this.length; i++){
		if (this[i].identifier == identifier){
			return this[i];
		}
	}
}
/**
 * fills the array with the given data (internal function)
 * param {Object} data
 */
fastr.android.multitouch.TouchList.prototype._fill = function(data){
	var t;
	for(var i = 0; i < data.length; i++){
		t = data[i];
        //console.log("ttt --- " +t.id + t.x + t.y);
		this.push(new fastr.android.multitouch.Touch(t.id,t.x,t.y,t.x,t.y));
	}
}

/**
 * @constructor
 * @param {string} type the type of the touchevent (eg. touchstart)
 * @param {number} changeid the id of the touch that triggers the event
 * @param {Object} touches the associated touches
 * @return {Object} the generated DOM-Event 
 */
fastr.android.multitouch.TouchEvent = function(type,changedid,touches){
	var evt = document.createEvent("Event");	
	evt.initEvent(type, true, true);
	evt.touches 				= new fastr.android.multitouch.TouchList(touches,"Touches");
	evt.targetTouches           = new fastr.android.multitouch.TouchList(touches,"TargetTouches");
	evt.changedTouches          = new fastr.android.multitouch.TouchList(touches,"changedTouches");
	//evt.changedTouches 	= new fastr.android.multitouch.TouchList([],"changedTouches");
	//evt.changedTouches.push(evt.touches.identifiedTouch(changedid));
	evt.altKey					= false;
	evt.metaKey					= false;
	evt.ctrlKey					= false;
	evt.shiftKey				= false;
	evt._target = evt.touches.identifiedTouch(changedid).target;
	evt._target = evt.changedTouches[0].target;
	evt._send = function(){
		if (this._target){
			for (var i = 0; i< this.changedTouches.length; i++){
                this.changedTouches[i].target.dispatchEvent(this);
            }
		}else{
            document.dispatchEvent(this);
		}
	}
	return evt;
}

/**
 * triggers touchstart event
 * @param {number} id the identifier of the touch
 * @param {number} x x-coord of the touch
 * @param {number} y y-coord of the touch
 * @param {Object} data touchevent information send from the phone
 */
fastr.android.multitouch.prototype['touchstart'] = function(id, x, y, data){
	var evt = new fastr.android.multitouch.TouchEvent("touchstart",id,data);
	evt._send();	
}

/**
 * triggers touchmove event
 * @param {number} id the identifier of the touch
 * @param {number} x x-coord of the touch
 * @param {number} y y-coord of the touch
 * @param {Object} data touchevent information send from the phone
 */
fastr.android.multitouch.prototype['touchmove'] = function(id, x, y, data){
	var evt = new fastr.android.multitouch.TouchEvent("touchmove",id,data);
	evt._send();	
}

/**
 * triggers touchend event
 * @param {number} id the identifier of the touch
 * @param {number} x x-coord of the touch
 * @param {number} y y-coord of the touch
 * @param {Object} data touchevent information send from the phone
 */
fastr.android.multitouch.prototype['touchend'] = function(id, x, y, data){
	var evt = new fastr.android.multitouch.TouchEvent("touchend",id,data);
	evt._send();	
}

