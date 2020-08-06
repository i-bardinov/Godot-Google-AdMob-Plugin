package ibardinov.godot.plugin.android.godotadmob;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import com.google.ads.mediation.admob.AdMobAdapter;

import com.unity3d.ads.metadata.MetaData;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

public class GodotGoogleAdMob extends GodotPlugin {
    private Activity activity = null; // The main activity of the game
    private Bundle extras = new Bundle();

    private FrameLayout layout = null; // Store the layout

    private HashMap<String, RewardedAd> rewardedAds = new HashMap<>(); // Rewarded Ad objects
    private HashMap<String, InterstitialAd> interstitialAds = new HashMap<>(); // Interstitial ad objects
    private Banner banner = null; // Banner object


    public GodotGoogleAdMob(Godot godot) {
        super(godot);
        activity = godot;
    }

    // create and add a new layout to Godot
    @Override
    public View onMainCreate(Activity activity) {
        layout = new FrameLayout(activity);
        return layout;
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "GodotGoogleAdMob";
    }

    @NonNull
    @Override
    public List<String> getPluginMethods() {
        return Arrays.asList(
                "init",
                "initWithContentRating",
                // banner
                "loadBanner", "showBanner", "hideBanner", "getBannerWidth", "getBannerHeight", "resize", "move",
                // Interstitial
                "loadInterstitial", "isInterstitialAdLoaded", "showInterstitial",
                // Rewarded Ad
                "loadRewardedAd", "isRewardedAdLoaded", "showRewardedAd");
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new ArraySet<>();

        signals.add(new SignalInfo("on_initialization_complete"));

        signals.add(new SignalInfo("on_banner_ad_loaded"));
        signals.add(new SignalInfo("on_banner_ad_failed_to_load", String.class));
        signals.add(new SignalInfo("on_banner_ad_opened"));
        signals.add(new SignalInfo("on_banner_ad_clicked"));
        signals.add(new SignalInfo("on_banner_ad_left_application"));
        signals.add(new SignalInfo("on_banner_ad_closed"));

        signals.add(new SignalInfo("on_interstitial_ad_loaded"));
        signals.add(new SignalInfo("on_interstitial_ad_failed_to_load", String.class));
        signals.add(new SignalInfo("on_interstitial_ad_opened"));
        signals.add(new SignalInfo("on_interstitial_ad_clicked"));
        signals.add(new SignalInfo("on_interstitial_ad_left_application"));
        signals.add(new SignalInfo("on_interstitial_ad_closed"));

        signals.add(new SignalInfo("on_rewarded_ad_loaded"));
        signals.add(new SignalInfo("on_rewarded_ad_failed_to_load", String.class));
        signals.add(new SignalInfo("on_rewarded_ad_opened"));
        signals.add(new SignalInfo("on_rewarded_ad_closed"));
        signals.add(new SignalInfo("on_rewarded_ad_earned_reward", String.class, Integer.class));
        signals.add(new SignalInfo("on_rewarded_ad_failed_to_show", String.class));

        return signals;
    }

    /* Init
     * ********************************************************************** */

    /**
     * Prepare for work with AdMob
     *
     * @param isReal     Tell if the enviroment is for real or test
     */
    public void init(boolean isReal) {
        this.initWithContentRating(isReal, false, true, "");
    }

