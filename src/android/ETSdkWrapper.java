package com.exacttarget.etpushsdk;

import java.util.Iterator;

import android.content.pm.PackageManager;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.exacttarget.etpushsdk.ETException;
import com.exacttarget.etpushsdk.ETLocationManager;
import com.exacttarget.etpushsdk.ETPush;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;

public class ETSdkWrapper extends CordovaPlugin {
    
	private static final String TAG = "ETSDKWrapper";
	Context context;
    
	private static boolean isActive;
	public static Activity mainActivity;
	private static CordovaWebView gWebView;
	public static String notificationCallBack;
    
	public ETSdkWrapper() {
        
	}
    
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize((CordovaInterface) cordova, webView);
		gWebView = webView;
	}
    
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";
		Log.v("action", action);
		boolean reRegisterDevice = true;
		if (context == null)
			context = this.cordova.getActivity().getApplicationContext();
        
		if (action.equals("registerForNotifications")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
			notificationCallBack = args.getString(0);
			Log.v("notification registered with: ", notificationCallBack);
                }
            });
		}
		else if (action.equals("enablePush")) {
			TogglePush(true);
			reRegisterDevice = false;
		}
		else if (action.equals("disablePush")) {
			TogglePush(false);
			reRegisterDevice = false;
		}
		else if (action.equals("enableGeoLocation")) {
			ToggleGeoLocation(true);
			reRegisterDevice = false;
		}
		else if (action.equals("disableGeoLocation")) {
			ToggleGeoLocation(false);
			reRegisterDevice = false;
		}
		else if (action.equals("addAttribute")) {
			try {
				ETPush.pushManager().addAttribute(args.getString(0), args.getString(1));
			}
			catch (ETException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		else if (action.equals("removeAttribute")) {
			try {
				ETPush.pushManager().removeAttribute(args.getString(0));
			}
			catch (ETException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		else if (action.equals("addTag")) {
			try {
				ETPush.pushManager().addTag(args.getString(0));
			}
			catch (ETException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		else if (action.equals("removeTag")) {
			try {
				ETPush.pushManager().removeTag(args.getString(0));
			}
			catch (ETException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		else if (action.equals("register")) {
			//Log.v("console", args.getBoolean(0) + args.getBoolean(1));
			Boolean analytics = args.getBoolean(0);
			Boolean loc = args.getBoolean(1);
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
			register(analytics, loc);
                }
            });
		}
		else if (action.equals("setSubscriberKey")) {
			try {
				ETPush.pushManager().setSubscriberKey(args.getString(0));
			}
			catch (ETException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			return false;
		}
		if (reRegisterDevice) {
			try {
				if (ETPush.pushManager().isPushEnabled())
					ETPush.pushManager().enablePush(null);
				else
					ETPush.pushManager().disablePush(null);
			}
			catch (ETException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        
		return true;
	}
    
	public void register(Boolean location, Boolean analytics) {
		//This method sets up the ExactTarget mobile push system
        
		context = this.cordova.getActivity().getApplicationContext();
		try {
			Bundle bundle = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
			boolean isDebuggable = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
			String appID;
			String accessToken;
			String gcmSenderID;
            
			if (isDebuggable) {
				ETPush.setLogLevel(Log.DEBUG);
				appID = bundle.getString("ETApplicationID_dev");
				accessToken = bundle.getString("ETAccessToken_dev");
				gcmSenderID = bundle.getString("GCMSenderID_dev");
			}
			else {
				appID = bundle.getString("ETApplicationID_prod");
				accessToken = bundle.getString("ETAccessToken_prod");
				gcmSenderID = bundle.getString("GCMSenderID_prod");
			}
			isActive = true;
			ETPush.readyAimFire(context, appID,
                                accessToken, analytics, location, false);
			ETPush pushManager = ETPush.pushManager();
			pushManager.setNotificationRecipientClass(ETPushNotificationRecipient.class);
			pushManager.setGcmSenderID(gcmSenderID);
		}
		catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
        
		// enable push manager
		try {
			if (!ETPush.pushManager().isPushEnabled()) {
				ETPush.pushManager().enablePush(null);
			}
		}
		catch (ETException e) {
			Log.e(TAG, e.getMessage(), e);
		}
        
	}
    
	public void ToggleGeoLocation(boolean enabled) {
		if (enabled) {
			try {
				if (!ETLocationManager.locationManager().isWatchingLocation()) {
					Log.d(TAG, "Geo enabled");
					ETLocationManager.locationManager().startWatchingLocation();
				}
			}
			catch (ETException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		else {
			try {
				if (ETLocationManager.locationManager().isWatchingLocation()) {
					Log.d(TAG, "Geo Disbaled");
					ETLocationManager.locationManager().stopWatchingLocation();
					;
				}
			}
			catch (ETException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}
    
	public void TogglePush(boolean enablePush) {
		if (enablePush) {
			// enable push manager
			try {
				if (!ETPush.pushManager().isPushEnabled()) {
					ETPush.pushManager().enablePush(mainActivity);
				}
			}
			catch (ETException e) {
				Log.e("catch", e.getMessage(), e);
			}
		}
		else {
			try {
				if (ETPush.pushManager().isPushEnabled()) {
					ETPush.pushManager().disablePush(mainActivity);
				}
			}
			catch (ETException e) {
				Log.e("catch", e.getMessage(), e);
			}
            
		}
	}
    
	public static void sendPushPayload(JSONObject json) {
		String callBack = "javascript:" + notificationCallBack + "(" + json.toString() + ")";
		Log.v(TAG, callBack);
		gWebView.sendJavascript(callBack);
	}
    
	public static boolean isActive() {
		return isActive;
	}
    
}
