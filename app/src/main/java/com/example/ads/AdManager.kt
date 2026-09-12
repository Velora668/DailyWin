package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdManager {
  private const val TAG = "DailyWinAdManager"

  // Official Google AdMob Rewarded Video Test Ad Unit ID
  const val REWARDED_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

  private var rewardedAd: RewardedAd? = null
  private var isAdLoading = false

  private val _isAdLoaded = MutableStateFlow(false)
  val isAdLoaded: StateFlow<Boolean> = _isAdLoaded.asStateFlow()

  /**
   * Initializes MobileAds SDK on application startup.
   */
  fun initialize(context: Context) {
    try {
      MobileAds.initialize(context) { initializationStatus ->
        Log.d(TAG, "AdMob MobileAds initialized successfully: $initializationStatus")
        // Preload first rewarded ad right after initialization
        loadRewardedAd(context.applicationContext)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error initializing AdMob MobileAds", e)
    }
  }

  /**
   * Preloads a Rewarded Video Ad with full callback handling.
   */
  fun loadRewardedAd(context: Context) {
    if (rewardedAd != null) {
      _isAdLoaded.value = true
      return
    }
    if (isAdLoading) {
      return
    }

    isAdLoading = true
    val adRequest = AdRequest.Builder().build()

    RewardedAd.load(
      context,
      REWARDED_TEST_AD_UNIT_ID,
      adRequest,
      object : RewardedAdLoadCallback() {
        override fun onAdLoaded(ad: RewardedAd) {
          Log.d(TAG, "Rewarded ad loaded successfully.")
          rewardedAd = ad
          isAdLoading = false
          _isAdLoaded.value = true
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
          Log.w(TAG, "Rewarded ad failed to load: ${loadAdError.message} (Code: ${loadAdError.code})")
          rewardedAd = null
          isAdLoading = false
          _isAdLoaded.value = false
        }
      }
    )
  }

  /**
   * Checks if an ad is ready to show.
   */
  fun isAdReady(): Boolean = rewardedAd != null

  /**
   * Shows the preloaded rewarded video ad.
   * Calls onUserEarnedReward ONLY when the user watches and earns the reward.
   * Immediately preloads the next ad once dismissed.
   */
  fun showRewardedAd(
    activity: Activity,
    onUserEarnedReward: (RewardItem) -> Unit,
    onAdDismissed: () -> Unit = {},
    onAdFailedToShow: (String) -> Unit = {}
  ) {
    val ad = rewardedAd
    if (ad == null) {
      val errMsg = "Ad is still loading or device is offline. Please try again in a moment."
      Toast.makeText(activity, errMsg, Toast.LENGTH_SHORT).show()
      onAdFailedToShow(errMsg)
      // Retry loading
      loadRewardedAd(activity.applicationContext)
      return
    }

    var userEarnedReward = false

    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
      override fun onAdShowedFullScreenContent() {
        Log.d(TAG, "Rewarded ad showed full screen content.")
      }

      override fun onAdFailedToShowFullScreenContent(adError: AdError) {
        Log.w(TAG, "Rewarded ad failed to show: ${adError.message}")
        rewardedAd = null
        _isAdLoaded.value = false
        val msg = "Unable to show ad: ${adError.message}"
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        onAdFailedToShow(msg)
        // Preload next
        loadRewardedAd(activity.applicationContext)
      }

      override fun onAdDismissedFullScreenContent() {
        Log.d(TAG, "Rewarded ad dismissed full screen content. Earned: $userEarnedReward")
        rewardedAd = null
        _isAdLoaded.value = false
        onAdDismissed()
        // Immediately preload the next rewarded ad for continuous engagement
        loadRewardedAd(activity.applicationContext)
      }
    }

    ad.show(activity) { rewardItem ->
      Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
      userEarnedReward = true
      onUserEarnedReward(rewardItem)
    }
  }
}