    /**
     * Init with content rating additional options
     *
     * @param isReal                      Tell if the enviroment is for real or test
     * @param isForChildDirectedTreatment
     * @param isPersonalized              If ads should be personalized or not.
     *                                    GDPR compliance within the European Economic Area requires that you
     *                                    disable ad personalization if the user does not wish to opt into
     *                                    ad personalization.
     * @param maxAdContentRating          must be "G", "PG", "T" or "MA"
     */
    public void initWithContentRating(
            boolean isReal,
            boolean isForChildDirectedTreatment,
            boolean isPersonalized,
            String maxAdContentRating) {
        if (maxAdContentRating != null && maxAdContentRating != "") {
            extras.putString("max_ad_content_rating", maxAdContentRating);
        }
        if (!isPersonalized) {
            // https://developers.google.com/admob/android/eu-consent#forward_consent_to_the_google_mobile_ads_sdk
            extras.putString("npa", "1");
        }
        else
        {
            MetaData gdprMetaData = new MetaData(activity);
            gdprMetaData.set("gdpr.consent", true);
            gdprMetaData.commit();
        }
        MobileAds.initialize(activity, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                emitSignal("on_initialization_complete");
            }
        });
        List<String> testDeviceIds = Arrays.asList(getAdMobDeviceId(), AdRequest.DEVICE_ID_EMULATOR);
        RequestConfiguration configuration =  new RequestConfiguration.Builder()
                .setTestDeviceIds(isReal ? null : testDeviceIds)
                .setTagForChildDirectedTreatment(isForChildDirectedTreatment ?
                        RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE :
                        RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE)
                .setMaxAdContentRating(maxAdContentRating)
                .build();
        MobileAds.setRequestConfiguration(configuration);
        Log.d("godot", "AdMob: init with content rating options");
    }


    /**
     * Returns AdRequest object constructed considering the parameters set in constructor of this class.
     *
     * @return AdRequest object
     */
    private AdRequest getAdRequest() {
        AdRequest.Builder adBuilder = new AdRequest.Builder();
        if (!extras.isEmpty()) {
            adBuilder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
        }
        return adBuilder.build();
    }

    /* Banner Ad
     * ********************************************************************** */

    /**
     * Load a banner
     *
     * @param id      AdMod Banner ID
     * @param isOnTop To made the banner top or bottom
     */
    public void loadBanner(final String id, final boolean isOnTop) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (banner != null) banner.remove();
                banner = new Banner(id, getAdRequest(), activity, new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        emitSignal("on_banner_ad_loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        emitSignal("on_banner_ad_failed_to_load", adError.getMessage());
                    }

                    @Override
                    public void onAdOpened() {
                        emitSignal("on_banner_ad_opened");
                    }

                    @Override
                    public void onAdClicked() {
                        emitSignal("on_banner_ad_clicked");
                    }

                    @Override
                    public void onAdLeftApplication() {
                        emitSignal("on_banner_ad_left_application");
                    }

                    @Override
                    public void onAdClosed() {
                        emitSignal("on_banner_ad_closed");
                    }
                }, isOnTop, layout);
            }
        });
    }

    /**
     * Show the banner
     */
    public void showBanner() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (banner != null) {
                    banner.show();
                }
            }
        });
    }

    /**
     * Resize the banner
     * @param isOnTop To made the banner top or bottom
     */
    public void move(final boolean isOnTop) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (banner != null) {
                    banner.move(isOnTop);
                }
            }
        });
    }

    /**
     * Resize the banner
     */
    public void resize() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (banner != null) {
                    banner.resize();
                }
            }
        });
    }


    /**
     * Hide the banner
     */
    public void hideBanner() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (banner != null) {
                    banner.hide();
                }
            }
        });
    }

    /**
     * Get the banner width
     *
     * @return int Banner width
     */
    public int getBannerWidth() {
        if (banner != null) {
            return banner.getWidth();
        }
        return 0;
    }

    /**
     * Get the banner height
     *
     * @return int Banner height
     */
    public int getBannerHeight() {
        if (banner != null) {
            return banner.getHeight();
        }
        return 0;
    }

    /* Interstitial Ad
     * ********************************************************************** */

    /**
     * Load interstitial ad
     *
     * @param id AdMod Interstitial ID
     */
    public void loadInterstitial(final String id) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InterstitialAd interstitialAd = interstitialAds.get(id);
                if (interstitialAd == null) {
                    interstitialAd = new InterstitialAd(activity);
                    interstitialAd.setAdUnitId(id);
                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            emitSignal("on_interstitial_ad_loaded");
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError adError) {
                            emitSignal("on_interstitial_ad_failed_to_load", adError.getMessage());
                        }

                        @Override
                        public void onAdOpened() {
                            emitSignal("on_interstitial_ad_opened");
                        }

                        @Override
                        public void onAdClicked() {
                            emitSignal("on_interstitial_ad_clicked");
                        }

                        @Override
                        public void onAdLeftApplication() {
                            emitSignal("on_interstitial_ad_left_application");
                        }

                        @Override
                        public void onAdClosed() {
                            emitSignal("on_interstitial_ad_closed");
                        }
                    });
                    interstitialAds.put(id, interstitialAd);
                }
                interstitialAd.loadAd(getAdRequest());
            }
        });
    }

    /**
     * Check is Interstitial Ad loaded
     *
     * @param id AdMod Interstitial Ad ID
     */

    public boolean isInterstitialAdLoaded(final String id) {
        InterstitialAd interstitialAd = interstitialAds.get(id);
        if (interstitialAd != null) {
            return interstitialAd.isLoaded();
        }
        return false;
    }

    /**
     * Show interstitial ad
     */
    public void showInterstitial(final String id) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InterstitialAd interstitialAd = interstitialAds.get(id);
                if (interstitialAd != null && interstitialAd.isLoaded()) {
                    interstitialAd.show();
                }
            }
        });
    }

    /* Rewarded Ad
     * ********************************************************************** */

    /**
     * Load a Rewarded Ad
     *
     * @param id AdMod Rewarded Ad ID
     */
    public void loadRewardedAd(final String id) {
        rewardedAds.remove(id);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RewardedAd rewardedAd = new RewardedAd(activity, id);
                rewardedAd.loadAd(getAdRequest(), new RewardedAdLoadCallback() {
                    @Override
                    public void onRewardedAdLoaded() {
                        emitSignal("on_rewarded_ad_loaded");
                    }

                    @Override
                    public void onRewardedAdFailedToLoad(LoadAdError adError) {
                        emitSignal("on_rewarded_ad_failed_to_load", adError.toString());
                    }
                });
                rewardedAds.put(id, rewardedAd);
            }
        });
    }

    /**
     * Check is Rewarded Ad loaded
     *
     * @param id AdMod Rewarded Ad ID
     */

    public boolean isRewardedAdLoaded(final String id) {
        RewardedAd rewardedAd = rewardedAds.get(id);
        if (rewardedAd != null) {
            return rewardedAd.isLoaded();
        }
        return false;
    }

    /**
     * Show a Rewarded Ad
     */
    public void showRewardedAd(final String id) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RewardedAd rewardedAd = rewardedAds.get(id);
                if (rewardedAd != null && rewardedAd.isLoaded()) {
                    rewardedAd.show(activity, new RewardedAdCallback() {
                        @Override
                        public void onRewardedAdOpened() {
                            emitSignal("on_rewarded_ad_opened");
                        }

                        @Override
                        public void onRewardedAdClosed() {
                            emitSignal("on_rewarded_ad_closed");
                        }

                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem reward) {
                            emitSignal("on_rewarded_ad_earned_reward", reward.getType(), reward.getAmount());
                        }

                        @Override
                        public void onRewardedAdFailedToShow(AdError adError) {
                            emitSignal("on_rewarded_ad_failed_to_show", adError.toString());
                        }
                    });
                }
            }
        });
    }

    /* Utils
     * ********************************************************************** */

    /**
     * Generate MD5 for the deviceID
     *
     * @param s The string to generate de MD5
     * @return String The MD5 generated
     */
    private String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2) h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            //Logger.logStackTrace(TAG,e);
        }
        return "";
    }

    /**
     * Get the Device ID for AdMob
     *
     * @return String Device ID
     */
    private String getAdMobDeviceId() {
        String android_id = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = md5(android_id).toUpperCase(Locale.US);
        return deviceId;
    }

}
