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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Bus
import com.example.model.BusStatus
import com.example.model.Driver
import com.example.model.Route
import com.example.ui.theme.*
import kotlin.math.hypot

@Composable
fun LiveFleetDistrictMap(
  buses: List<Bus>,
  routes: List<Route>,
  drivers: List<Driver>,
  selectedBusId: String? = null,
  onSelectBus: (Bus) -> Unit,
  modifier: Modifier = Modifier
) {
  val textMeasurer = rememberTextMeasurer()

  val infiniteTransition = rememberInfiniteTransition(label = "fleetPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.9f,
    targetValue = 1.8f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "fleetPulseScale"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "fleetPulseAlpha"
  )

  var activeBus by remember(selectedBusId) {
    mutableStateOf(buses.firstOrNull { it.id == selectedBusId } ?: buses.firstOrNull())
  }

  // Pre-calculate bus coordinates
  val busPositions = remember(buses, routes) {
    buses.associateWith { bus ->
      val route = routes.firstOrNull { it.id == bus.routeId } ?: routes.first()
      calculateBusCoords(route.stops, bus.currentProgress)
    }
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(Color(0xFFE2E8F0))
      .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(buses, routes) {
          detectTapGestures { tapOffset ->
            val w = size.width
            val h = size.height
            // Find closest bus
            buses.forEach { bus ->
              val normPos = busPositions[bus] ?: Pair(0.5f, 0.5f)
              val bx = normPos.first * w
              val by = normPos.second * h
              if (hypot(tapOffset.x - bx, tapOffset.y - by) < 45f) {
                activeBus = bus
                onSelectBus(bus)
              }
            }
          }
        }
    ) {
      val w = size.width
      val h = size.height

      // City Background Blocks
      drawFleetGrid(w, h)

      // Draw all Routes in their distinctive colors
      routes.forEach { r ->
        val routeColor = try {
          Color(android.graphics.Color.parseColor(r.colorHex))
        } catch (_: Exception) {
          NavyPrimary
        }
        val points = r.stops.map { Offset(it.xNorm * w, it.yNorm * h) }
        if (points.isNotEmpty()) {
          val p = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
              lineTo(points[i].x, points[i].y)
            }
          }
          drawPath(p, routeColor.copy(alpha = 0.35f), style = Stroke(width = 8f, cap = StrokeCap.Round))
        }

        // Draw small stop dots
        points.forEach { pt ->
          drawCircle(Color.White, radius = 5f, center = pt)
          drawCircle(routeColor, radius = 3.5f, center = pt)
        }
      }

      // Draw Central School Hub
      val schoolX = 0.45f * w
      val schoolY = 0.85f * h
      drawCircle(Color.White, radius = 18f, center = Offset(schoolX, schoolY))
      drawCircle(NavyPrimary, radius = 14f, center = Offset(schoolX, schoolY))
      drawText(
        textMeasurer = textMeasurer,
        text = "🏫",
        topLeft = Offset(schoolX - 10f, schoolY - 11f),
        style = TextStyle(fontSize = 11.sp)
      )

      // Draw Every Fleet Bus Marker
      buses.forEach { bus ->
        val normPos = busPositions[bus] ?: Pair(0.5f, 0.5f)
        val bx = normPos.first * w
        val by = normPos.second * h
        val isSelected = (bus.id == activeBus?.id)

        val busColor = when (bus.status) {
          BusStatus.EN_ROUTE -> SchoolBusYellow
          BusStatus.AT_STOP -> StatusGreen
          BusStatus.DELAYED -> StatusRed
          BusStatus.IDLE -> Color(0xFF94A3B8)
          BusStatus.COMPLETED -> StatusBlue
        }

        if (isSelected || bus.status == BusStatus.EN_ROUTE) {
          drawCircle(
            color = busColor.copy(alpha = pulseAlpha),
            radius = 32f * pulseScale,
            center = Offset(bx, by)
          )
        }

        // Outer ring
        drawCircle(
          color = if (isSelected) NavyDark else Color.White,
          radius = if (isSelected) 20f else 16f,
          center = Offset(bx, by)
        )
        // Filled core
        drawCircle(
          color = busColor,
          radius = if (isSelected) 17f else 13f,
          center = Offset(bx, by)
        )

        // Bus text label (Bus number)
        val busNum = bus.id.replace("BUS-", "#")
        val labelLayout = textMeasurer.measure(
          text = busNum,
          style = TextStyle(
            color = NavyDark,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        )
        drawText(
          textMeasurer = textMeasurer,
          text = busNum,
          topLeft = Offset(bx - labelLayout.size.width / 2, by - labelLayout.size.height / 2),
          style = TextStyle(
            color = NavyDark,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        )
      }
    }

    // Top Header Badge
    Row(
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(12.dp)
        .background(NavyPrimary.copy(alpha = 0.94f), RoundedCornerShape(12.dp))
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
        text = "DISTRICT LIVE FLEET (${buses.filter { it.status == BusStatus.EN_ROUTE || it.status == BusStatus.AT_STOP }.size}/${buses.size} ACTIVE)",
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
    }

    // Floating Telemetry Card for selected bus
    activeBus?.let { b ->
      val assignedDriver = drivers.firstOrNull { it.id == b.driverId }
      val assignedRoute = routes.firstOrNull { it.id == b.routeId }

      Card(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(12.dp)
          .fillMaxWidth()
          .shadow(8.dp, RoundedCornerShape(16.dp))
          .testTag("fleet_bus_telemetry_card"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .background(SchoolBusYellowLight, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(text = "🚌", fontSize = 16.sp)
              }
              Column {
                Text(
                  text = "${b.name} • ${b.plateNumber}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = TextPrimary
                )
                Text(
                  text = "${assignedRoute?.name ?: "Route"} • Driver: ${assignedDriver?.name ?: "Unassigned"}",
                  fontSize = 11.sp,
                  color = TextSecondary
                )
              }
            }

            // Status chip
            Box(
              modifier = Modifier
                .background(
                  when (b.status) {
                    BusStatus.EN_ROUTE -> StatusGreenLight
                    BusStatus.DELAYED -> StatusRedLight
                    else -> SchoolBusYellowLight
                  },
                  RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = b.status.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = when (b.status) {
                  BusStatus.EN_ROUTE -> StatusGreen
                  BusStatus.DELAYED -> StatusRed
                  else -> SchoolBusYellowDark
                }
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider(color = BorderSubtle)
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            TelemetryItem(title = "SPEED", value = "${b.speedMph} mph", icon = Icons.Default.Speed)
            TelemetryItem(title = "RIDERS", value = "${b.studentsOnBoard}/${b.capacity}", icon = Icons.Default.Group)
            TelemetryItem(title = "FUEL", value = "${b.batteryFuelLevel}%", icon = Icons.Default.LocalGasStation)
            TelemetryItem(title = "STATUS", value = b.lastPing, icon = Icons.Default.GpsFixed)
          }
        }
      }
    }
  }
}

