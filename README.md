# Journey Builder for APPS SDK for Cordova/PhoneGap

[![Join the chat at https://gitter.im/DeloitteDigital/JB4A-SDK-CordovaPlugin](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/DeloitteDigital/JB4A-SDK-CordovaPlugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Cordova plugin that implements the Journey Builder for APPS SDK to add push functionality to your Phonegap or Cordova applications.

## Release History

### Version 1.1.0
_Released November 17, 2014_

* Updated to use the 3.3.0 Journey Builder for APPS SDK.
* Support for Xcode 6 added as well as iOS8.
* get methods for attributes and device information added.

### Version 1.0.1
_Released June 19, 2014_

* Custom key functionality and callbacks to Javascript from native added.

### Version 1.0.0
_Released June 10, 2014_

* First public version live
* Uses iOS SDK version 3.1.1 and android version 3.1.2

## Installing the Plugin

Follow these [instructions](https://code.exacttarget.com/mobilepush/integrating-mobilepush-sdk-your-ios-mobile-app#How) for ios to provision and setup your [Code@ExactTarget](http://code.exacttarget.com) apps

Follow these [instructions](https://code.exacttarget.com/mobilepush/integrating-mobilepush-sdk-your-android-mobile-app#How) for android to provision and setup your [Code@ExactTarget](http://code.exacttarget.com) apps

Once provisioning and [Code@ExactTarget](http://code.exacttarget.com) apps are setup we can install the plugin with the following command:
**be sure to replace the values below with your Code@ET app ids/access tokens and the GCM Sender IDs.

```Bash
cordova plugin add https://github.com/exacttarget/MobilePushSDK-CordovaPlugin 
	--variable DEVAPPID='427c085f-5358944f2-a8f7-bbc5150c77c5' 
	--variable DEVACCESSTOKEN='yay73bzx6eygw8ypaqr67fvt'
	--variable PRODAPPID='35a19ebc-50ae-4ed5-9d6c-404290ada3cd'
	--variable PRODACCESSTOKEN='cghknp9rjrmk9pkf6qh392u3'
	--variable GCMSENDERIDDEV='123456'
	--variable GCMSENDERIDPROD='123456'
	--variable USEGEO='true'
	--variable USEANALYTICS='true'
```

### Android Installation

#### Add the android gcm library to your project

1. Via terminal use comand 'android' this will open the android sdk manager

2. Under extras choose the Google Play Services library

3. Update the GCM library with the command ```android update lib-project --path path/to/android/sdk/lib/project --target '<android api version you want to target>'```. You can get a list of the currently installed targets with command ```android list targets```.

4. Add ```android.library.reference.2=relative/path/from/local.properties/to/android/sdk/installation/extras/google/google_play_services/libproject/google-play-services_lib``` to the Android platforms local.properties file.
 **Note the number after reference as this needs to be above any current references that are in your local.properties or project.properties files.**

#### Add the following lines of code to the main activity class in your project, usually ```platforms/android/src/packageName/<projectname>/<class>.java```. This is needed for analytics.

```java
@Override

protected void onResume() {
	super.onResume(); 
	try {
		ETPush.pushManager().activityResumed(this); 
	}
	catch (ETException e) {

	Log.e(TAG, e.getMessage(), e);
	}
} 

@Override
protected void onPause() { 
	super.onPause();
	try { 
		ETPush.pushManager().activityPaused(this);
	}

	catch (ETException e) {
		Log.e(TAG, e.getMessage(), e); 
	}
}
```

### iOS Installation

#### Add the following to the ```appDelegate.m``` file in ```application:didFinishLaunchingWithOptions:```

```objective-c
NSBundle* mainBundle = [NSBundle mainBundle];
    NSDictionary* ETSettings = [mainBundle objectForInfoDictionaryKey:@"ETAppSettings"];
    BOOL useGeoLocation = [[ETSettings objectForKey:@"UseGeofences"] boolValue];
    BOOL useAnalytics = [[ETSettings objectForKey:@"UseAnalytics"] boolValue];
#ifdef DEBUG
    NSString* devAppID = [ETSettings objectForKey:@"ApplicationID-Dev"];
    NSString* devAccessToken = [ETSettings objectForKey:@"AccessToken-Dev"];
    //use your debug app id and token you setup in code.exacttarget.com here
    [[ETPush pushManager] configureSDKWithAppID:devAppID
                                 andAccessToken:devAccessToken
                                  withAnalytics:useAnalytics
                            andLocationServices:useGeoLocation
                                  andCloudPages:NO];
    
#else
    NSString* prodAppID = [ETSettings objectForKey:@"ApplicationID-Prod"];
    NSString* prodAccessToken = [ETSettings objectForKey:@"AccessToken-Prod"];
    //use your production app id and token you setup in code.exacttarget.com here
    
    [[ETPush pushManager] configureSDKWithAppID:prodAppID
                                 andAccessToken:prodAccessToken
                                  withAnalytics:useAnalytics
                            andLocationServices:useGeoLocation
                                  andCloudPages:NO];
    
#endif
    
    // IPHONEOS_DEPLOYMENT_TARGET = 6.X or 7.X
#if __IPHONE_OS_VERSION_MIN_REQUIRED < 80000
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 80000
    // Supports IOS SDK 8.X (i.e. XCode 6.X and up)
    // are we running on IOS8 and above?
    if ([[UIApplication sharedApplication] respondsToSelector:@selector(registerForRemoteNotifications)]) {
        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:
                                                 UIUserNotificationTypeBadge | UIUserNotificationTypeSound | UIUserNotificationTypeAlert
                                                                                  categories:nil];
        [[ETPush pushManager] registerUserNotificationSettings:settings];
        [[ETPush pushManager] registerForRemoteNotifications];
    }
    else {
        [[ETPush pushManager] registerForRemoteNotificationTypes:UIRemoteNotificationTypeAlert|UIRemoteNotificationTypeBadge|UIRemoteNotificationTypeSound];
    }
#else
    // Supports IOS SDKs < 8.X (i.e. XCode 5.X or less)
    [[ETPush pushManager] registerForRemoteNotificationTypes:UIRemoteNotificationTypeAlert|UIRemoteNotificationTypeBadge|UIRemoteNotificationTypeSound];
#endif
#else
    // IPHONEOS_DEPLOYMENT_TARGET >= 8.X
    // Supports IOS SDK 8.X (i.e. XCode 6.X and up)
    UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:
                                             UIUserNotificationTypeBadge | UIUserNotificationTypeSound | UIUserNotificationTypeAlert
                                                                              categories:nil];
    [[ETPush pushManager] registerUserNotificationSettings:settings];
    [[ETPush pushManager] registerForRemoteNotifications];
#endif
    [[ETPush pushManager] shouldDisplayAlertViewIfPushReceived:YES];
    [[ETPush pushManager] applicationLaunchedWithOptions:launchOptions];
    NSString* token = [[ETPush pushManager] deviceToken];
    NSString* deviceID = [ETPush safeDeviceIdentifier]; NSLog(@"token %@", token);
    NSLog(@"Device ID %@", deviceID);
    if (useGeoLocation) {
        [[ETLocationManager locationManager] startWatchingLocation]; 
    }
```

#### Add the following function to the ```appDelegate.m``` file
```objective-c
-(void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
    [[ETPush pushManager] handleNotification:userInfo forApplicationState:application.applicationState];
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:userInfo
                                                       options:0
                                                         error:&error];
    if (!jsonData) {
        
        NSLog(@"json error: %@", error);
        
    } else {
        
        [ETSdkWrapper.etPlugin notifyOfMessage:jsonData];
    }
}
```

#### Replace the following function in the ```appDelegate.m``` file

```objective-c
- (void) application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    [[ETPush pushManager] registerDeviceToken:deviceToken];
    // re-post ( broadcast )
    NSString* token = [[[[deviceToken description]
                         stringByReplacingOccurrencesOfString: @"<" withString: @""]
                        stringByReplacingOccurrencesOfString: @">" withString: @""]
                       stringByReplacingOccurrencesOfString: @" " withString: @""];

    [[NSNotificationCenter defaultCenter] postNotificationName:CDVRemoteNotification object:token];
}
```
```objective-c
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    
    [[ETPush pushManager] applicationDidFailToRegisterForRemoteNotificationsWithError:error];
    // re-post ( broadcast )
    [[NSNotificationCenter defaultCenter] postNotificationName:CDVRemoteNotificationError object:error];
}
```

```objective-c
- (void)application:(UIApplication*)application didReceiveLocalNotification:(UILocalNotification*)notification
{
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:notification.userInfo
                                                       options:0
                                                         error:&error];
    if (!jsonData) {
        
        NSLog(@"jsn error: %@", error);
        
    } else {
        
        [ETSdkWrapper.etPlugin notifyOfMessage:jsonData];
    }
    // re-post ( broadcast )
    [[NSNotificationCenter defaultCenter] postNotificationName:CDVLocalNotification object:notification];
}
```





