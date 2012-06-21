(function(){
	var win = window,
		currentTouches = [],
		currentTouch = null,
		/** will be true if a polyfilled touch event has just been raised, so the listener for native touches will know */
		justRaisedAnEvent = false,

		/**
		* @constructor Construct Touch Object from Mouse- (or compatible) Event
		* @param {Event} e The Event that was fired
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
		Touch = function (e) {
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

			if (e.target)
				this.target = e.target;
			else
				this.target = win.document.elementFromPoint(this.pageX, this.pageY);
			/**
			* These can be seen in chrome touch emulation, further specification required
			* (currently also not required by any application)
			*
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
		var touchesLength = touches.length,
			i;

		for (i = 0; i < touchesLength; i++)
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
				var evt = document.createEvent('TouchEvent');
				return (typeof evt.initTouchEvent === "function" && typeof win.document.createTouchList === "function");
			}catch(e){
				return false;
			}
		},
		checkMouseDevice: function(){
			try{
				document.createEvent('MouseEvent');
				return true;
			}catch(e){
				return false;
			}
		},
		polyfill: function(data){
			var newTouches = wmp._getTouchesFromPolyfillData(data);
			currentTouch = newTouches[0];
			for (action in data) {
				if (action == 'move') {
					for(i in newTouches)
						wmp._updateTouchMap( newTouches[i] );
				} else {
					if (action == 'down')
						wmp._updateTouchMap( currentTouch );
					else if (action == 'up' || action == 'cancel')
						wmp._removeFromTouchMap( currentTouch );
				}
			}
			wmp._raiseTouch (currentTouch, wmp.mapPolyfillToTouch[action]);
			return true;
		},
		nativeTouchListener: function(e) {
			if (justRaisedAnEvent) {
				justRaisedAnEvent = false;
				return;
			}

			// TODO For polyfilling on devices natively supporting more than one touch,
			// implement processing of complete changedTouches array
			currentTouch = wmp._getTouchFromEvent(e.changedTouches[0]);
			if (e.type == 'touchmove' || e.type == 'touchstart') {
				wmp._updateTouchMap( currentTouch );
			}
			else if (e.type == 'touchend' || e.type == 'touchcancel') {
				wmp._removeFromTouchMap( currentTouch );
			}
		},
		_raiseTouch: function(e, eType) {
			var evt = e,
				el = e.target,
				touches = this.getCleanedTouchMap(eType);

			if (true == false) {
//			if (this.knowsTouchAPI) {
// TODO Find reason why TouchLists are empty on phone (works as expected on rekonq, which supports all the native events)

				evt = win.document.createEvent('TouchEvent');

				/*
				* TODO Check browser type for non-webkit (initTouchEvent has different order of parameters otherwise)
				* Webkit implementation: https://trac.webkit.org/browser/trunk/WebCore/dom/TouchEvent.idl?rev=52113#L40
				* Safari: http://developer.apple.com/library/safari/documentation/UserExperience/Reference/TouchEventClassReference/TouchEvent/TouchEvent.html#//apple_ref/javascript/instm/TouchEvent/initTouchEvent
				* W3C's latest recommendation has removed this function: http://www.w3.org/TR/touch-events/
				*/

				/** todo reflect multi-moves on changedtouches (3rd argument) */
				evt.initTouchEvent( this._callCreateTouchList(touches),
					this._callCreateTouchList(this.extractTargetTouches(touches, e.target)),
					this._callCreateTouchList( [currentTouch] ), eType, win,
					e.screenX, e.screenY, e.clientX, e.clientY,
					false, false, false, false);

				console.log(evt.touches);
			} else {
				// following two functions should ideally be TouchEvent, but FF and most desktop Webkit only know UIEvent (which also does the job)
				evt = win.document.createEvent('Event');
				evt.initEvent(eType, true, true, document.body, 0);

				/** todo reflect multi-moves on changedtouches */
				evt.changedTouches = new TouchList( [ currentTouch ] );
				evt.touches = new TouchList(touches);
				evt.targetTouches = new TouchList( this.extractTargetTouches(touches, e.target) );

//				seems unnecessary (not included on first native touch)
//				evt.identifier = (e.identifier ? e.identifier : 0);

				this._fillUpEventData(evt);

				// attach TouchEvent-Attributes not in UIEvent by default
				evt.altKey = false;
				evt.ctrlKey = false;
				evt.metaKey = false;
				evt.shiftKey = false;
			}
//console.log(e, evt);
//		TODO Check which events need preventDefault ("up" is a hot candidate)
//			if (evt.type == 'touchdown')
//				evt.preventDefault();
//debug(print(evt,1));

			if (!el) {
				el = win.document.elementFromPoint(e.clientX, e.clientY);
			}

			justRaisedAnEvent = true;
			if (el)
				el.dispatchEvent(evt);
			else
				document.dispatchEvent(evt);
		},
		_getTouchesFromPolyfillData:function(data) {
			var returnTouches = [],
				i,
				evt;

			for (action in data) {
				if (action == 'move') {
					for (i=0; i < data[action].length; i++) {
						for (touchId in data[action][i]) {
							evt = {
								identifier: parseInt(touchId),
								clientX: data[action][i][touchId][0],
								clientY: data[action][i][touchId][1]
							};
							this._fillUpEventData(evt);
							returnTouches.push( wmp._getTouchFromEvent(evt) );
						}
					}
				}
				else {
					evt = {};
					if (action == 'down') {
						// NOTE: There is always one down event triggered per finger,
						// it seemed impossible in tests to trigger one event with two fingers simultaneously
						for (touchId in data[action]) {
							evt.identifier = parseInt(touchId);
							evt.clientX = data[action][touchId][0];
							evt.clientY = data[action][touchId][1];
						}
					} else if (action == 'up' || action == 'cancel') {
						evt.identifier =  parseInt(data[action]);
						evt.clientX = currentTouch.clientX;
						evt.clientY = currentTouch.clientY;
					}
					this._fillUpEventData(evt);
					returnTouches.push( wmp._getTouchFromEvent(evt) );
				}
			}

			return returnTouches;
		},
		_fillUpEventData: function(evt) {
			if (!currentTouches[evt.identifier])
				evt.target = win.document.elementFromPoint(evt.clientX, evt.clientY);
			else
				evt.target = currentTouches[evt.identifier].target;
			// TODO respect offset, etc... for scrolling pages (needed ?)
			evt.screenX = evt.clientX;
			evt.screenY = evt.clientY;
			evt.pageX = evt.clientX + win.pageXOffset;
			evt.pageY = evt.clientY + win.pageYOffset;
			return evt;
		},
		_getTouchFromEvent:  function(e) {
			if (this.knowsTouchAPI) {
				/** webkit-specific implementation */
				// example call http://code.google.com/p/webkit-mirror/source/browse/LayoutTests/fast/events/touch/script-tests/document-create-touch.js?r=20bf23dc3dbe1b396811a472b8ccd31b460a1bd3&spec=svn20bf23dc3dbe1b396811a472b8ccd31b460a1bd3
				return win.document.createTouch(win, e.target, (e.identifier ? e.identifier : 0), e.pageX, e.pageY, e.screenX, e.screenY);
			} else
				return new Touch(e);
		},
		getTouchList: function(touchesArray) {
			if (this.knowsTouchAPI)
				return this._callCreateTouchList(cleanedArray);

			return new TouchList(touchesArray);
		},
		getCleanedTouchMap: function(eType)
		{
			var i, touch,
				cleanedArray = [];

			for (i=0; i < currentTouches.length; i++) {
				if (currentTouches[i])
					cleanedArray.push(currentTouches[i]);
			}

			return cleanedArray;
		},
		_updateTouchMap: function(touch) {
			currentTouches[touch.identifier] = touch;
		},
		_removeFromTouchMap: function(touch) {
			delete currentTouches[touch.identifier];
		},
		_callCreateTouchList: function(touches) {
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
		 * Extract an array of touches with every point of contact that is touching the surface
		 * AND started on the targetElement (of the current event)
		 * @param {Array} touches An Array of Touch Objects
		 * @param {HTMLElement} targetElement The element to return any existing known touches for
		 */
		extractTargetTouches: function(touches, targetElement) {
			var i,
				touch,
				targetTouches = [];

			for (i = 0; i < currentTouches.length; i++) {
				if ((touch = currentTouches[i]) && touch.target == targetElement) {
					targetTouches.push(touch);
				}
			}

			return targetTouches;
		}
	}

	// initialisation
	wmp.knowsTouchAPI = wmp.checkTouchDevice();
	// Native Events need to be tracked in order to always have a complete map of touches in javascript
	win.document.addEventListener('touchstart', wmp.nativeTouchListener, true);
	win.document.addEventListener('touchend', wmp.nativeTouchListener, true);
	win.document.addEventListener('touchcancel', wmp.nativeTouchListener, true)
	win.document.addEventListener('touchmove', wmp.nativeTouchListener, true)

	win.WMP = {
		polyfill: wmp.polyfill,
		Version: '0.2'
	}

})();
