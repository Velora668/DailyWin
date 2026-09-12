package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WheelSector
import com.example.ui.SpinWheelState
import com.example.ui.components.LuckyWheelView
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
fun LuckyWheelScreen(
  sectors: List<WheelSector>,
  wheelState: SpinWheelState,
  spinsRemaining: Int,
  onSpinClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("lucky_wheel_screen"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header Hero Banner
    item {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A2000)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Casino,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Daily Lucky Spin",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = "Every spin is a guaranteed winner (5 - 50 Coins)",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }
          }
        }
      }
    }

    // Lucky Wheel Canvas & Controls
    item {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier.padding(vertical = 20.dp, horizontal = 12.dp),
          contentAlignment = Alignment.Center
        ) {
          LuckyWheelView(
            sectors = sectors,
            wheelState = wheelState,
            spinsRemaining = spinsRemaining,
            onSpinClick = onSpinClick
          )
        }
      }
    }

    // Rules & Prize Tiers Card
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Prize Tiers & Multipliers",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextGold
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            PrizeTierBadge(coins = "50 Coins", title = "Grand Jackpot", color = GoldPrimary)
            PrizeTierBadge(coins = "40 Coins", title = "Diamond Win", color = Color(0xFF06B6D4))
            PrizeTierBadge(coins = "30 Coins", title = "Gold Tier", color = Color(0xFFF97316))
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = TextSecondary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Free daily spins refresh automatically at midnight.",
              fontSize = 11.sp,
              color = TextSecondary
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun PrizeTierBadge(
  coins: String,
  title: String,
  color: Color
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = DarkElevated,
    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = coins,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = color
      )
      Text(
        text = title,
        fontSize = 10.sp,
        color = TextSecondary
      )
    }
  }
}
