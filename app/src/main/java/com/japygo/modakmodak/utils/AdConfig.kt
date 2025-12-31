package com.japygo.modakmodak.utils

import com.japygo.modakmodak.BuildConfig

object AdConfig {
    val BannersId: String
        get() = BuildConfig.ADMOB_BANNER_ID

    val RewardedFocusId: String
        get() = BuildConfig.ADMOB_REWARDED_FOCUS_ID

    val RewardedShopId: String
        get() = BuildConfig.ADMOB_REWARDED_SHOP_ID

    val NativeAdId: String
        get() = "ca-app-pub-3940256099942544/2247696110" // Test Ad ID
}
