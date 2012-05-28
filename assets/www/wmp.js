(function(){
	mousePressed = false;
})();


Touch = function (e, id) {

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


	wmp = {
		mapMouseToTouch: {
			mousedown: 'touchstart',
			mousemove: 'touchmove',
			mouseup: 'touchend'
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
			wmp._raiseEvent(touch.target, eventType, touch);
		},
		_raiseEvent: function(el, eType, touch) {
			// following two functions should ideally be TouchEvent, but Webkit only knows UIEvent (which also does the job)
			var evt = document.createEvent('UIEvent');
			evt.initUIEvent(eType, true, true);

			// Generate Touchlist
			var touchList = new function TouchList( ) {
				this[0] = touch;
				this.length = (eType == 'touchend') ? 0 : 1;
			};
			//console.log(eType, touchList.length);
			// attach TouchEvent-Attributes not in UIEvent by default
			evt.altKey = false;
			evt.ctrlKey = false;
			evt.metaKey = false;
			evt.shiftKey = false;
			evt.view = window;

			/** todo polyfill with multi-events */
			evt.changedTouches = touchList;
			evt.targetTouches = touchList;
			evt.touches = touchList;

//			evt.preventDefault();
			el.dispatchEvent(evt);
		}
	}
