package com.japygo.modakmodak.utils

import com.japygo.modakmodak.BuildConfig

object AdConfig {
    val BannersId: String
        get() = BuildConfig.ADMOB_BANNER_ID

    val RewardedFocusId: String
        get() = BuildConfig.ADMOB_REWARDED_FOCUS_ID

    val RewardedShopId: String
        get() = BuildConfig.ADMOB_REWARDED_SHOP_ID
}
