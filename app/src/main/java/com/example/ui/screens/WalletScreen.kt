package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PayoutMethod
import com.example.model.TransactionItem
import com.example.model.TransactionStatus
import com.example.model.TransactionType
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
  walletBalance: Int,
  balanceRupees: Double,
  selectedMethod: PayoutMethod,
  destinationInput: String,
  selectedWithdrawalCoins: Int,
  isProcessing: Boolean,
  errorMessage: String?,
  transactions: List<TransactionItem>,
  onMethodSelect: (PayoutMethod) -> Unit,
  onDestinationChange: (String) -> Unit,
  onWithdrawalCoinsSelect: (Int) -> Unit,
  onRequestWithdrawal: () -> Unit,
  modifier: Modifier = Modifier
) {
  var txFilter by remember { mutableStateOf("ALL") }

  val minRequiredCoins = 500
  val hasMinCoins = walletBalance >= minRequiredCoins
  val progressToUnlock = (walletBalance.toFloat() / minRequiredCoins.toFloat()).coerceIn(0f, 1f)

  val filteredTransactions = remember(transactions, txFilter) {
    when (txFilter) {
      "CREDIT" -> transactions.filter { it.type == TransactionType.CREDIT }
      "DEBIT" -> transactions.filter { it.type == TransactionType.DEBIT }
      else -> transactions
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("wallet_screen"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Hero Balance Card
    item {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF131926),
        border = androidx.compose.foundation.BorderStroke(
          width = 1.5.dp,
          brush = Brush.horizontalGradient(listOf(GoldLight, GoldDark))
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("wallet_balance_card")
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF2A2000)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AccountBalanceWallet,
                  contentDescription = null,
                  tint = GoldPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Total Reward Balance",
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
              )
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFF241D08),
              border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark)
            ) {
              Text(
                text = "10 Coins = ₹1.00",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextGold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Main Coins & Rupees readout
          Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.MonetizationOn,
                  contentDescription = null,
                  tint = GoldPrimary,
                  modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "$walletBalance",
                  fontSize = 36.sp,
                  fontWeight = FontWeight.Black,
                  color = GoldLight,
                  letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Coins",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextSecondary,
                  modifier = Modifier.padding(bottom = 4.dp)
                )
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "CASH VALUE",
                fontSize = 10.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "₹${String.format(Locale.US, "%.2f", balanceRupees)}",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldGreen
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Minimum withdrawal progress / status
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = DarkElevated,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (hasMinCoins) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (hasMinCoins) EmeraldGreen else GoldPrimary,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (hasMinCoins) "Cashout Unlocked! ✓" else "Minimum Cashout: 500 Coins (₹50.00)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasMinCoins) EmeraldGreen else TextGold
                  )
                }

                Text(
                  text = "$walletBalance / $minRequiredCoins",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextSecondary
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              LinearProgressIndicator(
                progress = { progressToUnlock },
                color = if (hasMinCoins) EmeraldGreen else GoldPrimary,
                trackColor = Color(0xFF1E2638),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp))
              )

              if (!hasMinCoins) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Earn ${minRequiredCoins - walletBalance} more coins via Daily Streak or Tasks to unlock withdrawal.",
                  fontSize = 11.sp,
                  color = TextSecondary
                )
              }
            }
          }
        }
      }
    }

    // Payout & Withdrawal Action Card
    item {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Withdraw Funds",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Text(
            text = "Select your preferred payment method and cash out",
            fontSize = 12.sp,
            color = TextSecondary
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Payout Methods Tabs
          Text(
            text = "PAYOUT METHOD",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextGold,
            letterSpacing = 0.5.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            PayoutMethodTab(
              method = PayoutMethod.UPI,
              selected = selectedMethod == PayoutMethod.UPI,
              icon = Icons.Default.QrCode,
              onClick = { onMethodSelect(PayoutMethod.UPI) },
              modifier = Modifier.weight(1f)
            )
            PayoutMethodTab(
              method = PayoutMethod.PAYTM,
              selected = selectedMethod == PayoutMethod.PAYTM,
              icon = Icons.Default.PhoneAndroid,
              onClick = { onMethodSelect(PayoutMethod.PAYTM) },
              modifier = Modifier.weight(1f)
            )
            PayoutMethodTab(
              method = PayoutMethod.GIFT_CARD,
              selected = selectedMethod == PayoutMethod.GIFT_CARD,
              icon = Icons.Default.CardGiftcard,
              onClick = { onMethodSelect(PayoutMethod.GIFT_CARD) },
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Destination Input Field
          Text(
            text = selectedMethod.hintText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
          )

          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = destinationInput,
            onValueChange = onDestinationChange,
            placeholder = {
              Text(
                text = when (selectedMethod) {
                  PayoutMethod.UPI -> "e.g., yourname@okhdfcbank"
                  PayoutMethod.PAYTM -> "e.g., 9876543210"
                  PayoutMethod.GIFT_CARD -> "e.g., recipient@email.com"
                },
                fontSize = 13.sp,
                color = Color(0xFF64748B)
              )
            },
            keyboardOptions = KeyboardOptions(
              keyboardType = when (selectedMethod) {
                PayoutMethod.UPI -> KeyboardType.Email
                PayoutMethod.PAYTM -> KeyboardType.Phone
                PayoutMethod.GIFT_CARD -> KeyboardType.Email
              }
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkElevated,
              unfocusedContainerColor = DarkElevated,
              focusedBorderColor = GoldPrimary,
              unfocusedBorderColor = DarkBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("withdrawal_input_field")
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Amount Selector Chips
          Text(
            text = "SELECT AMOUNT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextGold,
            letterSpacing = 0.5.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          val amountOptions = listOf(500, 1000, 2000, 5000)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            amountOptions.forEach { coins ->
              val isSelected = selectedWithdrawalCoins == coins
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) Color(0xFF2A2000) else DarkElevated,
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) GoldPrimary else DarkBorder
                ),
                modifier = Modifier
                  .weight(1f)
                  .clickable { onWithdrawalCoinsSelect(coins) }
                  .testTag("amount_chip_$coins")
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 8.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "₹${coins / 10}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) GoldLight else TextPrimary
                  )
                  Text(
                    text = "$coins c",
                    fontSize = 10.sp,
                    color = TextSecondary
                  )
                }
              }
            }
          }

          // Error text if any
          if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = errorMessage,
                color = Color(0xFFEF4444),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Withdraw Cash CTA Button
          Button(
            onClick = onRequestWithdrawal,
            enabled = hasMinCoins && !isProcessing && destinationInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
              containerColor = GoldPrimary,
              contentColor = Color(0xFF1E1700),
              disabledContainerColor = Color(0xFF232A38),
              disabledContentColor = Color(0xFF64748B)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("withdraw_cash_button")
          ) {
            if (isProcessing) {
              CircularProgressIndicator(
                color = Color(0xFF1E1700),
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Processing Secure Transfer...",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
            } else {
              Icon(
                imageVector = if (hasMinCoins) Icons.Default.AccountBalanceWallet else Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (hasMinCoins) "Withdraw Cash (₹${selectedWithdrawalCoins / 10}.00) 💸" else "Withdraw Cash (Min 500 Coins Required)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // Transaction History Section
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Transaction History",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Text(
            text = "Recent credits, task rewards, and cashouts",
            fontSize = 12.sp,
            color = TextSecondary
          )
        }

        // Filter Pills (All / Credits / Debits)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          FilterPill(
            label = "All",
            selected = txFilter == "ALL",
            onClick = { txFilter = "ALL" }
          )
          FilterPill(
            label = "+ Earned",
            selected = txFilter == "CREDIT",
            onClick = { txFilter = "CREDIT" }
          )
          FilterPill(
            label = "- Payouts",
            selected = txFilter == "DEBIT",
            onClick = { txFilter = "DEBIT" }
          )
        }
      }
    }

    // Transaction List items or Empty state
    if (filteredTransactions.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = DarkSurface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              tint = Color(0xFF64748B),
              modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No transactions found",
              fontSize = 14.sp,
              color = TextSecondary
            )
          }
        }
      }
    } else {
      items(filteredTransactions, key = { it.id }) { tx ->
        TransactionItemCard(tx)
      }
    }
  }
}

