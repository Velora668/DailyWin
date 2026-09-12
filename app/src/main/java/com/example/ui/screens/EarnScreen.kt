package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.DayStreakItem
import com.example.ui.components.DailyStreakCard
import com.example.ui.components.TaskEarnFeed

@Composable
fun EarnScreen(
  streakDays: Int,
  isTodayClaimed: Boolean,
  streakItems: List<DayStreakItem>,
  referralCode: String,
  onClaimDailyReward: () -> Unit,
  onWatchAdClick: () -> Unit,
  onSpinWheelClick: () -> Unit,
  onShareReturn: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("earn_screen"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // 1. Daily Streak & Check-In Card
    item {
      DailyStreakCard(
        streakDays = streakDays,
        isTodayClaimed = isTodayClaimed,
        streakItems = streakItems,
        onClaimClick = onClaimDailyReward
      )
    }

    // 2. Task & Earn Feed (Watch & Earn, Spin Wheel, Share & Earn)
    item {
      TaskEarnFeed(
        onWatchAdClick = onWatchAdClick,
        onSpinWheelClick = onSpinWheelClick,
        onShareReturn = onShareReturn,
        referralCode = referralCode
      )
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
