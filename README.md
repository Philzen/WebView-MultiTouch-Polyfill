![PolyFill Proof of Concept on HTC Desire Z](http://phil.timoessner.de/wmp/wmp.png)

This piece of code is for you, if
* you want to develop an HTML5 app
* you want it to work cross-platform including Android 2.x
* you'd like to track more than one fingertap action ("touch events")

	The most well-known use for this is the pinch zoom - but you could also be developing a cool HTML5-based game that needs some multi-finger-gestures.

Some people are surprised to learn this doesn't work on many (most?) Android 2.x devices. If all of the above applies to you and you have been going crazy (like me) on the question why the heck it's not enabled on those phones, you can stop coursing and continue developing your app, because this is

# webview-multitouch-polyfill
### A Polyfill example to enable multitouch functionality in Android 2.x HTML 5 Apps
------------------------------------------------------------------------------------

WebView MultiTouch PolyFill (WMP) is basically a few lines of Android java code which register touch events on an `Android.WebKit.View` and pass them on to an HTML Apps' DOM via javascript calls. The example project compiles against Android 2.3.3 (API 10), but the polyfill technique employed should work on all Android 2.x Devices. That being said - i personnally only own a API 10 device, so it'd be great if you folks out there could test on other (earlier) devices as well.

### Roadmap

**v0.1** - ~~in a couple of days~~ DONE ✓

Is just ~~going to be~~ the proof of concept, a small running HTML5 recognizing more than one tap at once

**v0.2** - ~~hopefully soon~~ DONE ✓

~~Make~~ Open Layers and Google Maps pinch-zoom work

**v0.x** - over time

Should be raising all [standard touch events](http://en.wikipedia.org/wiki/DOM_events#Touch_events) one (as in "Developer" or "My OSM Map") would expect to happen in the browser, just like on any Android 3+ or newer iPhone.

**v1.0** - when many people have tested it on many many different android 2.x phones

Final version of the WebClient class