@Composable
private fun PayoutMethodTab(
  method: PayoutMethod,
  selected: Boolean,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (selected) Color(0xFF2A2000) else DarkElevated,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (selected) GoldPrimary else DarkBorder
    ),
    modifier = modifier
      .clickable { onClick() }
      .testTag("payout_method_${method.name}")
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = method.displayName,
        tint = if (selected) GoldPrimary else TextSecondary,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = method.displayName,
        fontSize = 11.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = if (selected) GoldLight else TextSecondary
      )
    }
  }
}

@Composable
private fun FilterPill(
  label: String,
  selected: Boolean,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (selected) GoldPrimary else DarkElevated,
    modifier = Modifier.clickable { onClick() }
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = if (selected) Color(0xFF1E1700) else TextSecondary,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
  }
}

@Composable
private fun TransactionItemCard(tx: TransactionItem) {
  val isCredit = tx.type == TransactionType.CREDIT
  val formattedDate = remember(tx.timestampMillis) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    sdf.format(Date(tx.timestampMillis))
  }

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = DarkSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isCredit) Color(0xFF10281F) else Color(0xFF2A1517)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
            contentDescription = if (isCredit) "Earned" else "Withdrawn",
            tint = if (isCredit) EmeraldGreen else Color(0xFFEF4444),
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = tx.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "$formattedDate • ${tx.subtitle}",
            fontSize = 11.sp,
            color = TextSecondary,
            maxLines = 1
          )
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = if (isCredit) "+${tx.amountCoins} Coins" else "-${tx.amountCoins} Coins",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = if (isCredit) GoldLight else Color(0xFFEF4444)
        )
        Text(
          text = if (isCredit) "+₹${String.format(Locale.US, "%.2f", tx.amountRupees)}" else "-₹${String.format(Locale.US, "%.2f", tx.amountRupees)}",
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = if (isCredit) EmeraldGreen else Color(0xFFEF4444)
        )
      }
    }
  }
}
