package com.plumsoftware.risovalka.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.plumsoftware.risovalka.R;
import com.plumsoftware.risovalka.ads.AdsConfig;
import com.yandex.mobile.ads.appopenad.AppOpenAd;
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener;
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener;
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader;
import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;

public class MainActivity extends AppCompatActivity {
    private AppOpenAd mAppOpenAd = null;
    private AppOpenAdLoader mAppOpenAdLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SplashScreen);
        setContentView(R.layout.activity_main);

        View root = findViewById(android.R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    systemBars.bottom
            );

            return insets;
        });

        startActivity(new Intent(MainActivity.this, DrawActivity.class));
        finish();

//        mAppOpenAdLoader = new AppOpenAdLoader(this);
//        final AdRequest adRequest = new AdRequest.Builder(AdsConfig.openAdsId).build();
//        mAppOpenAdLoader.loadAd(adRequest, new AppOpenAdLoadListener() {
//            @Override
//            public void onAdLoaded(@NonNull final AppOpenAd appOpenAd) {
//                mAppOpenAd = appOpenAd;
//                mAppOpenAd.setAdEventListener(new AppOpenAdEventListener() {
//                    @Override
//                    public void onAdShown() {}
//
//                    @Override
//                    public void onAdFailedToShow(@NonNull final AdError adError) {}
//
//                    @Override
//                    public void onAdDismissed() {
//                        clearAppOpenAd();
//                    }
//
//                    @Override
//                    public void onAdClicked() {}
//
//                    @Override
//                    public void onAdImpression(@Nullable final ImpressionData impressionData) {}
//                });
//
//                startActivity(new Intent(MainActivity.this, DrawActivity.class));
//                finish();
//                showAppOpenAd();
//            }
//
//            @Override
//            public void onAdFailedToLoad(@NonNull final AdRequestError adRequestError) {
//                new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                    startActivity(new Intent(MainActivity.this, DrawActivity.class));
//                    finish();
//                }, 1300);
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        mAppOpenAdLoader = null;
        clearAppOpenAd();
        super.onDestroy();
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
