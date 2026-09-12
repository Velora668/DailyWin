package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.AdManager
import com.example.ui.DailyWinViewModel
import com.example.ui.components.CelebrationDialog
import com.example.ui.components.DailyWinTopBar
import com.example.ui.components.ProfileDialog
import com.example.ui.components.RewardedAdDialog
import com.example.ui.components.WithdrawalReceiptDialog
import com.example.ui.screens.EarnScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.LuckyWheelScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {

  private val viewModel: DailyWinViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AdManager.initialize(this)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        DailyWinApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun DailyWinApp(viewModel: DailyWinViewModel) {
  val firebaseAuth = remember { FirebaseAuth.getInstance() }
  var currentUser by remember { mutableStateOf(firebaseAuth.currentUser) }

  LaunchedEffect(currentUser) {
    currentUser?.let { user ->
      viewModel.onUserAuthenticated(user)
    }
  }

  Crossfade(
    targetState = currentUser != null,
    label = "authGateCrossfade"
  ) { isAuthenticated ->
    if (isAuthenticated) {
      DailyWinDashboard(
        viewModel = viewModel,
        onSignOut = {
          firebaseAuth.signOut()
          viewModel.onUserSignedOut()
          currentUser = null
        }
      )
    } else {
      LoginScreen(
        onSignInSuccess = { user ->
          viewModel.onUserAuthenticated(user)
          currentUser = user
        }
      )
    }
  }
}

@Composable
fun DailyWinDashboard(
  viewModel: DailyWinViewModel,
  onSignOut: () -> Unit
) {
  val context = LocalContext.current
  val activity = context as? Activity

  var selectedTab by rememberSaveable { mutableIntStateOf(0) }
  var showProfileDialog by rememberSaveable { mutableStateOf(false) }

  val walletBalance by viewModel.walletBalance.collectAsStateWithLifecycle()
  val balanceRupees by viewModel.balanceInRupees.collectAsStateWithLifecycle()
  val streakDays by viewModel.streakDays.collectAsStateWithLifecycle()
  val isTodayClaimed by viewModel.isTodayClaimed.collectAsStateWithLifecycle()
  val streakItems by viewModel.dailyStreakList.collectAsStateWithLifecycle()
  val adWatchState by viewModel.adWatchState.collectAsStateWithLifecycle()
  val spinWheelState by viewModel.spinWheelState.collectAsStateWithLifecycle()
  val spinsRemaining by viewModel.spinsRemaining.collectAsStateWithLifecycle()
  val celebrationData by viewModel.celebrationData.collectAsStateWithLifecycle()
  val transactions by viewModel.transactions.collectAsStateWithLifecycle()
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

  val selectedPayoutMethod by viewModel.selectedPayoutMethod.collectAsStateWithLifecycle()
  val payoutDestinationInput by viewModel.payoutDestinationInput.collectAsStateWithLifecycle()
  val selectedCoinsToWithdraw by viewModel.selectedCoinsToWithdraw.collectAsStateWithLifecycle()
  val isWithdrawing by viewModel.withdrawalProcessing.collectAsStateWithLifecycle()
  val withdrawalReceipt by viewModel.withdrawalSuccessReceipt.collectAsStateWithLifecycle()
  val withdrawalError by viewModel.withdrawalError.collectAsStateWithLifecycle()

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkBackground),
    topBar = {
      DailyWinTopBar(
        walletBalance = walletBalance,
        balanceInRupees = balanceRupees,
        userProfile = userProfile,
        onWalletBadgeClick = { selectedTab = 2 },
        onProfileClick = { showProfileDialog = true }
      )
    },
    bottomBar = {
      NavigationBar(
        windowInsets = WindowInsets.navigationBars,
        containerColor = DarkSurface,
        tonalElevation = 8.dp,
        modifier = Modifier
          .border(1.dp, DarkBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
          .testTag("bottom_navigation_bar")
      ) {
        // Tab 0: Earn
        NavigationBarItem(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 0) Icons.Default.EmojiEvents else Icons.Outlined.EmojiEvents,
              contentDescription = "Earn"
            )
          },
          label = {
            Text(
              text = "Earn",
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
              fontSize = 11.sp
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF1E1700),
            selectedTextColor = GoldLight,
            indicatorColor = GoldPrimary,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
          ),
          modifier = Modifier.testTag("nav_tab_earn")
        )

        // Tab 1: Lucky Spin
        NavigationBarItem(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 1) Icons.Default.Casino else Icons.Outlined.Casino,
              contentDescription = "Lucky Spin"
            )
          },
          label = {
            Text(
              text = "Spin Wheel",
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
              fontSize = 11.sp
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF1E1700),
            selectedTextColor = GoldLight,
            indicatorColor = GoldPrimary,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
          ),
          modifier = Modifier.testTag("nav_tab_spin")
        )

        // Tab 2: Wallet & Withdrawal
        NavigationBarItem(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 2) Icons.Default.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
              contentDescription = "Wallet"
            )
          },
          label = {
            Text(
              text = "Wallet",
              fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
              fontSize = 11.sp
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF1E1700),
            selectedTextColor = GoldLight,
            indicatorColor = GoldPrimary,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
          ),
          modifier = Modifier.testTag("nav_tab_wallet")
        )
      }
    },
    containerColor = DarkBackground
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Crossfade(
        targetState = selectedTab,
        label = "tabTransition"
      ) { tabIndex ->
        when (tabIndex) {
          0 -> EarnScreen(
            streakDays = streakDays,
            isTodayClaimed = isTodayClaimed,
            streakItems = streakItems,
            referralCode = userProfile.referralCode,
            onClaimDailyReward = { viewModel.claimDailyReward() },
            onWatchAdClick = {
              if (activity != null) {
                viewModel.showRewardedAd(activity)
              } else {
                viewModel.startWatchingAd()
              }
            },
            onSpinWheelClick = { selectedTab = 1 },
            onShareReturn = { viewModel.onShareCompleted() }
          )

          1 -> LuckyWheelScreen(
            sectors = viewModel.wheelSectors,
            wheelState = spinWheelState,
            spinsRemaining = spinsRemaining,
            onSpinClick = { viewModel.spinLuckyWheel() }
          )

          2 -> WalletScreen(
            walletBalance = walletBalance,
            balanceRupees = balanceRupees,
            selectedMethod = selectedPayoutMethod,
            destinationInput = payoutDestinationInput,
            selectedWithdrawalCoins = selectedCoinsToWithdraw,
            isProcessing = isWithdrawing,
            errorMessage = withdrawalError,
            transactions = transactions,
            onMethodSelect = { viewModel.setPayoutMethod(it) },
            onDestinationChange = { viewModel.setPayoutDestination(it) },
            onWithdrawalCoinsSelect = { viewModel.setSelectedWithdrawalCoins(it) },
            onRequestWithdrawal = { viewModel.requestWithdrawal() }
          )
        }
      }
    }
  }

  // 1. Rewarded Ad Dialog (3s simulated video ad player)
  RewardedAdDialog(
    adState = adWatchState,
    onDismiss = { viewModel.dismissAdDialog() }
  )

  // 2. Animated Reward Celebration Dialog
  celebrationData?.let { data ->
    CelebrationDialog(
      data = data,
      onDismiss = { viewModel.dismissCelebration() }
    )
  }

  // 3. Withdrawal Success Receipt Dialog
  withdrawalReceipt?.let { tx ->
    WithdrawalReceiptDialog(
      transaction = tx,
      onDismiss = { viewModel.dismissWithdrawalReceipt() }
    )
  }

  // 4. User Profile Dialog
  if (showProfileDialog) {
    ProfileDialog(
      profile = userProfile,
      walletBalance = walletBalance,
      balanceRupees = balanceRupees,
      onResetDemoData = { viewModel.resetToDemo() },
      onSignOut = onSignOut,
      onDismiss = { showProfileDialog = false }
    )
  }
}

// Keep Greeting for GreetingScreenshotTest backward compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}
