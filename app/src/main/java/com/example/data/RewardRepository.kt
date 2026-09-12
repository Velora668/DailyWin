package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.model.PayoutMethod
import com.example.model.TransactionItem
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import com.example.model.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class RewardRepository(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("dailywin_prefs", Context.MODE_PRIVATE)

  private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
  private var userDocListener: ListenerRegistration? = null
  private var historyDocListener: ListenerRegistration? = null
  private var activeUid: String? = null

  private val _walletBalance = MutableStateFlow(50)
  val walletBalance: StateFlow<Int> = _walletBalance.asStateFlow()

  private val _streakDays = MutableStateFlow(1)
  val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

  private val _lastCheckInDate = MutableStateFlow("")
  val lastCheckInDate: StateFlow<String> = _lastCheckInDate.asStateFlow()

  private val _isTodayClaimed = MutableStateFlow(false)
  val isTodayClaimed: StateFlow<Boolean> = _isTodayClaimed.asStateFlow()

  private val _lastSpinTime = MutableStateFlow(0L)
  val lastSpinTime: StateFlow<Long> = _lastSpinTime.asStateFlow()

  private val _spinsRemaining = MutableStateFlow(1)
  val spinsRemaining: StateFlow<Int> = _spinsRemaining.asStateFlow()

  private val _upiId = MutableStateFlow("")
  val upiId: StateFlow<String> = _upiId.asStateFlow()

  private val _transactions = MutableStateFlow<List<TransactionItem>>(emptyList())
  val transactions: StateFlow<List<TransactionItem>> = _transactions.asStateFlow()

  private val _userProfile = MutableStateFlow(UserProfile())
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  init {
    loadFromPrefs()
  }

  private fun loadFromPrefs() {
    val initialBalance = prefs.getInt(KEY_BALANCE, 50)
    val initialStreak = prefs.getInt(KEY_STREAK, 1)
    val savedCheckIn = prefs.getString(KEY_LAST_CHECKIN, "") ?: ""
    val savedSpinTime = prefs.getLong(KEY_LAST_SPIN_TIME, 0L)
    val savedUpi = prefs.getString(KEY_UPI_ID, "") ?: ""

    _walletBalance.value = initialBalance
    _streakDays.value = initialStreak
    _lastCheckInDate.value = savedCheckIn
    _lastSpinTime.value = savedSpinTime
    _upiId.value = savedUpi

    val todayStr = getTodayDateString()
    _isTodayClaimed.value = (savedCheckIn == todayStr)

    val now = System.currentTimeMillis()
    val canSpin = (savedSpinTime == 0L || (now - savedSpinTime) >= 24 * 60 * 60 * 1000L)
    _spinsRemaining.value = if (canSpin) 1 else 0

    val txJson = prefs.getString(KEY_TRANSACTIONS, null)
    _transactions.value = if (txJson != null) deserializeTransactions(txJson) else createInitialTransactions()

    val savedName = prefs.getString(KEY_USER_NAME, null)
    if (savedName != null) {
      val savedUid = prefs.getString(KEY_USER_UID, null)
      val savedEmail = prefs.getString(KEY_USER_EMAIL, null)
      val savedPhoto = prefs.getString(KEY_USER_PHOTO, null)
      val letter = savedName.firstOrNull()?.uppercase() ?: "U"
      _userProfile.value = UserProfile(
        uid = savedUid,
        name = savedName,
        email = savedEmail,
        photoUrl = savedPhoto,
        avatarLetter = letter
      )
      if (!savedUid.isNullOrBlank()) {
        attachUser(savedUid)
      }
    }
  }

  /**
   * Attaches real-time Firestore synchronization for the user's document
   * and history subcollection.
   */
  fun attachUser(uid: String) {
    if (activeUid == uid && userDocListener != null) return
    detachUser()
    activeUid = uid

    try {
      val userDocRef = firestore.collection("users").document(uid)

      // 1. First-time check: if document does not exist, initialize with 50 welcome bonus coins
      userDocRef.get().addOnSuccessListener { snapshot ->
        if (!snapshot.exists()) {
          val initialUserData = hashMapOf(
            "coins" to 50L,
            "streakDays" to 1,
            "lastCheckInDate" to "",
            "lastSpinTime" to 0L,
            "upiId" to ""
          )
          userDocRef.set(initialUserData).addOnSuccessListener {
            // Append welcome bonus record to history
            userDocRef.collection("history").add(
              hashMapOf(
                "title" to "Welcome Bonus",
                "amount" to 50L,
                "type" to "CREDIT",
                "timestamp" to FieldValue.serverTimestamp()
              )
            )
          }.addOnFailureListener { err ->
            Log.e("RewardRepository", "Error creating initial user doc in Firestore", err)
          }
        }
      }.addOnFailureListener { err ->
        Log.e("RewardRepository", "Error checking user document", err)
      }

      // 2. Real-time Snapshot Listener for Live Sync of coins, streak, check-in, spins, and upiId
      userDocListener = userDocRef.addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w("RewardRepository", "User snapshot listener error: ${error.message}")
          return@addSnapshotListener
        }
        if (snapshot != null && snapshot.exists()) {
          val cloudCoins = (snapshot.getLong("coins") ?: 50L).toInt()
          val streak = (snapshot.getLong("streakDays") ?: 1L).toInt()
          val checkInDate = snapshot.getString("lastCheckInDate") ?: ""
          val spinTime = snapshot.getLong("lastSpinTime") ?: 0L
          val savedUpi = snapshot.getString("upiId") ?: ""

          _walletBalance.value = cloudCoins
          _streakDays.value = streak
          _lastCheckInDate.value = checkInDate
          _lastSpinTime.value = spinTime
          _upiId.value = savedUpi

          val todayStr = getTodayDateString()
          _isTodayClaimed.value = (checkInDate == todayStr)

          val now = System.currentTimeMillis()
          val canSpin = (spinTime == 0L || (now - spinTime) >= 24 * 60 * 60 * 1000L)
          _spinsRemaining.value = if (canSpin) 1 else 0

          prefs.edit()
            .putInt(KEY_BALANCE, cloudCoins)
            .putInt(KEY_STREAK, streak)
            .putString(KEY_LAST_CHECKIN, checkInDate)
            .putLong(KEY_LAST_SPIN_TIME, spinTime)
            .putString(KEY_UPI_ID, savedUpi)
            .apply()
        }
      }

      // 3. Real-time subcollection history listener
      historyDocListener = userDocRef.collection("history")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(50)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w("RewardRepository", "History snapshot listener error: ${error.message}")
            return@addSnapshotListener
          }
          if (snapshot != null && !snapshot.isEmpty) {
            val items = snapshot.documents.mapNotNull { doc ->
              val title = doc.getString("title") ?: return@mapNotNull null
              val amount = (doc.getLong("amount") ?: 0L).toInt()
              val typeStr = doc.getString("type") ?: "CREDIT"
              val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: System.currentTimeMillis()
              val type = if (typeStr.equals("DEBIT", ignoreCase = true)) TransactionType.DEBIT else TransactionType.CREDIT
              val status = if (type == TransactionType.DEBIT) TransactionStatus.PROCESSING else TransactionStatus.COMPLETED
              TransactionItem(
                id = doc.id,
                title = title,
                subtitle = if (type == TransactionType.CREDIT) "Earned reward credited" else "Withdrawal requested",
                amountCoins = amount,
                amountRupees = amount / 10.0,
                type = type,
                timestampMillis = timestamp,
                status = status
              )
            }
            _transactions.value = items
            prefs.edit().putString(KEY_TRANSACTIONS, serializeTransactions(items)).apply()
          }
        }
    } catch (e: Exception) {
      Log.e("RewardRepository", "Failed to initialize Firestore", e)
    }
  }

  fun detachUser() {
    userDocListener?.remove()
    userDocListener = null
    historyDocListener?.remove()
    historyDocListener = null
    activeUid = null
  }

  fun updateUserProfile(uid: String?, name: String?, email: String?, photoUrl: String?) {
    val displayName = if (!name.isNullOrBlank()) name else (email?.substringBefore("@") ?: "DailyWin Winner")
    val letter = displayName.firstOrNull()?.uppercase() ?: "W"
    val updated = _userProfile.value.copy(
      uid = uid,
      name = displayName,
      email = email,
      photoUrl = photoUrl,
      avatarLetter = letter
    )
    _userProfile.value = updated
    prefs.edit()
      .putString(KEY_USER_UID, uid)
      .putString(KEY_USER_NAME, displayName)
      .putString(KEY_USER_EMAIL, email)
      .putString(KEY_USER_PHOTO, photoUrl)
      .apply()

    if (!uid.isNullOrBlank()) {
      attachUser(uid)
    }
  }

  fun clearUserProfile() {
    detachUser()
    _userProfile.value = UserProfile()
    prefs.edit()
      .remove(KEY_USER_UID)
      .remove(KEY_USER_NAME)
      .remove(KEY_USER_EMAIL)
      .remove(KEY_USER_PHOTO)
      .apply()
  }

  /**
   * Anti-Cheat Claim Daily Reward (+20 coins):
   * Validates today's date != lastCheckInDate.
   * Atomically increments coins by 20, updates date and streak in Firestore.
   */
  fun claimDailyReward(): Result<Int> {
    val todayStr = getTodayDateString()
    val lastDate = _lastCheckInDate.value

    if (lastDate == todayStr) {
      return Result.failure(IllegalStateException("Already claimed today ($todayStr). Come back tomorrow!"))
    }

    val currentStreak = _streakDays.value
    val yesterdayStr = getYesterdayDateString()
    val nextStreak = if (lastDate == yesterdayStr) {
      if (currentStreak < 7) currentStreak + 1 else 1
    } else {
      1
    }

    val rewardCoins = 20
    val newBalance = _walletBalance.value + rewardCoins

    val transaction = TransactionItem(
      id = UUID.randomUUID().toString(),
      title = "Daily Check-In (Day $nextStreak)",
      subtitle = "Claimed daily streak bonus",
      amountCoins = rewardCoins,
      amountRupees = rewardCoins / 10.0,
      type = TransactionType.CREDIT,
      timestampMillis = System.currentTimeMillis(),
      status = TransactionStatus.COMPLETED
    )

    // Local optimistic update
    _walletBalance.value = newBalance
    _streakDays.value = nextStreak
    _lastCheckInDate.value = todayStr
    _isTodayClaimed.value = true
    val updatedTxList = listOf(transaction) + _transactions.value
    _transactions.value = updatedTxList

    prefs.edit()
      .putInt(KEY_BALANCE, newBalance)
      .putInt(KEY_STREAK, nextStreak)
      .putString(KEY_LAST_CHECKIN, todayStr)
      .putBoolean(KEY_CLAIMED_TODAY, true)
      .putString(KEY_TRANSACTIONS, serializeTransactions(updatedTxList))
      .apply()

    // Cloud Firestore atomic sync
    val uid = activeUid
    if (uid != null) {
      try {
        val userRef = firestore.collection("users").document(uid)
        userRef.update(
          mapOf(
            "coins" to FieldValue.increment(20L),
            "streakDays" to nextStreak,
            "lastCheckInDate" to todayStr
          )
        ).addOnSuccessListener {
          userRef.collection("history").add(
            hashMapOf(
              "title" to "Daily Check-In (Day $nextStreak)",
              "amount" to 20L,
              "type" to "CREDIT",
              "timestamp" to FieldValue.serverTimestamp()
            )
          )
        }.addOnFailureListener { err ->
          Log.e("RewardRepository", "Error updating daily check-in in Firestore", err)
        }
      } catch (e: Exception) {
        Log.e("RewardRepository", "Failed Firestore check-in update", e)
      }
    }

    return Result.success(rewardCoins)
  }

  fun canSpinWheel(): Boolean {
    val lastSpin = _lastSpinTime.value
    if (lastSpin <= 0L) return true
    val elapsed = System.currentTimeMillis() - lastSpin
    return elapsed >= 24 * 60 * 60 * 1000L
  }

  fun getSpinCooldownRemainingMillis(): Long {
    val lastSpin = _lastSpinTime.value
    if (lastSpin <= 0L) return 0L
    val elapsed = System.currentTimeMillis() - lastSpin
    val cooldown = 24 * 60 * 60 * 1000L
    return if (elapsed < cooldown) cooldown - elapsed else 0L
  }

  /**
   * Anti-Cheat Spin Wheel (5–50 coins):
   * Enforces 24 hours cooldown since lastSpinTime.
   * Atomically increments coins and updates lastSpinTime.
   */
  fun creditSpinReward(chosenCoins: Int): Result<Int> {
    val now = System.currentTimeMillis()
    val lastSpin = _lastSpinTime.value
    val cooldown = 24 * 60 * 60 * 1000L

    if (lastSpin > 0L && (now - lastSpin) < cooldown) {
      val remaining = cooldown - (now - lastSpin)
      val hours = remaining / (3600 * 1000)
      val mins = (remaining % (3600 * 1000)) / (60 * 1000)
      return Result.failure(IllegalStateException("Spin is on cooldown. Next spin in ${hours}h ${mins}m."))
    }

    val newBalance = _walletBalance.value + chosenCoins

    val transaction = TransactionItem(
      id = UUID.randomUUID().toString(),
      title = "Lucky Wheel Spin",
      subtitle = "Won $chosenCoins coins on lucky wheel",
      amountCoins = chosenCoins,
      amountRupees = chosenCoins / 10.0,
      type = TransactionType.CREDIT,
      timestampMillis = now,
      status = TransactionStatus.COMPLETED
    )

    // Local optimistic update
    _walletBalance.value = newBalance
    _lastSpinTime.value = now
    _spinsRemaining.value = 0
    val updatedTxList = listOf(transaction) + _transactions.value
    _transactions.value = updatedTxList

    prefs.edit()
      .putInt(KEY_BALANCE, newBalance)
      .putLong(KEY_LAST_SPIN_TIME, now)
      .putString(KEY_TRANSACTIONS, serializeTransactions(updatedTxList))
      .apply()

    // Cloud Firestore atomic sync
    val uid = activeUid
    if (uid != null) {
      try {
        val userRef = firestore.collection("users").document(uid)
        userRef.update(
          mapOf(
            "coins" to FieldValue.increment(chosenCoins.toLong()),
            "lastSpinTime" to now
          )
        ).addOnSuccessListener {
          userRef.collection("history").add(
            hashMapOf(
              "title" to "Lucky Wheel Spin",
              "amount" to chosenCoins.toLong(),
              "type" to "CREDIT",
              "timestamp" to FieldValue.serverTimestamp()
            )
          )
        }.addOnFailureListener { err ->
          Log.e("RewardRepository", "Error updating spin in Firestore", err)
        }
      } catch (e: Exception) {
        Log.e("RewardRepository", "Failed Firestore spin update", e)
      }
    }

    return Result.success(chosenCoins)
  }

  /**
   * Rewarded Video (+15 coins):
   * Atomically increments coins and appends to history.
   */
  fun creditWatchAdReward(): Int {
    val rewardCoins = 15
    val newBalance = _walletBalance.value + rewardCoins

    val transaction = TransactionItem(
      id = UUID.randomUUID().toString(),
      title = "Watched Video Ad",
      subtitle = "Sponsored partner rewarded video",
      amountCoins = rewardCoins,
      amountRupees = rewardCoins / 10.0,
      type = TransactionType.CREDIT,
      timestampMillis = System.currentTimeMillis(),
      status = TransactionStatus.COMPLETED
    )

    // Local optimistic update
    val updatedTxList = listOf(transaction) + _transactions.value
    _walletBalance.value = newBalance
    _transactions.value = updatedTxList

    prefs.edit()
      .putInt(KEY_BALANCE, newBalance)
      .putString(KEY_TRANSACTIONS, serializeTransactions(updatedTxList))
      .apply()

    // Cloud Firestore atomic update
    val uid = activeUid
    if (uid != null) {
      try {
        val userRef = firestore.collection("users").document(uid)
        userRef.update("coins", FieldValue.increment(15L))
          .addOnSuccessListener {
            userRef.collection("history").add(
              hashMapOf(
                "title" to "Watched Video Ad",
                "amount" to 15L,
                "type" to "CREDIT",
                "timestamp" to FieldValue.serverTimestamp()
              )
            )
          }.addOnFailureListener { err ->
            Log.e("RewardRepository", "Error crediting ad reward in Firestore", err)
          }
      } catch (e: Exception) {
        Log.e("RewardRepository", "Failed Firestore ad reward update", e)
      }
    }

    return rewardCoins
  }

  fun creditShareReward(): Int {
    val rewardCoins = 50
    val newBalance = _walletBalance.value + rewardCoins

    val transaction = TransactionItem(
      id = UUID.randomUUID().toString(),
      title = "Referral Shared",
      subtitle = "Invite link shared with friends",
      amountCoins = rewardCoins,
      amountRupees = rewardCoins / 10.0,
      type = TransactionType.CREDIT,
      timestampMillis = System.currentTimeMillis(),
      status = TransactionStatus.COMPLETED
    )

    val updatedTxList = listOf(transaction) + _transactions.value
    _walletBalance.value = newBalance
    _transactions.value = updatedTxList

    prefs.edit()
      .putInt(KEY_BALANCE, newBalance)
      .putString(KEY_TRANSACTIONS, serializeTransactions(updatedTxList))
      .apply()

    val uid = activeUid
    if (uid != null) {
      try {
        val userRef = firestore.collection("users").document(uid)
        userRef.update("coins", FieldValue.increment(50L))
          .addOnSuccessListener {
            userRef.collection("history").add(
              hashMapOf(
                "title" to "Referral Shared",
                "amount" to 50L,
                "type" to "CREDIT",
                "timestamp" to FieldValue.serverTimestamp()
              )
            )
          }
      } catch (e: Exception) {
        Log.e("RewardRepository", "Failed Firestore share update", e)
      }
    }

    return rewardCoins
  }

  /**
   * Cash Out Request (UPI):
   * Requires minimum 500 coins. Atomically deducts coins using FieldValue.increment(-coins)
   * and creates a new document in the "withdrawals" collection with status "PENDING".
   */
  fun withdrawCash(
    coinsToDeduct: Int,
    payoutMethod: PayoutMethod,
    destination: String
  ): Result<TransactionItem> {
    if (coinsToDeduct < 500) {
      return Result.failure(IllegalArgumentException("Minimum withdrawal is 500 Coins (₹50.00)"))
    }
    if (_walletBalance.value < coinsToDeduct) {
      return Result.failure(IllegalStateException("Insufficient balance. You need ${coinsToDeduct - _walletBalance.value} more coins."))
    }

    val newBalance = _walletBalance.value - coinsToDeduct
    val rupees = coinsToDeduct / 10.0
    val refId = "DW" + (100000..999999).random()

    val transaction = TransactionItem(
      id = UUID.randomUUID().toString(),
      title = "Cashout to ${payoutMethod.displayName}",
      subtitle = "$destination • Ref: #$refId",
      amountCoins = coinsToDeduct,
      amountRupees = rupees,
      type = TransactionType.DEBIT,
      timestampMillis = System.currentTimeMillis(),
      status = TransactionStatus.PROCESSING,
      method = payoutMethod,
      referenceId = refId
    )

    // Local optimistic update
    val updatedTxList = listOf(transaction) + _transactions.value
    _walletBalance.value = newBalance
    _upiId.value = destination
    _transactions.value = updatedTxList

    prefs.edit()
      .putInt(KEY_BALANCE, newBalance)
      .putString(KEY_UPI_ID, destination)
      .putString(KEY_TRANSACTIONS, serializeTransactions(updatedTxList))
      .apply()

    // Cloud Firestore atomic withdrawal creation
    val uid = activeUid
    if (uid != null) {
      try {
        val userRef = firestore.collection("users").document(uid)

        // 1. Atomically deduct coins and store last used UPI ID
        userRef.update(
          mapOf(
            "coins" to FieldValue.increment(-coinsToDeduct.toLong()),
            "upiId" to destination
          )
        ).addOnFailureListener { err ->
          Log.e("RewardRepository", "Error deducting coins in Firestore", err)
        }

        // 2. Create document in root "withdrawals" collection
        val withdrawalDoc = hashMapOf(
          "uid" to uid,
          "amountCoins" to coinsToDeduct.toLong(),
          "payoutAmountInr" to rupees,
          "upiId" to destination,
          "status" to "PENDING",
          "requestedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("withdrawals").add(withdrawalDoc)
          .addOnFailureListener { err ->
            Log.e("RewardRepository", "Error creating withdrawal doc in Firestore", err)
          }

        // 3. Append to user's history subcollection
        userRef.collection("history").add(
          hashMapOf(
            "title" to "Cashout to ${payoutMethod.displayName}",
            "amount" to coinsToDeduct.toLong(),
            "type" to "DEBIT",
            "timestamp" to FieldValue.serverTimestamp()
          )
        )
      } catch (e: Exception) {
        Log.e("RewardRepository", "Failed Firestore withdrawal update", e)
      }
    }

    return Result.success(transaction)
  }

  fun resetToDemoState() {
    val initialBalance = 50
    val initialStreak = 1
    val defaultTx = createInitialTransactions()

    _walletBalance.value = initialBalance
    _streakDays.value = initialStreak
    _isTodayClaimed.value = false
    _lastCheckInDate.value = ""
    _lastSpinTime.value = 0L
    _spinsRemaining.value = 1
    _transactions.value = defaultTx

    prefs.edit()
      .putInt(KEY_BALANCE, initialBalance)
      .putInt(KEY_STREAK, initialStreak)
      .putString(KEY_LAST_CHECKIN, "")
      .putLong(KEY_LAST_SPIN_TIME, 0L)
      .putBoolean(KEY_CLAIMED_TODAY, false)
      .putString(KEY_TRANSACTIONS, serializeTransactions(defaultTx))
      .apply()
  }

  private fun getTodayDateString(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

  private fun getYesterdayDateString(): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -1)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
  }

  private fun createInitialTransactions(): List<TransactionItem> {
    val now = System.currentTimeMillis()
    return listOf(
      TransactionItem(
        id = "tx-welcome",
        title = "Welcome Bonus",
        subtitle = "DailyWin signup bonus credited",
        amountCoins = 50,
        amountRupees = 5.0,
        type = TransactionType.CREDIT,
        timestampMillis = now,
        status = TransactionStatus.COMPLETED
      )
    )
  }

  private fun serializeTransactions(list: List<TransactionItem>): String {
    val jsonArray = JSONArray()
    for (item in list) {
      val obj = JSONObject().apply {
        put("id", item.id)
        put("title", item.title)
        put("subtitle", item.subtitle)
        put("amountCoins", item.amountCoins)
        put("amountRupees", item.amountRupees)
        put("type", item.type.name)
        put("timestampMillis", item.timestampMillis)
        put("status", item.status.name)
        item.method?.let { put("method", it.name) }
        item.referenceId?.let { put("referenceId", it) }
      }
      jsonArray.put(obj)
    }
    return jsonArray.toString()
  }

  private fun deserializeTransactions(jsonStr: String): List<TransactionItem> {
    val list = mutableListOf<TransactionItem>()
    try {
      val array = JSONArray(jsonStr)
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val methodStr = if (obj.has("method")) obj.getString("method") else null
        val refId = if (obj.has("referenceId")) obj.getString("referenceId") else null
        list.add(
          TransactionItem(
            id = obj.getString("id"),
            title = obj.getString("title"),
            subtitle = obj.getString("subtitle"),
            amountCoins = obj.getInt("amountCoins"),
            amountRupees = obj.getDouble("amountRupees"),
            type = TransactionType.valueOf(obj.getString("type")),
            timestampMillis = obj.getLong("timestampMillis"),
            status = TransactionStatus.valueOf(obj.getString("status")),
            method = methodStr?.let { PayoutMethod.valueOf(it) },
            referenceId = refId
          )
        )
      }
    } catch (e: Exception) {
      return createInitialTransactions()
    }
    return list
  }

  companion object {
    private const val KEY_BALANCE = "wallet_balance"
    private const val KEY_STREAK = "streak_days"
    private const val KEY_LAST_CHECKIN = "last_check_in_date"
    private const val KEY_LAST_SPIN_TIME = "last_spin_time"
    private const val KEY_CLAIMED_TODAY = "claimed_today"
    private const val KEY_UPI_ID = "upi_id"
    private const val KEY_TRANSACTIONS = "transactions_json"
    private const val KEY_USER_UID = "user_uid"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_PHOTO = "user_photo"
  }
}
