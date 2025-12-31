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

// Imports for Native Ad
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.japygo.modakmodak.R

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

    private var networkCallback: android.net.ConnectivityManager.NetworkCallback? = null

    fun initialize(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob initialized: $initializationStatus")
            // Load initial ads on Main Thread
            CoroutineScope(Dispatchers.Main).launch {
                loadRewardedAd(context, AdType.FOCUS)
                loadRewardedAd(context, AdType.SHOP)
            }
        }
        
        // Register Network Callback
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkRequest = android.net.NetworkRequest.Builder()
            .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
            
        networkCallback = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                super.onAvailable(network)
                Log.d(TAG, "Network available, retrying ad load...")
                CoroutineScope(Dispatchers.Main).launch {
                    loadRewardedAd(context, AdType.FOCUS)
                    loadRewardedAd(context, AdType.SHOP)
                }
            }
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
    }

    fun cleanup(context: Context) {
        networkCallback?.let { callback ->
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            connectivityManager.unregisterNetworkCallback(callback)
            networkCallback = null
            Log.d(TAG, "NetworkCallback unregistered")
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
                    kotlinx.coroutines.delay(3000) 
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

    fun loadNativeAd(context: Context, onAdLoaded: (NativeAd) -> Unit, onAdFailed: (LoadAdError) -> Unit) {
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(context, AdConfig.NativeAdId)
            .forNativeAd { nativeAd ->
                onAdLoaded(nativeAd)
            }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "NativeAd failed to load: ${adError.message}")
                    onAdFailed(adError)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
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

    @Composable
    fun NativeAdView(nativeAd: NativeAd, modifier: Modifier = Modifier) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                 val adView = android.view.LayoutInflater.from(context).inflate(R.layout.ad_unified, null) as NativeAdView
                 populateNativeAdView(nativeAd, adView)
                 adView
            }
        )
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        // Set the media view.
        adView.mediaView = adView.findViewById(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)


        // The headline and mediaContent are guaranteed to be in every NativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView?.mediaContent = nativeAd.mediaContent

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = android.view.View.INVISIBLE
        } else {
            adView.bodyView?.visibility = android.view.View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = android.view.View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = android.view.View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = android.view.View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = android.view.View.VISIBLE
        }
        
         if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = android.view.View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView?.visibility = android.view.View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
    }
}

