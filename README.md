# WebView MultiTouch Polyfill
> ... joyfully re-enabling multitouch functionality for HTML 5 apps on many pre-Android 3 devices

------

This piece of code is for you, if
* you want to develop an HTML5 app
* you'd like to track more than one fingertap action ("touch events")
* you want it to work on more pre-Android-3-phones


## Usage

### Cordova 3.x

##### `cordova plugin add https://github.com/Philzen/WebView-MultiTouch-Polyfill`

That's it - without further ado, any 2nd, 3rd, whatever other touch your device understands
will be polyfilled in your `(Cordova)WebView` when running on android 2.x

### Custom webview projects / cordova 1.9+

Grab the latest release version from https://github.com/Philzen/WebView-MultiTouch-Polyfill/tags

1. Copy wmp.jar into your own projects' `libs` folder
2. In your Main Activity, create a new `WebClient` object, passing in the `WebView` that you want to enable multitouch on:

		WebClient wmp = new WebClient(webview);

Refer to the [MainActivity](https://github.com/Philzen/Webview-MultiTouch-Polyfill-Demo/blob/master/src/com/changeit/wmpdemo/MainActivity.java) of the demo app to see the command in context.

#### Enabling Multitouch for Phonegap 1.9+ (Cordova) projects

- As above, but instead of `WebClient` use `CordovaWebClient`:

		CordovaWebClient wmp = new CordovaWebClient(this, appView);

#### Enabling Multitouch for legacy (pre-1.9) phonegap projects

1. Copy WebClient.java and PhonegapWebClient.java from src/com/changeit/wmpolyfill/ into your project. You will need to refactor those classes namespace to match those of your project - some IDEs (i.e. Netbeans) will do that conveniently for you as you paste the files
2. In your Main (`DroidGap`) Activity, instantiate a new `PhonegapWebClient`:

		PhonegapWebClient wmp = new PhonegapWebClient(this, appView);

This solution was tested with Phonegap 1.8.1 and should work with many earlier versions.


### Options

##### `Boolean` setPolyfillAllTouches [default: `false`] 

  By default WMP won't do anything to single hand gestures in order not to interfere with varying event implementations on different devices. If you set this value to `true`, all touches on the webview will be intercepted and emulated in the polyfill.  
  NOTE: The polyfill won't interfere with any touches (basically it will be inactive) if the API Level is 11 or higher (= devices running Android 3+)


### Demo

Preview & Play around with WMP by installing WebView-MultiTouch-Polyfill-DemoApp.apk on your device (compiled for Level 10 API = Android 2.3.3+).
Full source code found [here on github](https://github.com/Philzen/Webview-MultiTouch-Polyfill-Demo).


### Miscellaneous

#### Help Testing

1. *USE IT*... and add your device details to the [tested device list](https://github.com/Philzen/WebView-MultiTouch-Polyfill/wiki/Device-Chart) - if at least the demo app works for your device
2. don't hesitate to [report any WMP-related issues](https://github.com/Philzen/WebView-MultiTouch-Polyfill/issues) you may encounter on your device and/or app  


#### Help Hacking [![Stories in Ready](https://badge.waffle.io/philzen/webview-multitouch-polyfill.png?label=ready&title=Ready)](https://waffle.io/philzen/webview-multitouch-polyfill)  

Preferable pick issues in "ready" status on the [waffle.io board](https://badge.waffle.io/philzen/webview-multitouch-polyfill.png), but of course you are welcome to work on any other issues and send in pull requests for other cool new features & fixes unthought of so far.

### Licence Information
The author of this repository strongly sympathises with the "Non-Military Use Only" Licence model. However, since it poses a logical contradiction of the open source definition, all rights are hereby granted under the Apache licence:

	Copyright 2012 github.com/Philzen et. al.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
