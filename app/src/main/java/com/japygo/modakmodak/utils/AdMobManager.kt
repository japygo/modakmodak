package com.japygo.modakmodak.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AdMobManager {
    private const val TAG = "AdMobManager"
    enum class AdType { FOCUS, SHOP }

    private var focusRewardedAd: RewardedAd? = null
    private var shopRewardedAd: RewardedAd? = null

    private var isFocusAdLoading = false
    private var isShopAdLoading = false

    private val _isFocusAdLoaded = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isFocusAdLoaded: kotlinx.coroutines.flow.StateFlow<Boolean> = _isFocusAdLoaded.asStateFlow()

    private val _isShopAdLoaded = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isShopAdLoaded: kotlinx.coroutines.flow.StateFlow<Boolean> = _isShopAdLoaded.asStateFlow()

    fun initialize(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob initialized: $initializationStatus")
            // Load initial ads on Main Thread
            CoroutineScope(Dispatchers.Main).launch {
                loadRewardedAd(context, AdType.FOCUS)
                loadRewardedAd(context, AdType.SHOP)
            }
        }
    }

    fun loadRewardedAd(context: Context, type: AdType, onLoaded: (() -> Unit)? = null, onFailed: (() -> Unit)? = null) {
        val isAdLoading = if (type == AdType.FOCUS) isFocusAdLoading else isShopAdLoading
        val currentAd = if (type == AdType.FOCUS) focusRewardedAd else shopRewardedAd

        if (currentAd != null || isAdLoading) return

        if (type == AdType.FOCUS) isFocusAdLoading = true else isShopAdLoading = true
        
        val adUnitId = if (type == AdType.FOCUS) AdConfig.RewardedFocusId else AdConfig.RewardedShopId
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "RewardedAd ($type) failed to load: ${adError.message}")
                if (type == AdType.FOCUS) {
                    focusRewardedAd = null
                    isFocusAdLoading = false
                    _isFocusAdLoaded.value = false
                } else {
                    shopRewardedAd = null
                    isShopAdLoading = false
                    _isShopAdLoaded.value = false
                }
                onFailed?.invoke()
                
                // Retry after delay
                CoroutineScope(Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(5000) 
                    loadRewardedAd(context, type)
                }
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "RewardedAd ($type) loaded.")
                if (type == AdType.FOCUS) {
                    focusRewardedAd = ad
                    isFocusAdLoading = false
                    _isFocusAdLoaded.value = true
                } else {
                    shopRewardedAd = ad
                    isShopAdLoading = false
                    _isShopAdLoaded.value = true
                }
                onLoaded?.invoke()
            }
        })
    }

    fun showRewardedAd(
        activity: Activity,
        type: AdType,
        onUserEarnedReward: (Int) -> Unit,
        onAdDismissed: () -> Unit,
        onAdFailed: () -> Unit
    ) {
        val ad = if (type == AdType.FOCUS) focusRewardedAd else shopRewardedAd
        
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad ($type) dismissed.")
                    if (type == AdType.FOCUS) {
                        focusRewardedAd = null
                        _isFocusAdLoaded.value = false
                    } else {
                        shopRewardedAd = null
                        _isShopAdLoaded.value = false
                    }
                    loadRewardedAd(activity, type) // Preload next
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    Log.e(TAG, "Ad ($type) failed to show.")
                    if (type == AdType.FOCUS) {
                        focusRewardedAd = null
                        _isFocusAdLoaded.value = false
                    } else {
                        shopRewardedAd = null
                        _isShopAdLoaded.value = false
                    }
                    onAdFailed()
                }
            }

            ad.show(activity, OnUserEarnedRewardListener { rewardItem ->
                val rewardAmount = rewardItem.amount
                Log.d(TAG, "User earned reward ($type): $rewardAmount")
                onUserEarnedReward(rewardAmount)
            })
        } else {
            Log.d(TAG, "The rewarded ad ($type) wasn't ready yet.")
            onAdFailed()
            loadRewardedAd(activity, type)
        }
    }

    @Composable
    fun BannerAd(modifier: Modifier = Modifier) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdConfig.BannersId
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
