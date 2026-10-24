package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Bus
import com.example.model.BusStop
import com.example.model.Route
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun SafeRideLiveMap(
  route: Route,
  bus: Bus,
  selectedStop: BusStop? = null,
  onStopClick: ((BusStop) -> Unit)? = null,
  modifier: Modifier = Modifier,
  showControls: Boolean = true,
  isDriverView: Boolean = false
) {
  val textMeasurer = rememberTextMeasurer()

  // Pulse animation for bus and child stop
  val infiniteTransition = rememberInfiniteTransition(label = "mapPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 1.6f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulseScale"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulseAlpha"
  )

  var inspectedStop by remember { mutableStateOf(selectedStop) }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(Color(0xFFE8EEF5))
      .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(route) {
          detectTapGestures { tapOffset ->
            val w = size.width
            val h = size.height
            // Check if tapped near any stop
            val tappedStop = route.stops.firstOrNull { stop ->
              val sx = stop.xNorm * w
              val sy = stop.yNorm * h
              val dist = hypot(tapOffset.x - sx, tapOffset.y - sy)
              dist < 40f
            }
            inspectedStop = tappedStop
            if (tappedStop != null) {
              onStopClick?.invoke(tappedStop)
            }
          }
        }
    ) {
      val w = size.width
      val h = size.height

      // 1. Draw City Map Background Features (Parks, River, Grid)
      drawMapBackground(w, h)

      // 2. Draw Route Polyline
      val stopPoints = route.stops.map { Offset(it.xNorm * w, it.yNorm * h) }
      if (stopPoints.isNotEmpty()) {
        // Full route shadow/base
        val routePath = Path().apply {
          moveTo(stopPoints.first().x, stopPoints.first().y)
          for (i in 1 until stopPoints.size) {
            val prev = stopPoints[i - 1]
            val curr = stopPoints[i]
            // Draw smooth curve between stops
            val midX = (prev.x + curr.x) / 2
            val midY = (prev.y + curr.y) / 2
            quadraticTo(prev.x, prev.y, midX, midY)
            lineTo(curr.x, curr.y)
          }
        }

        // Road backing
        drawPath(
          path = routePath,
          color = Color(0xFFCBD5E1),
          style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Route colored ribbon
        drawPath(
          path = routePath,
          color = Color(0xFFF59E0B),
          style = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
      }

      // 3. Draw Bus Stops
      route.stops.forEachIndexed { index, stop ->
        val sx = stop.xNorm * w
        val sy = stop.yNorm * h
        val isChild = stop.isChildStop
        val isPassed = (bus.currentProgress >= stop.order.toFloat() / route.stops.size)

        // Child stop pulse
        if (isChild) {
          drawCircle(
            color = SchoolBusYellow.copy(alpha = pulseAlpha),
            radius = 28f * pulseScale,
            center = Offset(sx, sy)
          )
        }

        // Stop Pin Outer Circle
        val pinColor = when {
          isChild -> SchoolBusYellow
          isPassed -> StatusGreen
          else -> NavyPrimary
        }

        drawCircle(
          color = Color.White,
          radius = if (isChild) 16f else 12f,
          center = Offset(sx, sy)
        )
        drawCircle(
          color = pinColor,
          radius = if (isChild) 13f else 9f,
          center = Offset(sx, sy)
        )

        // Draw stop order number
        val label = if (isChild) "★" else "${stop.order}"
        val textLayout = textMeasurer.measure(
          text = label,
          style = TextStyle(
            color = if (isChild) NavyDark else Color.White,
            fontSize = if (isChild) 11.sp else 9.sp,
            fontWeight = FontWeight.Bold
          )
        )
        drawText(
          textMeasurer = textMeasurer,
          text = label,
          topLeft = Offset(sx - textLayout.size.width / 2, sy - textLayout.size.height / 2),
          style = TextStyle(
            color = if (isChild) NavyDark else Color.White,
            fontSize = if (isChild) 11.sp else 9.sp,
            fontWeight = FontWeight.Bold
          )
        )

        // If child stop, draw "Leo's Stop" badge tag
        if (isChild && !isDriverView) {
          drawChildStopCallout(sx, sy, stop.name, textMeasurer)
        }
      }

      // 4. Calculate Bus Position along Route Progress
      val busPos = calculateBusCoordinates(route.stops, bus.currentProgress, w, h)

      // Pulsing radar ring around bus
      drawCircle(
        color = SchoolBusYellow.copy(alpha = pulseAlpha),
        radius = 36f * pulseScale,
        center = busPos
      )

      // Outer bus shadow
      drawCircle(
        color = Color(0x33000000),
        radius = 20f,
        center = Offset(busPos.x, busPos.y + 4f)
      )

      // Bus circular badge
      drawCircle(
        color = SchoolBusYellow,
        radius = 18f,
        center = busPos
      )
      drawCircle(
        color = NavyDark,
        radius = 18f,
        center = busPos,
        style = Stroke(width = 3.5f)
      )

      // Draw Mini Bus Icon / Plate Number
      val busLabelLayout = textMeasurer.measure(
        text = "🚌",
        style = TextStyle(fontSize = 13.sp)
      )
      drawText(
        textMeasurer = textMeasurer,
        text = "🚌",
        topLeft = Offset(busPos.x - busLabelLayout.size.width / 2, busPos.y - busLabelLayout.size.height / 2 - 1f)
      )

      // Bus Live Tag with Speed
      val speedText = "${bus.speedMph} mph"
      val speedLayout = textMeasurer.measure(
        text = speedText,
        style = TextStyle(
          color = Color.White,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold
        )
      )
      val tagWidth = speedLayout.size.width + 16f
      val tagHeight = speedLayout.size.height + 6f
      val tagX = busPos.x - tagWidth / 2
      val tagY = busPos.y - 36f

      drawRoundRect(
        color = NavyPrimary,
        topLeft = Offset(tagX, tagY),
        size = Size(tagWidth, tagHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
      )
      drawText(
        textMeasurer = textMeasurer,
        text = speedText,
        topLeft = Offset(tagX + 8f, tagY + 3f),
        style = TextStyle(
          color = Color.White,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold
        )
      )
    }

    // Top overlay badge (Safety status & Route Name)
    Row(
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(12.dp)
        .background(NavyPrimary.copy(alpha = 0.92f), RoundedCornerShape(12.dp))
        .padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .background(StatusGreen, CircleShape)
      )
      Text(
        text = "LIVE GPS • ${route.name}",
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
    }

    // Floating map controls (Recenter, Layers)
    if (showControls) {
      Column(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        IconButton(
          onClick = { inspectedStop = route.stops.firstOrNull { it.isChildStop } },
          modifier = Modifier
            .size(36.dp)
            .shadow(4.dp, CircleShape)
            .background(Color.White, CircleShape)
            .testTag("recenter_child_stop")
        ) {
          Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = "Center on Child Stop",
            tint = SchoolBusYellowDark,
            modifier = Modifier.size(18.dp)
          )
        }

        IconButton(
          onClick = { inspectedStop = null },
          modifier = Modifier
            .size(36.dp)
            .shadow(4.dp, CircleShape)
            .background(Color.White, CircleShape)
            .testTag("reset_map_view")
        ) {
          Icon(
            imageVector = Icons.Default.Layers,
            contentDescription = "Map Layers",
            tint = NavyPrimary,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // Interactive Stop inspection popover
    inspectedStop?.let { stop ->
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(12.dp)
          .fillMaxWidth()
          .shadow(6.dp, RoundedCornerShape(14.dp))
          .background(Color.White, RoundedCornerShape(14.dp))
          .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
          .padding(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Box(
                modifier = Modifier
                  .size(20.dp)
                  .background(if (stop.isChildStop) SchoolBusYellowLight else SurfaceContainer, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${stop.order}",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (stop.isChildStop) SchoolBusYellowDark else NavyPrimary
                )
              }
              Text(
                text = stop.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary
              )
              if (stop.isChildStop) {
                Box(
                  modifier = Modifier
                    .background(SchoolBusYellowLight, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "Child's Stop",
                    fontSize = 10.sp,
                    color = SchoolBusYellowDark,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${stop.address} • Scheduled: ${stop.scheduledMorningTime}",
              fontSize = 11.sp,
              color = TextSecondary
            )
          }

          IconButton(
            onClick = { inspectedStop = null },
            modifier = Modifier.size(28.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
          }
        }
      }
    }
  }
}

private fun DrawScope.drawChildStopCallout(sx: Float, sy: Float, name: String, textMeasurer: TextMeasurer) {
  val title = "⭐ Leo's Stop"
  val layout = textMeasurer.measure(
    text = title,
    style = TextStyle(
      color = NavyDark,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold
    )
  )
  val padX = 12f
  val padY = 6f
  val boxW = layout.size.width + padX * 2
  val boxH = layout.size.height + padY * 2
  val boxX = sx - boxW / 2
  val boxY = sy + 20f

  // Callout pointer triangle
  val triangle = Path().apply {
    moveTo(sx, sy + 14f)
    lineTo(sx - 6f, boxY)
    lineTo(sx + 6f, boxY)
    close()
  }
  drawPath(triangle, color = SchoolBusYellow)

  drawRoundRect(
    color = SchoolBusYellow,
    topLeft = Offset(boxX, boxY),
    size = Size(boxW, boxH),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
  )
  drawRoundRect(
    color = NavyDark,
    topLeft = Offset(boxX, boxY),
    size = Size(boxW, boxH),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
    style = Stroke(width = 1.5f)
  )
  drawText(
    textMeasurer = textMeasurer,
    text = title,
    topLeft = Offset(boxX + padX, boxY + padY),
    style = TextStyle(
      color = NavyDark,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold
    )
  )
}

private fun DrawScope.drawMapBackground(w: Float, h: Float) {
  // Park green area
  drawRoundRect(
    color = Color(0xFFDCFCE7),
    topLeft = Offset(w * 0.70f, h * 0.15f),
    size = Size(w * 0.25f, h * 0.28f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
  )

  // School zone district area
  drawRoundRect(
    color = Color(0xFFFEF3C7).copy(alpha = 0.6f),
    topLeft = Offset(w * 0.30f, h * 0.72f),
    size = Size(w * 0.45f, h * 0.20f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
  )

  // Arterial cross streets
  val roadColor = Color(0xFFFFFFFF)
  val roadStroke = 8f
  drawLine(roadColor, Offset(0f, h * 0.35f), Offset(w, h * 0.35f), strokeWidth = roadStroke)
  drawLine(roadColor, Offset(0f, h * 0.65f), Offset(w, h * 0.65f), strokeWidth = roadStroke)
  drawLine(roadColor, Offset(w * 0.30f, 0f), Offset(w * 0.30f, h), strokeWidth = roadStroke)
  drawLine(roadColor, Offset(w * 0.65f, 0f), Offset(w * 0.65f, h), strokeWidth = roadStroke)

  // River / Lake curve
  val riverPath = Path().apply {
    moveTo(0f, h * 0.85f)
    quadraticTo(w * 0.25f, h * 0.75f, w * 0.15f, h)
    lineTo(0f, h)
    close()
  }
  drawPath(riverPath, color = Color(0xFFBFDBFE))
}

private fun calculateBusCoordinates(stops: List<BusStop>, progress: Float, w: Float, h: Float): Offset {
  if (stops.isEmpty()) return Offset(w / 2, h / 2)
  if (stops.size == 1) return Offset(stops[0].xNorm * w, stops[0].yNorm * h)

  val totalSegments = stops.size - 1
  val scaled = (progress.coerceIn(0f, 1f)) * totalSegments
  val currentSegment = scaled.toInt().coerceIn(0, totalSegments - 1)
  val segFraction = scaled - currentSegment

  val p1 = stops[currentSegment]
  val p2 = stops[(currentSegment + 1).coerceAtMost(stops.size - 1)]

  val bx = (p1.xNorm + (p2.xNorm - p1.xNorm) * segFraction) * w
  val by = (p1.yNorm + (p2.yNorm - p1.yNorm) * segFraction) * h
  return Offset(bx, by)
}
