package com.plumsoftware.risovalka;

import android.app.Application;

import com.yandex.mobile.ads.common.YandexAds;

public class RisovalkaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        YandexAds.initialize(this, () -> {});
    }
}
