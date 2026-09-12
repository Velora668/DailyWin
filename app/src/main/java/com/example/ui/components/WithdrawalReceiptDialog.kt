package com.example.ui.components

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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.TransactionItem
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
fun WithdrawalReceiptDialog(
  transaction: TransactionItem,
  onDismiss: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(26.dp))
        .background(Color(0xFF131824))
        .border(1.5.dp, EmeraldGreen, RoundedCornerShape(26.dp))
        .padding(22.dp)
        .testTag("withdrawal_receipt_dialog")
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color(0xFF10281F)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = EmeraldGreen,
            modifier = Modifier.size(44.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Withdrawal Initiated!",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Your payout request has been queued securely. Cash typically reaches your account within 2-4 hours.",
          fontSize = 13.sp,
          color = TextSecondary,
          textAlign = TextAlign.Center,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Receipt Details Box
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = DarkElevated,
          border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            ReceiptRow(
              label = "Payout Amount",
              value = "₹${String.format(Locale.US, "%.2f", transaction.amountRupees)} (${transaction.amountCoins} Coins)",
              valueColor = EmeraldGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            ReceiptRow(
              label = "Method",
              value = transaction.method?.displayName ?: "Direct Transfer",
              valueColor = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            ReceiptRow(
              label = "Status",
              value = "Processing",
              valueColor = GoldLight
            )
            if (transaction.referenceId != null) {
              Spacer(modifier = Modifier.height(8.dp))
              ReceiptRow(
                label = "Reference ID",
                value = "#${transaction.referenceId}",
                valueColor = TextGold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = Color(0xFF1E1700)
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("dismiss_receipt_button")
        ) {
          Text(
            text = "Done & View Wallet",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
private fun ReceiptRow(
  label: String,
  value: String,
  valueColor: Color
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      fontSize = 13.sp,
      color = TextSecondary
    )
    Text(
      text = value,
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      color = valueColor
    )
  }
}
