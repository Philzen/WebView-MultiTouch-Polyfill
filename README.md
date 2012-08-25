# webview-multitouch-polyfill
A polyfill re-enabling multitouch functionality on Android 2.x HTML 5 apps
---------------------------------------------------------------------------

This piece of code is for you, if
* you want to develop an HTML5 app
* you'd like to track more than one fingertap action ("touch events")
* you want it to work on more pre-Android-3-phones

### Demo
Compile the project or just start the included apk in the /bin directory.
The Demo app includes a slightly modified version of the scripty2 Touchspector to visualise your touches,
as well as links to Online MultiTouch examples.

## Usage
1. Copy bin/wmp.jar into your own projects' `libs` folder
2. In your Main Activity, create a new `WebClient` object and pass it to the `WebView` that you want to enable multitouch on via `setWebViewClient()`:

        WebClient wmp = new WebClient(webview)

To see the command in full context refer to src/com/changeit/wmpolyfill/MainActivity.java

### Enabling Multitouch for Phonegap 1.9+ (Cordova) projects

As above, but instead of `WebClient` use `CordovaWebClient`:
		CordovaWebClient wmp = new CordovaWebClient(this, appView);

### Enabling Multitouch for Phonegap <1.9 projects <small>(tested with 1.8.1)</small

1. Copy WebClient.java and PhonegapWebClient.java from src/com/changeit/wmpolyfill/ into your project. You will need to refactor those classes namespace to match those of your project - some IDEs (i.e. Netbeans) will do that conveniently for you as you paste the files
2. In your Main (`DroidGap`) Activity, instantiate a new `PhonegapWebClient`:

		PhonegapWebClient wmp = new PhonegapWebClient(this, appView);

### Options
* setPolyfillAllTouches (Boolean)
	[default: `false`] Per default WMP won't do anything to single hand gestures in order not to interfere with varying event implementations on different devices. If you set this value to `true`, all touches on the webview will be intercepted and emulated in the polyfill.
	NOTE: The polyfill won't interfere with any touches (basically it will be inactive) if the API Level is 11 or higher (= devices running Android 3+)

### Miscellaneous
Please visit https://github.com/Philzen/WebView-MultiTouch-Polyfill/wiki for further information and ongoing development updates.

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
