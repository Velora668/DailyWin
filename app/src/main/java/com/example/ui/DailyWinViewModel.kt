package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdManager
import com.example.data.RewardRepository
import com.example.model.DayStreakItem
import com.example.model.PayoutMethod
import com.example.model.TransactionItem
import com.example.model.UserProfile
import com.example.model.WheelSector
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AdWatchState {
  object Idle : AdWatchState()
  data class Watching(val progress: Float, val secondsRemaining: Int) : AdWatchState()
  data class Completed(val rewardCoins: Int) : AdWatchState()
}

sealed class SpinWheelState {
  object Idle : SpinWheelState()
  data class Spinning(val targetRotation: Float, val rewardSector: WheelSector) : SpinWheelState()
  data class Won(val rewardSector: WheelSector) : SpinWheelState()
}

data class CelebrationData(
  val title: String,
  val subtitle: String,
  val coinsEarned: Int
)

class DailyWinViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = RewardRepository(application)

  val walletBalance: StateFlow<Int> = repository.walletBalance
  val streakDays: StateFlow<Int> = repository.streakDays
  val isTodayClaimed: StateFlow<Boolean> = repository.isTodayClaimed
  val spinsRemaining: StateFlow<Int> = repository.spinsRemaining
  val transactions: StateFlow<List<TransactionItem>> = repository.transactions
  val userProfile: StateFlow<UserProfile> = repository.userProfile

  val balanceInRupees: StateFlow<Double> = walletBalance.combine(MutableStateFlow(10.0)) { balance, divisor ->
    balance / divisor
  }.stateIn(viewModelScope, SharingStarted.Eagerly, 25.0)

  // 7-day streak visual tracker list
  val dailyStreakList: StateFlow<List<DayStreakItem>> =
    combine(streakDays, isTodayClaimed) { streak, claimedToday ->
      val streakRewards = listOf(20, 25, 30, 35, 40, 50, 100)
      (1..7).map { day ->
        val isClaimed = if (claimedToday) day <= streak else day < streak
        val isToday = if (claimedToday) false else day == streak
        val isLocked = if (claimedToday) day > streak else day > streak
        DayStreakItem(
          dayNumber = day,
          coinsReward = streakRewards.getOrElse(day - 1) { 20 },
          isClaimed = isClaimed,
          isToday = isToday,
          isLocked = isLocked
        )
      }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  private val _adWatchState = MutableStateFlow<AdWatchState>(AdWatchState.Idle)
  val adWatchState: StateFlow<AdWatchState> = _adWatchState.asStateFlow()

  private val _spinWheelState = MutableStateFlow<SpinWheelState>(SpinWheelState.Idle)
  val spinWheelState: StateFlow<SpinWheelState> = _spinWheelState.asStateFlow()

  private val _celebrationData = MutableStateFlow<CelebrationData?>(null)
  val celebrationData: StateFlow<CelebrationData?> = _celebrationData.asStateFlow()

  // Wheel sectors
  val wheelSectors = listOf(
    WheelSector(5, "5 Coins", 0xFFF59E0B),
    WheelSector(10, "10 Coins", 0xFF8B5CF6),
    WheelSector(15, "15 Coins", 0xFF3B82F6),
    WheelSector(20, "20 Coins", 0xFF10B981),
    WheelSector(25, "25 Coins", 0xFFEC4899),
    WheelSector(30, "30 Coins", 0xFFF97316),
    WheelSector(40, "40 Coins", 0xFF06B6D4),
    WheelSector(50, "50 Coins", 0xFFFFD700)
  )

  // Wallet & Withdrawal State
  private val _selectedPayoutMethod = MutableStateFlow(PayoutMethod.UPI)
  val selectedPayoutMethod: StateFlow<PayoutMethod> = _selectedPayoutMethod.asStateFlow()

  private val _payoutDestinationInput = MutableStateFlow("")
  val payoutDestinationInput: StateFlow<String> = _payoutDestinationInput.asStateFlow()

  private val _selectedCoinsToWithdraw = MutableStateFlow(500)
  val selectedCoinsToWithdraw: StateFlow<Int> = _selectedCoinsToWithdraw.asStateFlow()

  private val _withdrawalProcessing = MutableStateFlow(false)
  val withdrawalProcessing: StateFlow<Boolean> = _withdrawalProcessing.asStateFlow()

  private val _withdrawalSuccessReceipt = MutableStateFlow<TransactionItem?>(null)
  val withdrawalSuccessReceipt: StateFlow<TransactionItem?> = _withdrawalSuccessReceipt.asStateFlow()

  private val _withdrawalError = MutableStateFlow<String?>(null)
  val withdrawalError: StateFlow<String?> = _withdrawalError.asStateFlow()

  private var adTimerJob: Job? = null

  init {
    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.let { user ->
      onUserAuthenticated(user)
    }

    viewModelScope.launch {
      repository.upiId.collect { savedUpi ->
        if (savedUpi.isNotBlank() && _payoutDestinationInput.value.isBlank()) {
          _payoutDestinationInput.value = savedUpi
        }
      }
    }
  }

  fun claimDailyReward() {
    val result = repository.claimDailyReward()
    result.onSuccess { reward ->
      _celebrationData.value = CelebrationData(
        title = "Streak Reward Claimed!",
        subtitle = "Day ${streakDays.value} check-in bonus added to your cloud wallet.",
        coinsEarned = reward
      )
    }.onFailure { err ->
      _celebrationData.value = CelebrationData(
        title = "Already Claimed Today",
        subtitle = err.message ?: "Come back tomorrow for Day ${streakDays.value + 1} reward!",
        coinsEarned = 0
      )
    }
  }

  fun dismissCelebration() {
    _celebrationData.value = null
  }

  fun dismissAdDialog() {
    _adWatchState.value = AdWatchState.Idle
  }

  /**
   * Watch & Earn: Shows real Google AdMob Rewarded Video Ad.
   * Only awards the +15 Firestore coins after the user successfully completes watching
   * the ad via onUserEarnedReward callback.
   * Shows a Toast/Snackbar if ad fails to load or device is offline.
   */
  fun showRewardedAd(activity: Activity) {
    AdManager.showRewardedAd(
      activity = activity,
      onUserEarnedReward = { rewardItem ->
        val reward = repository.creditWatchAdReward()
        _celebrationData.value = CelebrationData(
          title = "Ad Reward Credited! 🎬",
          subtitle = "You watched the sponsored video! +15 coins added to your cloud wallet.",
          coinsEarned = reward
        )
      },
      onAdDismissed = {
        // Handled in AdManager (preloads next ad)
      },
      onAdFailedToShow = { errorMsg ->
        // Error toast shown by AdManager; also provide clear user feedback
        _celebrationData.value = CelebrationData(
          title = "Ad Not Ready",
          subtitle = "$errorMsg\nPlease check your internet connection and try again.",
          coinsEarned = 0
        )
      }
    )
  }

  fun startWatchingAd(activity: Activity? = null) {
    if (activity != null) {
      showRewardedAd(activity)
    }
  }

  // Daily Spin Wheel
  fun spinLuckyWheel() {
    if (_spinWheelState.value is SpinWheelState.Spinning) return

    if (!repository.canSpinWheel()) {
      val remaining = repository.getSpinCooldownRemainingMillis()
      val hours = remaining / (3600 * 1000)
      val mins = (remaining % (3600 * 1000)) / (60 * 1000)
      _celebrationData.value = CelebrationData(
        title = "Spin on Cooldown ⏳",
        subtitle = "Daily Lucky Spin is available once every 24 hours. Next spin ready in ${hours}h ${mins}m.",
        coinsEarned = 0
      )
      return
    }

    val sectorIndex = wheelSectors.indices.random()
    val chosenSector = wheelSectors[sectorIndex]

    // Sector angle calculation: 8 sectors = 45 deg per sector
    val sectorAngle = 360f / wheelSectors.size
    // Pointer is at the top (270 deg or 0 deg depending on canvas orientation)
    // To land on sectorIndex at top (270 degrees):
    val targetBaseAngle = 360f - (sectorIndex * sectorAngle + sectorAngle / 2f) + 270f
    val fullRotations = 5 * 360f // 5 full exciting spins
    val totalTargetRotation = fullRotations + targetBaseAngle

    _spinWheelState.value = SpinWheelState.Spinning(
      targetRotation = totalTargetRotation,
      rewardSector = chosenSector
    )

    viewModelScope.launch {
      // Allow animation to complete (3.5 seconds)
      delay(3600)
      val result = repository.creditSpinReward(chosenSector.coins)
      result.onSuccess { coins ->
        _spinWheelState.value = SpinWheelState.Won(chosenSector)
        _celebrationData.value = CelebrationData(
          title = "Lucky Wheel Winner!",
          subtitle = "The wheel stopped at ${chosenSector.label}!",
          coinsEarned = coins
        )
      }.onFailure { err ->
        _spinWheelState.value = SpinWheelState.Idle
        _celebrationData.value = CelebrationData(
          title = "Spin Cooldown ⏳",
          subtitle = err.message ?: "Available every 24 hours.",
          coinsEarned = 0
        )
      }
    }
  }

  fun dismissSpinResult() {
    _spinWheelState.value = SpinWheelState.Idle
  }

  fun onShareCompleted() {
    val reward = repository.creditShareReward()
    _celebrationData.value = CelebrationData(
      title = "Referral Shared!",
      subtitle = "Bonus credited for spreading the word to friends.",
      coinsEarned = reward
    )
  }

  // Wallet methods
  fun setPayoutMethod(method: PayoutMethod) {
    _selectedPayoutMethod.value = method
    _withdrawalError.value = null
  }

  fun setPayoutDestination(input: String) {
    _payoutDestinationInput.value = input
    _withdrawalError.value = null
  }

  fun setSelectedWithdrawalCoins(coins: Int) {
    _selectedCoinsToWithdraw.value = coins
    _withdrawalError.value = null
  }

  fun requestWithdrawal() {
    val coins = _selectedCoinsToWithdraw.value
    val destination = _payoutDestinationInput.value.trim()
    val method = _selectedPayoutMethod.value

    if (coins < 500) {
      _withdrawalError.value = "Minimum withdrawal is 500 Coins (₹50.00)"
      return
    }

    if (walletBalance.value < coins) {
      _withdrawalError.value = "Insufficient balance. You need ${coins - walletBalance.value} more coins."
      return
    }

    if (destination.isEmpty()) {
      _withdrawalError.value = "Please enter your ${method.displayName} details"
      return
    }

    // Validate format
    when (method) {
      PayoutMethod.UPI -> {
        if (!destination.contains("@") || destination.length < 5) {
          _withdrawalError.value = "Please enter a valid UPI ID (e.g. yourname@upi)"
          return
        }
      }
      PayoutMethod.PAYTM -> {
        if (destination.length != 10 || !destination.all { it.isDigit() }) {
          _withdrawalError.value = "Please enter a valid 10-digit mobile number"
          return
        }
      }
      PayoutMethod.GIFT_CARD -> {
        if (!destination.contains("@") || !destination.contains(".")) {
          _withdrawalError.value = "Please enter a valid email address"
          return
        }
      }
    }

    _withdrawalProcessing.value = true
    _withdrawalError.value = null

    viewModelScope.launch {
      delay(1800) // Simulated secure payment gateway processing
      val result = repository.withdrawCash(coins, method, destination)
      _withdrawalProcessing.value = false
      result.onSuccess { tx ->
        _withdrawalSuccessReceipt.value = tx
        _payoutDestinationInput.value = ""
      }.onFailure { err ->
        _withdrawalError.value = err.message ?: "Withdrawal failed. Please try again."
      }
    }
  }

  fun dismissWithdrawalReceipt() {
    _withdrawalSuccessReceipt.value = null
  }

  fun resetToDemo() {
    repository.resetToDemoState()
    _payoutDestinationInput.value = ""
    _withdrawalError.value = null
    _withdrawalSuccessReceipt.value = null
  }

  fun onUserAuthenticated(firebaseUser: com.google.firebase.auth.FirebaseUser) {
    repository.updateUserProfile(
      uid = firebaseUser.uid,
      name = firebaseUser.displayName,
      email = firebaseUser.email,
      photoUrl = firebaseUser.photoUrl?.toString()
    )
  }

  fun onUserSignedOut() {
    repository.clearUserProfile()
  }
}
