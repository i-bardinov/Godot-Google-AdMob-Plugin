# Godot Google AdMob Plugin

This is a Android plugin for [Godot Engine](https://github.com/godotengine/godot) 3.2.2 or higher.

This plugin supports:
- Banners
- Interstitial Ads
- Rewarded Video Ads

## Setup

- Configure, install  and enable the "Android Custom Template" for your project, just follow the [official documentation](https://docs.godotengine.org/en/latest/getting_started/workflow/export/android_custom_build.html);
- go to the release tab, choose a version and download the respective package;
- extract the package and put```GodotAdmob.gdap``` and ```GodotAdmob.release.aar``` inside the ```res://android/plugins``` directory on your Godot project.
- also put ```admob-script``` directory (from the zip package) inside the ```res://scripts/``` directory on your Godot project.
- on the Project -> Export... -> Android -> Options -> 
    - Permissions: check the permissions for _Access Network State_ and _Internet_
    - Custom Template: check the _Use Custom Build_
    - Plugins: check the _Godot Google Ad Mob_ (this plugin)
- edit the file ```res//android/build/AndroidManifest.xml``` to add your App ID as described [here](https://developers.google.com/admob/android/quick-start#update_your_androidmanifestxml). For the demo project, for example, you should use:
```
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-3940256099942544~3347511713"/>
```
**NOTE**: everytime you install a new version of the Android Build Template this step must be done again, as the ```AndroidManifest.xml``` file will be overriden.


Now you'll be able to add an AdMob Node to your scene (**only one node should be added per scene**).
You can edit it's properties and attach to signals.

## API Reference

### Properties
```python
# If true use your real ad, if false use test ads. Make sure to only set it to true with your published apk, otherwise you can be banned by Google
# type bool, default false
is_real

# If true, displays banner on the top of the screen, if false displays on the bottom 
# type bool, default true
banner_on_top

# Your app banner ad ID
# type String, optional
banner_id

# Your app interstitial ad ID
# type String, optional
interstitial_id

# Your app rewarded video ad ID
# type String, optional
rewarded_id

# If true, set the ads to children directed. If true, max_ad_content_rate will be ignored (your max_ad_content_rate would can not be other than "G")
# type bool, default false
child_directed

# If ads should be personalized. In the European Economic Area, GDPR requires ad personalization to be opt-in.
# type bool, default true
is_personalized

# Its value must be "G", "PG", "T" or "MA". If the rating of your app in Play Console and your config of max_ad_content_rate in AdMob are not matched, your app can be banned by Google
# type String, default G
max_ad_content_rate 
```

### Methods
```python

# Load the banner (and show inmediatly)
load_banner()

# Load the interstitial ad
load_interstitial()

# Load the rewarded video ad
load_rewarded_video()

# Show the banner ad
show_banner()

# Hide the banner ad		
hide_banner()

# Move banner after loaded
move_banner(on_top: bool)

# Show the interstitial ad
show_interstitial()

# Show the rewarded video ad
show_rewarded_video()

# Check if the interstitial ad is loaded
# @return bool true if is loaded
is_interstitial_loaded()

# Check if the rewarded video ad is loaded
# @return bool true if is loaded
is_rewarded_video_loaded()

# Resize the banner (useful when the orientation changes for example)
banner_resize()

# Get the current banner dimension 
# @return Vector2 (width, height)
get_banner_dimension()

```
### Signals
```python
# Banner ad was loaded with success
banner_ad_loaded

# Banner ad has failed to load
# @param String error the error code
banner_ad_failed_to_load(error)

# Banner ad was opened with success
banner_ad_opened

# Banner ad was clicked
banner_ad_clicked

# User exit from application with banner ad opened
banner_ad_left_application

# Banner ad was closed
banner_ad_closed

# Interstitial ad was loaded with success
interstitial_ad_loaded

# Interstitial ad has failed to load
# @param String error the error code
interstitial_ad_failed_to_load(error)

# Interstitial ad was opened
interstitial_ad_opened

# Interstitial ad was clicked
interstitial_ad_clicked

# User exit from application with interstitial ad opened
interstitial_ad_left_application

# Interstitial ad was closed
interstitial_ad_closed

# Rewarded video ad was loaded with success
rewarded_ad_loaded

# Rewarded video ad was not loaded
rewarded_ad_failed_to_load(error)

# Rewarded video ad was watched and will reward the user
# @param String currency The reward item description, ex: coin
# @param int amount The reward item amount
rewarded_ad_opened(currency, amount)

# Rewarded video ad was closed
rewarded_ad_closed

# Rewarded video ad was watched and will reward the user
# @param String currency The reward item description, ex: coin
# @param int amount The reward item amount
rewarded_ad_earned_reward(type, amount)

# Rewarded video ad has failed to show
# @param String error_code the error code
rewarded_ad_failed_to_show(error)
```

## Compiling the Plugin (optional)

If you want to compile the plugin by yourself, it's very easy:
1. clone this repository;
2. checkout the desired version;
3. open ```godot-google-admob``` directory in ```Android Studio```
4. don't forget to put ```godot-lib.release.aar``` to ```godot-lib.release``` directory

If everything goes fine, you'll find the ```.aar``` files at ```godot-google-admob/godotadmob/build/outputs/aar/```.

## Troubleshooting

* First of all, please make sure you're able to compile the custom build for Android without the AdMob plugin, this way we can isolate the cause of the issue.

* Using logcat for Android is the best way to troubleshoot most issues. You can filter Godot only messages with logcat using the command: 
```
adb logcat -s godot
```
* _AdMob Java Singleton not found_: 
    1. this plugin is Android only, so the AdMob Java singleton will only exists on the Android platform. In other words, you will be able to run it on an Android device (or emulator) only, it will not work on editor or on another platform;
    2. make sure you checked the _Use Custom Build_ and _Godot Ad Mob_ options in the export window.

* Error code 3 (_ERROR_CODE_NO_FILL_) is a common issue with Admob, but out of the scope to this plugin. Here's the description on the API page: [ERROR_CODE_NO_FILL: The ad request was successful, but no ad was returned due to lack of ad inventory.](https://developers.google.com/android/reference/com/google/android/gms/ads/AdRequest.html#ERROR_CODE_NO_FILL)

* Any other error code: you can find more information about the error codes [here](https://support.google.com/admob/thread/3494603). Please don't open issues on this repository asking for help about that, as we can't provide any, sorry.

* Banner sizes: this plugin uses [Adaptive Banners](https://developers.google.com/admob/android/banner/adaptive), you can find more information on how its sizes works on the [official AdMob documentation](https://developers.google.com/admob/android/banner/adaptive).

## References

Based on the works of:
* https://github.com/Shin-NiL/Godot-Android-Admob-Plugin

## License

MIT license
