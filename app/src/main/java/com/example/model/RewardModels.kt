package com.example.model

enum class TransactionType {
  CREDIT,
  DEBIT
}

enum class TransactionStatus {
  COMPLETED,
  PROCESSING,
  FAILED
}

enum class PayoutMethod(val displayName: String, val hintText: String, val iconResName: String) {
  UPI("UPI ID", "Enter UPI ID (e.g., name@okhdfcbank)", "ic_upi"),
  PAYTM("Paytm Wallet", "Enter 10-digit Paytm mobile number", "ic_paytm"),
  GIFT_CARD("Gift Card", "Enter delivery email address", "ic_gift")
}

data class TransactionItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val amountCoins: Int,
  val amountRupees: Double,
  val type: TransactionType,
  val timestampMillis: Long,
  val status: TransactionStatus,
  val method: PayoutMethod? = null,
  val referenceId: String? = null
)

data class DayStreakItem(
  val dayNumber: Int,
  val coinsReward: Int,
  val isClaimed: Boolean,
  val isToday: Boolean,
  val isLocked: Boolean
)

data class WheelSector(
  val coins: Int,
  val label: String,
  val colorHex: Long
)

data class UserProfile(
  val uid: String? = null,
  val name: String = "Lucky Winner",
  val email: String? = null,
  val photoUrl: String? = null,
  val avatarLetter: String = "W",
  val referralCode: String = "WIN888",
  val lifetimeCoinsEarned: Int = 450,
  val totalWithdrawnRupees: Double = 0.0,
  val streakDays: Int = 3
)
