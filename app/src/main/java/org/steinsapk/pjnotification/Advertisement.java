package org.steinsapk.pjnotification;

import android.app.Activity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class Advertisement {
    static void initializeAd(Activity activity, AdView adView) {
        // 광고 초기화
        MobileAds.initialize(activity, "ca-app-pub-8135189840500081~6669562666");

        // 광고 호출
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
}
