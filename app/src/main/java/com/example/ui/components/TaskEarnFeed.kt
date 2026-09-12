package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun TaskEarnFeed(
  onWatchAdClick: () -> Unit,
  onSpinWheelClick: () -> Unit,
  onShareReturn: () -> Unit,
  referralCode: String = "WIN888",
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  val shareLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) {
    onShareReturn()
  }

  Column(modifier = modifier.fillMaxWidth()) {
    // Section Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Task & Earn Feed",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "Complete high-reward tasks to earn coins",
          fontSize = 12.sp,
          color = TextSecondary
        )
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1B2232),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
      ) {
        Text(
          text = "3 Tasks Available",
          color = TextGold,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // CARD 1: Watch & Earn
    TaskFeedCard(
      tag = "REWARDED VIDEO",
      tagColor = Color(0xFF3B82F6),
      coinReward = "+15 Coins",
      cashEquivalent = "₹1.50",
      title = "Watch & Earn",
      description = "Watch an official Google AdMob sponsored rewarded video to earn 15 instant coins.",
      icon = Icons.Default.SmartDisplay,
      iconGradient = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
      buttonText = "Watch Video (+15) ▶",
      testTag = "task_watch_earn_card",
      buttonTestTag = "watch_ad_button",
      onActionClick = onWatchAdClick
    )

    Spacer(modifier = Modifier.height(14.dp))

    // CARD 2: Daily Spin Wheel
    TaskFeedCard(
      tag = "DAILY LUCKY WHEEL",
      tagColor = GoldPrimary,
      coinReward = "5 - 50 Coins",
      cashEquivalent = "Up to ₹5.00",
      title = "Daily Spin Wheel",
      description = "Spin the interactive lucky wheel to win between 5 and 50 coins right away.",
      icon = Icons.Default.Casino,
      iconGradient = listOf(GoldLight, GoldDark),
      buttonText = "Spin Lucky Wheel 🎯",
      testTag = "task_spin_wheel_card",
      buttonTestTag = "spin_feed_button",
      onActionClick = onSpinWheelClick
    )

    Spacer(modifier = Modifier.height(14.dp))

    // CARD 3: Share & Earn (Native Share Intent)
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = DarkSurface,
      border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("task_share_earn_card")
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF132B20)
          ) {
            Text(
              text = "HIGHEST REWARD",
              color = EmeraldGreen,
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 0.5.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.MonetizationOn,
              contentDescription = null,
              tint = GoldPrimary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "+50 Coins",
              color = GoldLight,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "(₹5.00)",
              color = EmeraldGreen,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.Top) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                brush = Brush.linearGradient(
                  colors = listOf(Color(0xFF10B981), Color(0xFF047857))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "Share & Earn",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = "Open native Android share sheet with your invite link. Adds +50 coins when you return!",
              fontSize = 12.sp,
              color = TextSecondary,
              lineHeight = 18.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Referral Code Chip with Copy action
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "YOUR REFERRAL CODE",
              fontSize = 9.sp,
              color = TextSecondary,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = referralCode,
              fontSize = 15.sp,
              color = TextGold,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 1.sp
            )
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF2A2000),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark),
            modifier = Modifier.clickable {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              clipboard.setPrimaryClip(ClipData.newPlainText("DailyWin Referral Code", referralCode))
              Toast.makeText(context, "Referral code copied!", Toast.LENGTH_SHORT).show()
            }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = GoldLight,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Copy",
                color = GoldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Native Share Trigger Button
        Button(
          onClick = {
            val shareText = "Hey! I'm earning real cash on DailyWin! Use my referral code $referralCode to get 100 free bonus coins upon joining! 💰🚀 Download now: https://dailywin.app/ref/$referralCode"
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
              putExtra(Intent.EXTRA_TEXT, shareText)
              type = "text/plain"
            }
            val chooser = Intent.createChooser(sendIntent, "Share DailyWin with Friends")
            shareLauncher.launch(chooser)
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = Color(0xFF1E1700)
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("share_earn_button")
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Share With Friends (+50 Coins)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
private fun TaskFeedCard(
  tag: String,
  tagColor: Color,
  coinReward: String,
  cashEquivalent: String,
  title: String,
  description: String,
  icon: ImageVector,
  iconGradient: List<Color>,
  buttonText: String,
  testTag: String,
  buttonTestTag: String,
  onActionClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = DarkSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTag)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      // Header tag and coins
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = tagColor.copy(alpha = 0.15f)
        ) {
          Text(
            text = tag,
            color = tagColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = null,
            tint = GoldPrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = coinReward,
            color = GoldLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "($cashEquivalent)",
            color = EmeraldGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(verticalAlignment = Alignment.Top) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush = Brush.linearGradient(colors = iconGradient)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = description,
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 18.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = onActionClick,
        colors = ButtonDefaults.buttonColors(
          containerColor = GoldPrimary,
          contentColor = Color(0xFF1E1700)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp)
          .testTag(buttonTestTag)
      ) {
        Text(
          text = buttonText,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
