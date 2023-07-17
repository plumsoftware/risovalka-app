package com.plumsoftware.risovalka;

public class ADS_ID implements AdsMethods{
    private final String REWARDED_ID = String.valueOf(R.string.interstitial_id);
    private final String APPLICATION_ID = String.valueOf(R.string.application_id);
    private final String BANNER_ID = String.valueOf(R.string.banner_id);

    @Override
    public String getRewardedAdId() {
        return REWARDED_ID;
    }

    @Override
    public String getBannerAdId() {
        return BANNER_ID;
    }

    @Override
    public String getApplicationAdId() {
        return APPLICATION_ID;
    }
}
