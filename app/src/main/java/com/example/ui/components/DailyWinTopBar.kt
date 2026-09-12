package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.model.UserProfile
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun DailyWinTopBar(
  walletBalance: Int,
  balanceInRupees: Double,
  userProfile: UserProfile = UserProfile(),
  onWalletBadgeClick: () -> Unit,
  onProfileClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "coinShine")
  val coinGlowScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "coinGlow"
  )

  Surface(
    color = DarkBackground,
    modifier = modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .testTag("dailywin_top_bar")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: App Title & Branding
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        // DailyWin Trophy Icon Badge
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              brush = Brush.linearGradient(
                colors = listOf(GoldLight, GoldDark)
              )
            )
        ) {
          Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = "DailyWin Logo",
            tint = Color(0xFF1E1700),
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Daily",
              color = TextPrimary,
              fontSize = 20.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = (-0.5).sp
            )
            Text(
              text = "Win",
              color = GoldPrimary,
              fontSize = 20.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = (-0.5).sp
            )
          }
          Text(
            text = "REWARDS & CASHOUT",
            color = TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
        }
      }

      // Right: Live Coin Wallet Badge & User Profile
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Live Coin Wallet Badge
        Box(
          modifier = Modifier
            .scale(coinGlowScale)
            .clip(RoundedCornerShape(20.dp))
            .background(
              brush = Brush.horizontalGradient(
                colors = listOf(
                  Color(0xFF2E2405),
                  Color(0xFF1B1812)
                )
              )
            )
            .border(
              width = 1.2.dp,
              brush = Brush.horizontalGradient(
                colors = listOf(GoldLight, GoldDark)
              ),
              shape = RoundedCornerShape(20.dp)
            )
            .clickable { onWalletBadgeClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("coin_wallet_badge"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.MonetizationOn,
              contentDescription = "Coins",
              tint = GoldPrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "$walletBalance",
              color = GoldLight,
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "•",
              color = Color(0xFF64748B),
              fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "₹${String.format(Locale.US, "%.2f", balanceInRupees)}",
              color = EmeraldGreen,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Profile Avatar Button
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(DarkElevated)
            .border(1.5.dp, GoldPrimary, CircleShape)
            .clickable { onProfileClick() }
            .testTag("profile_button"),
          contentAlignment = Alignment.Center
        ) {
          if (!userProfile.photoUrl.isNullOrBlank()) {
            AsyncImage(
              model = userProfile.photoUrl,
              contentDescription = "User Profile Photo",
              modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
              contentScale = ContentScale.Crop
            )
          } else if (userProfile.avatarLetter.isNotBlank()) {
            Text(
              text = userProfile.avatarLetter,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = GoldLight
            )
          } else {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "User Profile",
              tint = GoldLight,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}
