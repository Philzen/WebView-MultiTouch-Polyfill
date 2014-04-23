
package org.changeit.android2;

import com.changeit.wmpolyfill.Cordova3WebClient;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

/*
 * @author philzen
 */
public class MultiTouchView extends CordovaPlugin {

    private Cordova3WebClient wmp;

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        wmp = new Cordova3WebClient(cordova, webView);
    }
}
