
function touchToMouse(event)
{
	alert(':) Registered Javascript-Touch Event ' + event.type + ' (#)');
	event.preventDefault();
	return;

    var touches = event.changedTouches,
        first = touches[0],
        type = "";

         switch(event.type)
    {
        case "touchstart":type = "mousedown";break;
        case "touchmove":type="mousemove";break;
        case "touchend":type="mouseup";break;
        default:return;
    }

             //initMouseEvent(type, canBubble, cancelable, view, clickCount,
    //           screenX, screenY, clientX, clientY, ctrlKey,
    //           altKey, shiftKey, metaKey, button, relatedTarget);

    var simulatedEvent = document.createEvent("MouseEvent");
    simulatedEvent.initMouseEvent(type, true, true, window, 1,
                              first.screenX, first.screenY,
                              first.clientX, first.clientY, false,
                              false, false, false, 0/*left*/, null);

                                                                                 first.target.dispatchEvent(simulatedEvent);
    event.preventDefault();
}

function mouseToTouch(event)
{
	// This will fake an arbitrary event on a node and add in the extra touch pieces
	var fireTouchEvent = function(originalEvent, newType) {
		var newEvent = document.createEvent('MouseEvent');
		newEvent.initMouseEvent(newType, true, true, window, originalEvent.detail,
			originalEvent.screenX, originalEvent.screenY, originalEvent.clientX, originalEvent.clientY,
			originalEvent.ctrlKey, originalEvent.shiftKey, originalEvent.altKey, originalEvent.metaKey,
			originalEvent.button, originalEvent.relatedTarget
		);

		// Touch events have a touches array, which contains kinda-sub-event objects
		// In this case we'll only need the one
		if (!('touches' in newEvent)) newEvent.touches = [newEvent];

		// And and they have "page" coordinates, which I guess are just like screen coordinates
		if (!('pageX' in newEvent)) newEvent.pageX = originalEvent.clientX;
		if (!('pageY' in newEvent)) newEvent.pageY = originalEvent.clientY;

		// TODO: Read the spec, fill in what's missing
		// Set this if we need to, because sometimes I use it
		window.event = window.event || newEvent;
		// Fire off the new event
		originalEvent.target.dispatchEvent(newEvent);
		// And delete the window's event property
		delete window.event;
	};
}

window.ondocument.on('touchstart', function(e){ alert('touch') });
window.on('mousedown', function(e){ alert('mousedown') });

//document.addEventListener("mousedown", touchToMouse, false);

//document.addEventListener("click", touchToMouse, false);
//document.addEventListener("touchstart", touchToMouse, false);
//document.addEventListener("touchmove", touchToMouse, false);
//document.addEventListener("touchend", touchToMouse, false);
//document.addEventListener("touchcancel", touchToMouse, false);