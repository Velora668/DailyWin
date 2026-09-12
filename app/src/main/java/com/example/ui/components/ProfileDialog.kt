package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
fun ProfileDialog(
  profile: UserProfile,
  walletBalance: Int,
  balanceRupees: Double,
  onResetDemoData: () -> Unit,
  onSignOut: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(26.dp))
        .background(DarkBackground)
        .border(1.2.dp, DarkBorder, RoundedCornerShape(26.dp))
        .padding(22.dp)
        .testTag("profile_dialog")
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Top row with Close
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "User Profile",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp).testTag("close_profile_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = TextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Avatar (Google Profile Image or Initial)
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(
              brush = Brush.linearGradient(listOf(GoldLight, GoldDark))
            )
            .border(2.dp, GoldPrimary, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          if (!profile.photoUrl.isNullOrBlank()) {
            AsyncImage(
              model = profile.photoUrl,
              contentDescription = "Google Profile Picture",
              modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
              contentScale = ContentScale.Crop
            )
          } else {
            Text(
              text = profile.avatarLetter,
              fontSize = 32.sp,
              fontWeight = FontWeight.Black,
              color = Color(0xFF1E1700)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = profile.name,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )

        if (!profile.email.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Email,
              contentDescription = null,
              tint = TextSecondary,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = profile.email,
              fontSize = 12.sp,
              color = TextSecondary
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = EmeraldGreen,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Verified Google Member",
            fontSize = 12.sp,
            color = EmeraldGreen,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lifetime Earnings Grid
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ProfileStatCard(
            label = "Current Coins",
            value = "$walletBalance",
            icon = Icons.Default.MonetizationOn,
            iconTint = GoldPrimary,
            modifier = Modifier.weight(1f)
          )
          ProfileStatCard(
            label = "Cash Value",
            value = "₹${String.format(Locale.US, "%.2f", balanceRupees)}",
            icon = Icons.Default.EmojiEvents,
            iconTint = EmeraldGreen,
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Referral Box
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = DarkElevated,
          border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Referral Code",
                fontSize = 11.sp,
                color = TextSecondary
              )
              Text(
                text = profile.referralCode,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextGold
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF2A2000),
              border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark),
              modifier = Modifier.clickable {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("DailyWin Referral", profile.referralCode))
                Toast.makeText(context, "Referral Code copied to clipboard", Toast.LENGTH_SHORT).show()
              }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sign Out Button
        Button(
          onClick = {
            onSignOut()
            onDismiss()
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3B1818),
            contentColor = Color(0xFFFF7171)
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6E2828)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("sign_out_button")
        ) {
          Icon(
            imageVector = Icons.Default.Logout,
            contentDescription = "Sign Out",
            tint = Color(0xFFFF7171),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Sign Out of DailyWin",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF7171)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reset Demo Data button
        OutlinedButton(
          onClick = {
            onResetDemoData()
            Toast.makeText(context, "Demo state reset to 250 coins & Day 3 streak", Toast.LENGTH_SHORT).show()
            onDismiss()
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
          border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("reset_demo_button")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Reset Demo Data (250 Coins)",
            fontSize = 13.sp
          )
        }
      }
    }
  }
}

@Composable
private fun ProfileStatCard(
  label: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = DarkElevated,
    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
      Text(
        text = label,
        fontSize = 11.sp,
        color = TextSecondary
      )
    }
  }
}
