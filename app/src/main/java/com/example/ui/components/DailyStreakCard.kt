package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DayStreakItem
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DailyStreakCard(
  streakDays: Int,
  isTodayClaimed: Boolean,
  streakItems: List<DayStreakItem>,
  onClaimClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val claimScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.03f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "claimScale"
  )

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = DarkSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
    modifier = modifier
      .fillMaxWidth()
      .testTag("daily_streak_card")
  ) {
    Column(
      modifier = Modifier.padding(18.dp)
    ) {
      // Header with Flame
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(Color(0xFF3B1E08)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Whatshot,
              contentDescription = "Streak Flame",
              tint = Color(0xFFFF7A00),
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Daily Streak & Check-In",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "Claim daily to unlock mega rewards",
              fontSize = 12.sp,
              color = TextSecondary
            )
          }
        }

        // Streak Count Badge
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF2A2000),
          border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark)
        ) {
          Text(
            text = "🔥 Day $streakDays Streak",
            color = GoldLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 7-day Tracker Horizontal Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        streakItems.forEach { dayItem ->
          DayStreakBubble(
            item = dayItem,
            modifier = Modifier.weight(1f)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Claim Button
      if (!isTodayClaimed) {
        Button(
          onClick = onClaimClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = Color(0xFF1E1700)
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .scale(claimScale)
            .testTag("claim_daily_reward_button")
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Claim Daily Reward (+20 Coins)",
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      } else {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = DarkElevated,
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B3A30)),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = EmeraldGreen,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Claimed Today ✓ • Next Check-In Tomorrow",
              color = EmeraldGreen,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DayStreakBubble(
  item: DayStreakItem,
  modifier: Modifier = Modifier
) {
  val isSpecialMegaDay = item.dayNumber == 7

  val borderColor = when {
    item.isToday -> GoldPrimary
    item.isClaimed -> EmeraldGreen
    else -> DarkBorder
  }

  val bgColor = when {
    item.isToday -> Color(0xFF2E2405)
    item.isClaimed -> Color(0xFF11261B)
    else -> DarkElevated
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.padding(horizontal = 2.dp)
  ) {
    Text(
      text = "D${item.dayNumber}",
      fontSize = 11.sp,
      fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Medium,
      color = if (item.isToday) GoldLight else TextSecondary
    )

    Spacer(modifier = Modifier.height(6.dp))

    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(bgColor)
        .border(
          width = if (item.isToday) 2.dp else 1.dp,
          color = borderColor,
          shape = RoundedCornerShape(10.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      when {
        item.isClaimed -> {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Claimed",
            tint = EmeraldGreen,
            modifier = Modifier.size(18.dp)
          )
        }
        item.isToday -> {
          Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = "Reward Ready",
            tint = GoldPrimary,
            modifier = Modifier.size(20.dp)
          )
        }
        else -> {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            tint = Color(0xFF4A5568),
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = "+${item.coinsReward}",
      fontSize = 10.sp,
      fontWeight = FontWeight.SemiBold,
      color = if (isSpecialMegaDay) GoldLight else TextSecondary
    )
  }
}
