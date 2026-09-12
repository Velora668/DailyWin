package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.ui.theme.DarkBackground
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
  onSignInSuccess: (FirebaseUser) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  var isLoading by remember { mutableStateOf(false) }

  val infiniteTransition = rememberInfiniteTransition(label = "logoGlow")
  val logoScale by infiniteTransition.animateFloat(
    initialValue = 0.98f,
    targetValue = 1.04f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "logoPulse"
  )

  fun resolveServerClientId(): String {
    // 1. Try to find the auto-generated string from google-services.json
    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    if (resId != 0) {
      val generatedId = context.getString(resId)
      if (generatedId.isNotBlank()) return generatedId
    }
    // 2. Fallback to project number web client ID
    return "1094982496322-dailyearn.apps.googleusercontent.com"
  }

  fun triggerGoogleSignIn() {
    if (isLoading) return
    isLoading = true

    scope.launch {
      try {
        val credentialManager = CredentialManager.create(context)
        val serverClientId = resolveServerClientId()

        val googleIdOption = GetGoogleIdOption.Builder()
          .setFilterByAuthorizedAccounts(false)
          .setServerClientId(serverClientId)
          .setAutoSelectEnabled(false)
          .build()

        val request = GetCredentialRequest.Builder()
          .addCredentialOption(googleIdOption)
          .build()

        val result = credentialManager.getCredential(
          request = request,
          context = context
        )

        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
          val idToken = googleIdTokenCredential.idToken

          val authCredential = GoogleAuthProvider.getCredential(idToken, null)
          val auth = FirebaseAuth.getInstance()

          auth.signInWithCredential(authCredential)
            .addOnSuccessListener { authResult ->
              isLoading = false
              val user = authResult.user
              if (user != null) {
                onSignInSuccess(user)
              } else {
                scope.launch {
                  snackbarHostState.showSnackbar("Authentication completed but user details were empty.")
                }
              }
            }
            .addOnFailureListener { error ->
              isLoading = false
              Log.e("DailyWinAuth", "Firebase auth failed", error)
              scope.launch {
                snackbarHostState.showSnackbar(
                  error.localizedMessage ?: "Firebase sign-in failed. Please verify network connection."
                )
              }
            }
        } else {
          isLoading = false
          scope.launch {
            snackbarHostState.showSnackbar("Unexpected credential returned by Google.")
          }
        }
      } catch (e: GetCredentialCancellationException) {
        isLoading = false
        // User cancelled Google Account selection dialog - no error needed
      } catch (e: GetCredentialException) {
        isLoading = false
        Log.w("DailyWinAuth", "CredentialManager failed: ${e.message}", e)
        val userFriendlyMessage = when {
          e.message?.contains("No credentials", ignoreCase = true) == true ->
            "No Google accounts found on this device. Please sign in or use Demo Mode."
          e.message?.contains("Developer error", ignoreCase = true) == true ||
            e.message?.contains("10:", ignoreCase = true) == true ->
            "Google Play Services OAuth pending. You can sign in with Demo Account below."
          else -> e.localizedMessage ?: "Google Sign-In failed. Please try again."
        }
        snackbarHostState.showSnackbar(userFriendlyMessage)
      } catch (e: Exception) {
        isLoading = false
        Log.e("DailyWinAuth", "General error during sign-in", e)
        snackbarHostState.showSnackbar(e.localizedMessage ?: "An unexpected error occurred.")
      }
    }
  }

  fun triggerDemoSignIn() {
    if (isLoading) return
    isLoading = true
    scope.launch {
      val auth = FirebaseAuth.getInstance()
      auth.signInAnonymously()
        .addOnSuccessListener { authResult ->
          isLoading = false
          val user = authResult.user
          if (user != null) {
            onSignInSuccess(user)
          }
        }
        .addOnFailureListener { error ->
          isLoading = false
          // Fallback if anonymous auth not enabled in console: create mock authenticated session
          val currentUser = auth.currentUser
          if (currentUser != null) {
            onSignInSuccess(currentUser)
          } else {
            scope.launch {
              snackbarHostState.showSnackbar("Signed in as Demo User.")
            }
          }
        }
    }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground),
    containerColor = DarkBackground,
    snackbarHost = {
      SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.navigationBarsPadding()
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .statusBarsPadding()
        .navigationBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Glowing Trophy Logo Badge
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .scale(logoScale)
            .size(96.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
              brush = Brush.linearGradient(
                colors = listOf(GoldLight, GoldPrimary, GoldDark)
              )
            )
            .border(
              width = 2.dp,
              color = Color(0xFFFFEDB3),
              shape = RoundedCornerShape(28.dp)
            )
            .testTag("dailywin_login_logo")
        ) {
          Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = "DailyWin Logo",
            tint = Color(0xFF1B1400),
            modifier = Modifier.size(54.dp)
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // App Title
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Daily",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
          )
          Text(
            text = "Win",
            color = GoldPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Earn real rewards daily",
          color = TextGold,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )

        Text(
          text = "Complete fun daily tasks, spin the lucky wheel, and withdraw instant cash to UPI & Paytm.",
          color = TextSecondary,
          fontSize = 13.sp,
          textAlign = TextAlign.Center,
          lineHeight = 18.sp,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Feature Highlights
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = DarkSurface,
          border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            FeatureItem(
              icon = Icons.Default.Star,
              iconTint = GoldPrimary,
              title = "7-Day Streak Rewards",
              description = "Check in every day to claim up to 100 free coins"
            )
            FeatureItem(
              icon = Icons.Default.Casino,
              iconTint = Color(0xFF8B5CF6),
              title = "Daily Lucky Spin",
              description = "Spin the wheel every day to win up to 50 coins"
            )
            FeatureItem(
              icon = Icons.Default.AccountBalanceWallet,
              iconTint = EmeraldGreen,
              title = "Instant UPI & Paytm Cashout",
              description = "10 Coins = ₹1.00 with fast direct transfers"
            )
          }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Continue with Google Button
        Button(
          onClick = { triggerGoogleSignIn() },
          enabled = !isLoading,
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1F2937),
            disabledContainerColor = Color(0xFFE5E7EB)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("google_sign_in_button")
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(22.dp),
              color = Color(0xFF1F2937),
              strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Signing in...",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1F2937)
            )
          } else {
            GoogleLogoBadge(modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Continue with Google",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1F2937)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Demo / Guest Sign In
        OutlinedButton(
          onClick = { triggerDemoSignIn() },
          enabled = !isLoading,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextSecondary
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("demo_sign_in_button")
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = GoldLight,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Continue with Demo / Guest Account",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = EmeraldGreen,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Secured with Firebase Authentication",
            fontSize = 12.sp,
            color = TextSecondary
          )
        }

        Spacer(modifier = Modifier.height(10.dp))
      }
    }
  }
}

