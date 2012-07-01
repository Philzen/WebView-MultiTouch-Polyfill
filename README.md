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

### Enabling multitouch for own projects
1. Import src/com/changeit/wmpolyfill/WebClient.java into your project
2. Instanciate a WebClient Object and hand over your webview (the new WebClient will automatic become WebClient of your WebView) 

        WebClient wmp = new WebClient(webview)

To see the command in full context refer to src/com/changeit/wmpolyfill/MainActivity.java

### Enabling Multitouch in Phonegap projects *(inside your Droidgap Activity Class)*

1. see step 1. above, and additionally import src/com/changeit/wmpolyfill/PhonegapWebClient.java into your project
2. **After** calling loadUrl in your DroidGap-Activity, replace the default `CordovaWebViewClient` of the appView object with an instance of `PhonegapWebClient` like this

		super.loadUrl("http://myawesome.app.com");

		    ...

		PhonegapWebClient wmp = new PhonegapWebClient(this, appView);

Please note that at this stage this workaround hasn't been reviewed by any members of the PhoneGap community.
Any problems / feedback regarding WMP + Phonegap is kindly to be reported at https://github.com/Philzen/cordova-android-multitouch-polyfill/issues, where a merge of WMP into phonegap is planned, once WMP is tested and mature enough.

### Options
* _Boolean_	polyfillAllTouches	(default: true)
	If true, all touches on the webview (including natively working touches be intercepted and emulated in the polyfill.
	NOTE: No worries - the polyfill won't interfere with any touches if the API Level is 11 or higher (Android 3+)
* _int_		maxNativeTouches	(default: 1)
	If polyfillAllTouches is set to false, this is the number of touches already working out-of-the-box and therefore will not be interfered with

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