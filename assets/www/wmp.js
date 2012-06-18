(function(){
	var win = window,
		touched = false,
		touchCount = 0,
		currentTouches = [],
		currentTouch = null;
		/** will be true if a polyfilled touch event has just been raised, so the listener for native touches will know */
		justRaisedAnEvent = false;

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
	var Touch = function (e) {

		this.clientX = e.clientX;
		this.clientY = e.clientY;
		this.pageX = e.pageX;
		this.pageY = e.pageY;
		this.screenX = e.screenX;
		this.screenY = e.screenY;

		if (e.identifier)
			this.identifier = e.identifier;
		else
			this.identifier = 0;

		this.target = e.target;
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

	var wmp = {
		currentTouch: null,
		knowsTouchAPI: null,
		mapPolyfillToTouch: {
			down: 'touchstart',
			move: 'touchmove',
			up: 'touchend',
			cancel: 'touchcancel'
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
		polyfill: function(data){
			currentTouch = wmp._getTouchFromPolyfillData(data);
			for (action in data) {
				if (action == 'down' || action == 'move')
					wmp._updateTouchMap( currentTouch );
				else if (action == 'up' || action == 'cancel')
					wmp._removeFromTouchMap( currentTouch );
			}
			wmp._raiseTouch (currentTouch, wmp.mapPolyfillToTouch[action]);
		},
		nativeTouchListener: function(e) {
			if (justRaisedAnEvent)
				return justRaisedAnEvent = false;
			console.log('nativeListener', e);

			currentTouch = wmp._getTouchFromEvent(e);
			if (e.type == 'touchmove' || e.type == 'touchstart') {
				wmp._updateTouchMap( currentTouch );
			}
			else if (e.type == 'touchend' || e.type == 'touchcancel') {
				wmp._removeFromTouchMap( currentTouch );
			}
		},
		_raiseTouch: function(e, eType) {
			var evt;
			if (this.knowsTouchAPI) {
				evt = win.document.createEvent('TouchEvent');

				/*
				* TODO Check browser type for non-webkit (initTouchEvent has different order of parameters otherwise)
				* Webkit implementation: https://trac.webkit.org/browser/trunk/WebCore/dom/TouchEvent.idl?rev=52113#L40
				* Safari: http://developer.apple.com/library/safari/documentation/UserExperience/Reference/TouchEventClassReference/TouchEvent/TouchEvent.html#//apple_ref/javascript/instm/TouchEvent/initTouchEvent
				* W3C's latest recommendation has removed this function: http://www.w3.org/TR/touch-events/
				*/

				evt.initTouchEvent( this.getTouches(),
					this.getTargetTouches(e.target), this._createTouchList( [currentTouch] ), eType, win,
					e.screenX, e.screenY, e.clientX, e.clientY,
					false, false, false, false);

			} else {
				// following two functions should ideally be TouchEvent, but FF and most desktop Webkit only know UIEvent (which also does the job)
				evt = win.document.createEvent('UIEvent');
				evt.initUIEvent(eType, true, true, win, 0);

				/** todo polyfill with multi-events */

				evt.touches = new TouchList(currentTouches);
				evt.changedTouches = new TouchList( [ currentTouch ] );
				evt.targetTouches = this.getTargetTouches(e.target);
			}

			// attach TouchEvent-Attributes not in UIEvent by default
			evt.altKey = false;
			evt.ctrlKey = false;
			evt.metaKey = false;
			evt.shiftKey = false;

//			evt.preventDefault();
			el = e.target;
			if (el == undefined)
				el = win.document.elementFromPoint(e.clientX, e.clientY);

			justRaisedAnEvent = true;
			if (el != undefined)
				el.dispatchEvent(evt);
			else
				document.dispatchEvent(evt);
		},
		_getTouchFromPolyfillData:function(data) {
			var evt = {
				identifier: undefined,
				screenX: undefined,
				screenY: undefined
			};

			for (action in data) {
				if (action == 'down') {
					for (touchId in data[action]) {
						evt.identifier = parseInt(touchId);
						evt.pageX = data[action][touchId][0];
						evt.pageY = data[action][touchId][1];
					}
				} else if (action == 'up') {
					console.log(data[action]);
					evt.identifier = data[action];
				}
			}

			// TODO respect offset, etc... for scrolling pages (needed ?)
			evt.screenX = evt.pageX;
			evt.screenY = evt.pageY;
			evt.clientX = evt.pageX;
			evt.clientY = evt.pageY;
			evt.target = win.document.elementFromPoint(evt.pageX, evt.pageY);

			return wmp._getTouchFromEvent(evt);
		},
		_getTouchFromEvent:  function(e) {
			if (this.knowsTouchAPI) {
				/** webkit-specific implementation */
				// example call http://code.google.com/p/webkit-mirror/source/browse/LayoutTests/fast/events/touch/script-tests/document-create-touch.js?r=20bf23dc3dbe1b396811a472b8ccd31b460a1bd3&spec=svn20bf23dc3dbe1b396811a472b8ccd31b460a1bd3
				return win.document.createTouch(win, e.target, (e.identifier ? e.identifier : 0), e.pageX, e.pageY, e.screenX, e.screenY);
			} else
				return new wmp.Touch(e);
		},
		getTouches: function()		{
			if (this.knowsTouchAPI)
				return this._createTouchList(currentTouches);

			return new TouchList(currentTouches);
		},
		_updateTouchMap: function(touch) {
			touched = true;
			currentTouches[touch.identifier] = touch;
			console.log( currentTouches);
		},
		_removeFromTouchMap: function(touch) {
			if (touched && currentTouches.length == 1)
				touched = false;

			currentTouches.splice(touch.identifier, 1);
		},
		_createTouchList: function(touches) {
			/**
				* Very very ugly implementation, required as for some reason .apply() won't work on createTouchList()
				* (at least in WebKit - throws TypeError)
				* TODO replace with smarter currying function
				*/
			switch(touches.length) {
				case 1:
					return win.document.createTouchList(touches[0]);
				case 2:
					return win.document.createTouchList(touches[0], touches[1]);
				case 3:
					return win.document.createTouchList(touches[0], touches[1], touches[2]);
				case 4:
					return win.document.createTouchList(touches[0], touches[1], touches[2], touches[3]);
				case 5:
					return win.document.createTouchList(touches[0], touches[1], touches[2], touches[3], touches[4]);
				default:
					return win.document.createTouchList();
			}
		},
		/**
		 * Get a TouchList for every point of contact that is touching the surface
		 * AND started on the targetElement (of the current event)
		 * @param {HTMLElement} targetElement The element to return any existing known touches for
		 */
		getTargetTouches: function(targetElement) {
			var targetTouches = [];
			for (var i = 0; i < currentTouches.length; i++) {
				var touch;
				if ((touch = currentTouches[i]) && touch.target == targetElement) {
					targetTouches.push(touch);
				}
			}

			if (this.knowsTouchAPI)
				return this._createTouchList(targetTouches);

			return new TouchList (targetTouches);
		}
	}

	// initialisation
	wmp.knowsTouchAPI = wmp.checkTouchDevice();
	// Native Events need to be tracked in order to always have a complete map of touches in javascript
	win.document.addEventListener('touchstart', wmp.nativeTouchListener, true);
	win.document.addEventListener('touchend', wmp.nativeTouchListener, true);
	win.document.addEventListener('touchcancel', wmp.nativeTouchListener, true)
	win.document.addEventListener('touchmove', wmp.nativeTouchListener, true)


	win.wmp = wmp;
	win.wmp.prototype = {
		Touch: Touch,
		TouchList: TouchList
	};

})();