@Composable
private fun TelemetryItem(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Icon(icon, contentDescription = null, tint = NavyMuted, modifier = Modifier.size(16.dp))
    Spacer(modifier = Modifier.height(2.dp))
    Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    Text(text = title, fontSize = 9.sp, color = TextMuted)
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFleetGrid(w: Float, h: Float) {
  // Suburb zones
  drawRoundRect(
    color = Color(0xFFF1F5F9),
    topLeft = Offset(w * 0.05f, h * 0.05f),
    size = Size(w * 0.90f, h * 0.90f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
  )

  // Secondary grid lines
  for (i in 1..4) {
    val x = (w / 5) * i
    drawLine(Color(0xFFCBD5E1), Offset(x, 0f), Offset(x, h), strokeWidth = 1.5f)
  }
  for (j in 1..4) {
    val y = (h / 5) * j
    drawLine(Color(0xFFCBD5E1), Offset(0f, y), Offset(w, y), strokeWidth = 1.5f)
  }
}

private fun calculateBusCoords(stops: List<com.example.model.BusStop>, progress: Float): Pair<Float, Float> {
  if (stops.isEmpty()) return Pair(0.5f, 0.5f)
  if (stops.size == 1) return Pair(stops[0].xNorm, stops[0].yNorm)

  val totalSegments = stops.size - 1
  val scaled = (progress.coerceIn(0f, 1f)) * totalSegments
  val currentSegment = scaled.toInt().coerceIn(0, totalSegments - 1)
  val segFraction = scaled - currentSegment

  val p1 = stops[currentSegment]
  val p2 = stops[(currentSegment + 1).coerceAtMost(stops.size - 1)]

  val bx = (p1.xNorm + (p2.xNorm - p1.xNorm) * segFraction)
  val by = (p1.yNorm + (p2.yNorm - p1.yNorm) * segFraction)
  return Pair(bx, by)
}
