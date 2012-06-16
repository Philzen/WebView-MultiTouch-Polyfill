(function(){
	var win = window,
		touched = false,
		touchCount = 0,
		currentTouches = [],
		currentTouch = null;


	/**
	 * @constructor Construct Touch Object from Mouse- (or compatible) Event
	 * @param {Event} e The Event that was fired
	 * @param {number} id See property identifier
	 *
	 * @class Dummy Implementation of the W3C Touch object (Level 1)
	 * @property {EventTarget} target
	 * The Element on which the touch point started when it was first placed on the surface, even if the touch point has since moved outside the interactive area of that element.
	 * @property {number} identifier
	 * An identification number for each touch point. When a touch point becomes active, it must be assigned an identifier that is distinct from any other active touch point. While the touch point remains active, all events that refer to it must assign it the same identifier.
	 * @property {number} screenX
	 * The horizontal coordinate of point relative to the screen in pixels
	 * @property {number} screenY
	 * The vertical coordinate of point relative to the screen in pixels
	 * @property {number} clientX
	 * The horizontal coordinate of point relative to the viewport in pixels, excluding any scroll offset
	 * @property {number} clientY
	 * clientY The vertical coordinate of point relative to the viewport in pixels, excluding any scroll offset
	 * @property {number} pageX
	 * The horizontal coordinate of point relative to the viewport in pixels, including any scroll offset
	 * @property {number} pageY
	 * The vertical coordinate of point relative to the viewport in pixels, including any scroll offset
	 */
	var Touch = function (e, id) {

		this.clientX = e.clientX;
		this.clientY = e.clientY;
		this.pageX = e.pageX;
		this.pageY = e.pageY;
		this.screenX = e.screenX;
		this.screenY = e.screenY;

		// TODO identifier übermitteln
		this.identifier = id;
		this.target = document.elementFromPoint(this.pageX, this.pageY);

		/**
		 * These can be seen in chrome touch emulation, not sure if they will be required for WebView

		this.webkitForce = 1;
		this.webkitRadiusX= 1;
		this.webkitRadiusY= 1
		this.webkitRotationAngle = 0;

		*/
	}

	/**
	 * Dummy Implementation of the W3C TouchList object (Level 1)
	 * @constructor
	 * @class
	 * @param {array} touches an array of all touches that are currently active
	 *
	 * @property {number} length
	 * the number of Touches in the list
	 * @function {Touch} item({number} index)
	 * returns the Touch at the specified index in the list
	 * @function {Touch} item({number} index)
	 * returns the first Touch item in the list whose identifier property matches the specified identifier
	 */
	function TouchList (touches) {
		var touchesLength = touches.length;
		for (var i = 0; i < touchesLength; i++)
			this[i] = touches[i];

		this.length = touchesLength;
		function identifiedTouch (id) {
			self.item = function item() {
				return this.item.id;
			};
			return this[id];
		}
	};

	/**
	 * @param {number} id Index
	 * @return {Touch}
	 */
	TouchList.prototype.item = function item (id){
		return this[id];
	};

	wmp = {
		currentTouch: null,
		knowsTouchAPI: null,
		mapMouseToTouch: {
			mousedown: 'touchstart',
			mousemove: 'touchmove',
			mouseup: 'touchend'
		},
		checkTouchDevice: function(){
			try{
				var evt = document.createEvent("TouchEvent");
				return evt.initTouchEvent;
			}catch(e){
				return false;
			}
		},
		checkMouseDevice: function(){
			try{
				document.createEvent("MouseEvent");
				return true;
			}catch(e){
				return false;
			}
		},
		touchListener: function(e) {
			currentTouch = new Touch(e, 0);
			if (e.type == 'touchmove' && !touched)
				return;
			else if (e.type == 'touchstart') {
				wmp._addTouch( currentTouch );
			}
			else if (e.type == 'touchend' || e.type == 'touchcancel') {
				wmp._removeTouch( currentTouch );
			}
			wmp._raiseTouch(e, e.Type);
		},
		mouseListener: function(e) {
			currentTouch = new Touch(e, 0);

			if (e.type == 'mousemove' && !touched)
				return;
			else if (e.type == 'mousedown') {
				wmp._addTouch( currentTouch );
			}
			else if (e.type == 'mouseup') {
				wmp._removeTouch( currentTouch );
			}

			var eventType = wmp.mapMouseToTouch[e.type];
			wmp._raiseTouch(e, eventType);
		},
		_raiseTouch: function(e, eType) {

			if (this.knowsTouchAPI) {

				var evt = win.document.createEvent('TouchEvent');

				/*
				* TODO Check browser type for non-webkit (initTouchEvent has different order of parameters otherwise)
				* Webkit implementation: https://trac.webkit.org/browser/trunk/WebCore/dom/TouchEvent.idl?rev=52113#L40
				* Safari: http://developer.apple.com/library/safari/documentation/UserExperience/Reference/TouchEventClassReference/TouchEvent/TouchEvent.html#//apple_ref/javascript/instm/TouchEvent/initTouchEvent
				* W3C's latest recommendation has removed this function: http://www.w3.org/TR/touch-events/
				*/
				var targetTouches = win.document.createTouchList( currentTouch );
				console.log(targetTouches);
				evt.initTouchEvent( currentTouches.length > 0 ? win.document.createTouchList( currentTouches ) : win.document.createTouchList(),
					targetTouches, win.document.createTouchList( [evt] ), eType, win,
					e.screenX, e.screenY, e.clientX, e.clientY,
					false, false, false, false);

			} else {
				// following two functions should ideally be TouchEvent, but FF and most desktop Webkit only know UIEvent (which also does the job)
				var evt = win.document.createEvent('UIEvent');
				evt.initUIEvent(eType, true, true, win, 0);

				/** todo polyfill with multi-events */

				evt.touches = new TouchList(currentTouches);
				evt.changedTouches = new TouchList( [ currentTouch ] );
				evt.targetTouches = wmp.getTargetTouches(e.target);
			}


			// attach TouchEvent-Attributes not in UIEvent by default
			evt.altKey = false;
			evt.ctrlKey = false;
			evt.metaKey = false;
			evt.shiftKey = false;


//			evt.preventDefault();
			console.log(evt);

			el = e.target;
			if (el != undefined)
				el = win.document.elementFromPoint(e.clientX, e.clientY);
			if (el != undefined)
				el.dispatchEvent(evt);
			else
				document.dispatchEvent(evt);
		},
		_addTouch: function(touch) {
			touched = true;
			++touchCount;
			currentTouches[touch.identifier] = touch;
		},
		_removeTouch: function(touch) {
			if (touchCount > 0)
				--touchCount;

			if (touched && touchCount === 0)
				touched = false;

			currentTouches.splice(touch.identifier, 1);
		},
		getTargetTouches: function(element) {
			var targetTouches = [];
			for (var i = 0; i < currentTouches.length; i++) {
				var touch = currentTouches[i];
				console.log(touch.target, element);
				if (touch.target == element) {
					targetTouches.push(touch);
				}
			}
			return new TouchList (targetTouches);
		}
	}

	// initialisation
	wmp.knowsTouchAPI = wmp.checkTouchDevice();
	wmp.isMouseDevice = wmp.checkMouseDevice();
	if (wmp.isMouseDevice)
	{
		addEventListener('mousedown', wmp.mouseListener, true);
		addEventListener('mouseup', wmp.mouseListener, true);
		addEventListener('mousemove', wmp.mouseListener, true)
	}

	if (wmp.knowsTouchAPI) {
//
//		win.document.addEventListener('touchstart', wmp.touchListener, true);
//		win.document.addEventListener('touchend', wmp.touchListener, true);
//		win.document.addEventListener('touchcancel', wmp.touchListener, true)
//		win.document.addEventListener('touchmove', wmp.touchListener, true)
	}


	win.wmp = wmp;
	win.wmp.prototype = {
		Touch: Touch,
		TouchList: TouchList
	};

})();


