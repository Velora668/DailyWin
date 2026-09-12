package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.AdWatchState
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RewardedAdDialog(
  adState: AdWatchState,
  onDismiss: () -> Unit
) {
  if (adState !is AdWatchState.Watching && adState !is AdWatchState.Completed) return

  val progress = when (adState) {
    is AdWatchState.Watching -> adState.progress
    is AdWatchState.Completed -> 1f
    else -> 0f
  }

  val secondsLeft = when (adState) {
    is AdWatchState.Watching -> adState.secondsRemaining
    is AdWatchState.Completed -> 0
    else -> 3
  }

  val isCompleted = adState is AdWatchState.Completed

  Dialog(
    onDismissRequest = {
      if (isCompleted) onDismiss()
    },
    properties = DialogProperties(dismissOnBackPress = isCompleted, dismissOnClickOutside = isCompleted)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(DarkBackground)
        .border(1.5.dp, if (isCompleted) EmeraldGreen else GoldDark, RoundedCornerShape(24.dp))
        .padding(20.dp)
        .testTag("rewarded_ad_modal")
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = GoldPrimary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "SPONSORED REWARD AD",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TextGold,
              letterSpacing = 1.sp
            )
          }

          if (isCompleted) {
            IconButton(
              onClick = onDismiss,
              modifier = Modifier.size(32.dp).testTag("close_ad_button")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Ad",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
              )
            }
          } else {
            // Countdown badge
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF2A2000),
              border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark)
            ) {
              Text(
                text = "${secondsLeft}s left",
                color = GoldLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulated Ad Screen Container
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
              brush = Brush.verticalGradient(
                colors = listOf(
                  Color(0xFF1E2638),
                  Color(0xFF111726)
                )
              )
            )
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
          ) {
            if (!isCompleted) {
              CircularProgressIndicator(
                progress = { progress },
                color = GoldPrimary,
                trackColor = Color(0xFF2B354D),
                strokeWidth = 4.dp,
                modifier = Modifier.size(56.dp)
              )
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Watching Sponsored Video...",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Stay for 3 seconds to claim +15 coins",
                color = TextSecondary,
                fontSize = 12.sp
              )
            } else {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Verified",
                tint = EmeraldGreen,
                modifier = Modifier.size(64.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Reward Verified!",
                color = EmeraldGreen,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "+15 Coins added to your wallet",
                color = TextGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Linear Progress Bar
        LinearProgressIndicator(
          progress = { progress },
          color = if (isCompleted) EmeraldGreen else GoldPrimary,
          trackColor = Color(0xFF222B3D),
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ad Sponsor Card footer
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = DarkSurface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF2B354D)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "PlayZone Rewards Partner",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Official verified sponsor ad",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }
          }
        }
      }
    }
  }
}
