package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WheelSector
import com.example.ui.SpinWheelState
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LuckyWheelView(
  sectors: List<WheelSector>,
  wheelState: SpinWheelState,
  spinsRemaining: Int,
  onSpinClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isSpinning = wheelState is SpinWheelState.Spinning

  val targetRotation = when (wheelState) {
    is SpinWheelState.Spinning -> wheelState.targetRotation
    else -> 0f
  }

  val animatedRotation by animateFloatAsState(
    targetValue = targetRotation,
    animationSpec = tween(
      durationMillis = if (isSpinning) 3500 else 0,
      easing = FastOutSlowInEasing
    ),
    label = "wheelRotation"
  )

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth()
  ) {
    // Spins remaining tag
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = Color(0xFF2A2000),
      border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark),
      modifier = Modifier.padding(bottom = 12.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Casino,
          contentDescription = null,
          tint = GoldLight,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (spinsRemaining > 0) "Daily Spin Ready! (5 - 50 Coins)" else "Daily Spin on 24h Cooldown ⏳",
          color = if (spinsRemaining > 0) GoldLight else Color(0xFFFFB74D),
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Wheel Container with Top Pointer
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(280.dp)
        .padding(8.dp)
    ) {
      // Outer glow and rim
      Box(
        modifier = Modifier
          .size(264.dp)
          .clip(CircleShape)
          .background(
            brush = Brush.radialGradient(
              colors = listOf(Color(0xFF2A3449), Color(0xFF131926))
            )
          )
          .border(
            width = 4.dp,
            brush = Brush.sweepGradient(
              colors = listOf(GoldLight, GoldDark, GoldPrimary, GoldLight)
            ),
            shape = CircleShape
          )
      )

      // Spinning Canvas Wheel
      Canvas(
        modifier = Modifier
          .size(250.dp)
          .rotate(animatedRotation)
          .testTag("lucky_wheel_canvas")
      ) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val sweepAngle = 360f / sectors.size

        sectors.forEachIndexed { index, sector ->
          val startAngle = index * sweepAngle
          drawArc(
            color = Color(sector.colorHex),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            size = Size(radius * 2f, radius * 2f),
            topLeft = Offset(center.x - radius, center.y - radius)
          )

          // Sector separator line
          val angleRad = (startAngle * PI / 180f).toFloat()
          val lineEnd = Offset(
            center.x + radius * cos(angleRad),
            center.y + radius * sin(angleRad)
          )
          drawLine(
            color = Color(0xFF1E2638),
            start = center,
            end = lineEnd,
            strokeWidth = 2.dp.toPx()
          )

          // Draw Sector Text Label
          val textAngleRad = ((startAngle + sweepAngle / 2f) * PI / 180f).toFloat()
          val textDist = radius * 0.68f
          val textX = center.x + textDist * cos(textAngleRad)
          val textY = center.y + textDist * sin(textAngleRad)

          drawContext.canvas.nativeCanvas.apply {
            save()
            rotate(startAngle + sweepAngle / 2f + 90f, textX, textY)
            val paint = Paint().apply {
              color = android.graphics.Color.WHITE
              textSize = 12.sp.toPx()
              textAlign = Paint.Align.CENTER
              typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
              isAntiAlias = true
              setShadowLayer(3f, 0f, 1f, android.graphics.Color.BLACK)
            }
            drawText("${sector.coins}", textX, textY + 4.dp.toPx(), paint)
            restore()
          }
        }

        // Inner golden rim
        drawCircle(
          color = Color(0xFFFFD54F),
          radius = radius * 0.28f,
          center = center,
          style = Stroke(width = 2.dp.toPx())
        )
      }

      // Center Golden Hub
      Box(
        modifier = Modifier
          .size(54.dp)
          .clip(CircleShape)
          .background(
            brush = Brush.radialGradient(
              colors = listOf(GoldLight, GoldDark)
            )
          )
          .border(2.dp, Color.White, CircleShape)
          .clickable(enabled = !isSpinning) { onSpinClick() },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = "Spin Hub",
          tint = Color(0xFF1E1700),
          modifier = Modifier.size(28.dp)
        )
      }

      // Top Winning Indicator Peg / Pointer (Points downwards to top of wheel)
      Canvas(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .size(24.dp, 28.dp)
      ) {
        val path = Path().apply {
          moveTo(size.width / 2f, size.height) // tip pointing down
          lineTo(0f, 0f)
          lineTo(size.width, 0f)
          close()
        }
        drawPath(
          path = path,
          brush = Brush.verticalGradient(
            colors = listOf(GoldLight, GoldDark)
          )
        )
        drawPath(
          path = path,
          color = Color.White,
          style = Stroke(width = 1.5.dp.toPx())
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Spin CTA Button
    Button(
      onClick = onSpinClick,
      enabled = !isSpinning,
      colors = ButtonDefaults.buttonColors(
        containerColor = GoldPrimary,
        contentColor = Color(0xFF1E1700),
        disabledContainerColor = Color(0xFF3B331A),
        disabledContentColor = TextSecondary
      ),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .height(50.dp)
        .testTag("spin_wheel_button")
    ) {
      Icon(
        imageVector = Icons.Default.Casino,
        contentDescription = null,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (isSpinning) "Spinning..." else if (spinsRemaining > 0) "SPIN LUCKY WHEEL 🎯" else "24H SPIN COOLDOWN ⏳",
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp
      )
    }
  }
}
