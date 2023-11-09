package com.plumsoftware.risovalka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.yandex.mobile.ads.appopenad.AppOpenAd;
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener;
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener;
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader;
import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.AdRequestConfiguration;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.MobileAds;

public class MainActivity extends AppCompatActivity {
    private AppOpenAd mAppOpenAd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SplashScreen);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, () -> {

        });



        final AppOpenAdLoader appOpenAdLoader = new AppOpenAdLoader(this);
        final String AD_UNIT_ID = "R-M-2522647-4";
        final AdRequestConfiguration adRequestConfiguration = new AdRequestConfiguration.Builder(AD_UNIT_ID).build();

        AppOpenAdEventListener appOpenAdEventListener = new AppOpenAdEventListener() {
            @Override
            public void onAdShown() {
                // Called when ad is shown.
            }

            @Override
            public void onAdFailedToShow(@NonNull final AdError adError) {
                // Called when ad failed to show.
            }

            @Override
            public void onAdDismissed() {
                // Called when ad is dismissed.
                // Clean resources after dismiss and preload new ad.
                clearAppOpenAd();
//                showAppOpenAd();
            }

            @Override
            public void onAdClicked() {
                // Called when a click is recorded for an ad.
            }

            @Override
            public void onAdImpression(@Nullable final ImpressionData impressionData) {
                // Called when an impression is recorded for an ad.
            }
        };

        AppOpenAdLoadListener appOpenAdLoadListener = new AppOpenAdLoadListener() {
            @Override
            public void onAdLoaded(@NonNull final AppOpenAd appOpenAd) {
                // The ad was loaded successfully. Now you can show loaded ad.
                mAppOpenAd = appOpenAd;
                mAppOpenAd.setAdEventListener(appOpenAdEventListener);

                startActivity(new Intent(MainActivity.this, DrawActivity.class));
                finish();

                showAppOpenAd();

            }

            @Override
            public void onAdFailedToLoad(@NonNull final AdRequestError adRequestError) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, DrawActivity.class));
                        finish();
                    }
                }, 1300);
            }
        };

        appOpenAdLoader.setAdLoadListener(appOpenAdLoadListener);

        appOpenAdLoader.loadAd(adRequestConfiguration);
    }


    private void showAppOpenAd() {
        if (mAppOpenAd != null) {
            mAppOpenAd.show(MainActivity.this);
        }
    }

    private void clearAppOpenAd() {
        if (mAppOpenAd != null) {
            mAppOpenAd.setAdEventListener(null);
            mAppOpenAd = null;
        }
    }
}