(function(){
	var win = window;
	var mousePressed = false;

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
		isTouchDevice: true,
		mapMouseToTouch: {
			mousedown: 'touchstart',
			mousemove: 'touchmove',
			mouseup: 'touchend'
		},
		checkTouchDevice: function(){
			try{
				document.createEvent("TouchEvent");
				return true;
			}catch(e){
				return false;
			}
		},
		mouseListener: function(e) {
			if (e.type == 'mousemove' && !mousePressed)
				return;
			else {
				if (e.type == 'mousedown')
					mousePressed = true;
				else if (e.type == 'mouseup')
					mousePressed = false;
			}


			var eventType = wmp.mapMouseToTouch[e.type];
			var touch = new Touch(e, 0);
			wmp._raiseEvent(touch.target, eventType, [touch] );
		},
		_raiseEvent: function(el, eType, touches) {

			if (this.isTouchDevice)
				;
			else {
				// following two functions should ideally be TouchEvent, but Webkit only knows UIEvent (which also does the job)
				var evt = document.createEvent('UIEvent');
				evt.initUIEvent(eType, true, true);
			}

			// Generate Touchlist
			var touchList = new TouchList(touches);

			// attach TouchEvent-Attributes not in UIEvent by default
			evt.altKey = false;
			evt.ctrlKey = false;
			evt.metaKey = false;
			evt.shiftKey = false;
			evt.view = win;

			/** todo polyfill with multi-events */
			evt.changedTouches = touchList;
			evt.targetTouches = touchList;
			evt.touches = touchList;

//			evt.preventDefault();
			el.dispatchEvent(evt);
		}
	}

	// initialisation
	wmp.isTouchDevice = wmp.checkTouchDevice();
	if (!wmp.isTouchDevice)
	{
		addEventListener('mousedown', wmp.mouseListener, true);
		addEventListener('mouseup', wmp.mouseListener, true);
		addEventListener('mousemove', wmp.mouseListener, true)
	}


	win.wmp = wmp;
	win.wmp.prototype = {
		Touch: Touch,
		TouchList: TouchList
	};

})();