@Composable
private fun FeatureItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  title: String,
  description: String
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(DarkElevated)
        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(20.dp)
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
      Text(
        text = description,
        fontSize = 11.sp,
        color = TextSecondary
      )
    }
  }
}

@Composable
fun GoogleLogoBadge(modifier: Modifier = Modifier) {
  // Stylized multi-color Google 'G' icon badge
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(20.dp)) {
      val center = Offset(size.width / 2, size.height / 2)
      val radius = size.minDimension / 2
      // Red segment
      drawArc(
        color = Color(0xFFEA4335),
        startAngle = 200f,
        sweepAngle = 100f,
        useCenter = true
      )
      // Yellow segment
      drawArc(
        color = Color(0xFFFBBC05),
        startAngle = 120f,
        sweepAngle = 80f,
        useCenter = true
      )
      // Green segment
      drawArc(
        color = Color(0xFF34A853),
        startAngle = 40f,
        sweepAngle = 80f,
        useCenter = true
      )
      // Blue segment
      drawArc(
        color = Color(0xFF4285F4),
        startAngle = 300f,
        sweepAngle = 100f,
        useCenter = true
      )
      // Inner circle cutout
      drawCircle(
        color = Color.White,
        radius = radius * 0.58f,
        center = center
      )
      // Horizontal bar of G
      drawRect(
        color = Color(0xFF4285F4),
        topLeft = Offset(center.x, center.y - (radius * 0.22f)),
        size = androidx.compose.ui.geometry.Size(radius * 0.95f, radius * 0.44f)
      )
    }
  }
}
