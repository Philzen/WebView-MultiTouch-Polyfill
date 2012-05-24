
This piece of code might be what you have been long searching for, if
* you want to develop an HTML5 app
* you want it to work on Android 2.x
* you'd like it to be capable to track more than one fingertap. The most well-known use for this is the pinch zoom - but you could also be developing a cool HTML5-based game that needs some two (or more) finger gestures.

Many people are surprised to learn this doesn't work on many (most?) Android 2.x devices. If all of the above applies to you and you have been going crazy (like me) because you thought you now had to either implement everything in java or change your app to only work with boring single-taps, you can now smile again because you have found

webview-multitouch-polyfill
===========================
### A Polyfill example to enable multitouch functionality in Android 2.x HTML 5 Apps
------------------------------------------------------------------------------------

WebView MultiTouch PolyFill (WMP) is basically a few lines of Android java code which register touch events on an Android.WebKit.View and pass them on into the HTML Apps' DOM via javascript calls. The example project compiles against Android 2.3.3 (API 10), but the polyfill technique employed should work on all Android 2.x Devices. That being said - i personnally only own a API 10 device, so it'd be great if you folks out there could test on other (earlier) devices as well.

### Roadmap

v0.1 - in a couple of days
Is just going to be the proof of concept, a small running HTML5 recognizing more than one tap at once

v0.2 - some time after
Is just going to be the proof of concept, a small running HTML5 recognizing more than one tap at once

v1.0 - when many people have tested it v0.2+ on many many different android 2.x phones
The final first version of the WebClient class should be raising all standard events [http://en.wikipedia.org/wiki/DOM_events#Touch_events] one (as in "Developer" or "My OSM Map") would nowadays expect to happen in the browser.