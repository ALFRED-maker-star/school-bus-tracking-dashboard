package com.example.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SafeRideRepository
import com.example.model.BusStop
import com.example.ui.components.SafeRideLiveMap
import com.example.ui.theme.*

@Composable
fun DriverRouteScreen(
  repository: SafeRideRepository,
  modifier: Modifier = Modifier
) {
  val buses by repository.buses.collectAsState()
  val routes by repository.routes.collectAsState()
  val isTripActive by repository.isTripActive.collectAsState()

  val bus = buses.firstOrNull { it.id == "BUS-14" } ?: buses.first()
  val route = routes.firstOrNull { it.id == bus.routeId } ?: routes.first()

  // Upcoming stop
  val currentStopIdx = (bus.currentProgress * (route.stops.size - 1)).toInt().coerceIn(0, route.stops.size - 1)
  val upcomingStop = route.stops.getOrNull(currentStopIdx) ?: route.stops.first()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundSlate)
      .testTag("driver_route_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Next Waypoint / Upcoming Stop Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("upcoming_stop_banner"),
        colors = CardDefaults.cardColors(containerColor = NavyPrimary),
        shape = RoundedCornerShape(20.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .background(if (isTripActive) StatusGreen else StatusOrange, CircleShape)
              )
              Text(
                text = if (isTripActive) "APPROACHING STOP #${upcomingStop.order}" else "TRIP PAUSED",
                color = SchoolBusYellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              )
            }

            Text(
              text = upcomingStop.scheduledMorningTime,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = upcomingStop.name,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
          )
          Text(
            text = upcomingStop.address,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { repository.setDriverTab(com.example.model.DriverTab.STUDENT_CHECKLIST) },
              modifier = Modifier
                .weight(1f)
                .testTag("open_stop_roster_button"),
              colors = ButtonDefaults.buttonColors(containerColor = SchoolBusYellow),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Checklist, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Check-In Students", color = NavyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { repository.setDriverTab(com.example.model.DriverTab.TRIP_CONTROL) },
              modifier = Modifier
                .weight(1f)
                .testTag("open_trip_controls_button"),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Trip Controls", fontSize = 12.sp)
            }
          }
        }
      }
    }

    // Driver Route Map View
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "LIVE DRIVER NAVIGATION MAP",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
          )
          Spacer(modifier = Modifier.height(10.dp))
          SafeRideLiveMap(
            route = route,
            bus = bus,
            modifier = Modifier
              .fillMaxWidth()
              .height(240.dp),
            isDriverView = true
          )
        }
      }
    }

    // Ordered Stop List for Driver
    item {
      Text(
        text = "TODAY'S SCHEDULED STOPS (${route.stops.size} TOTAL)",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 4.dp)
      )
    }

    itemsIndexed(route.stops) { index, stop ->
      val isPassed = (bus.currentProgress >= stop.order.toFloat() / route.stops.size)
      val isCurrent = (stop.order == upcomingStop.order)

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("driver_stop_row_${stop.id}"),
        colors = CardDefaults.cardColors(
          containerColor = if (isCurrent) SchoolBusYellowLight.copy(alpha = 0.6f) else SurfaceWhite
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
          if (isCurrent) 1.5.dp else 1.dp,
          if (isCurrent) SchoolBusYellowDark else BorderSubtle
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(
                  when {
                    isCurrent -> SchoolBusYellow
                    isPassed -> StatusGreenLight
                    else -> SurfaceContainer
                  },
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              if (isPassed && !isCurrent) {
                Icon(Icons.Default.Check, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(18.dp))
              } else {
                Text(
                  text = "${stop.order}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = if (isCurrent) NavyDark else NavyPrimary
                )
              }
            }

            Column {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = stop.name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = TextPrimary
                )
                if (isCurrent) {
                  Box(
                    modifier = Modifier
                      .background(SchoolBusYellowDark, RoundedCornerShape(4.dp))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(text = "CURRENT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${stop.address} • ${stop.studentIds.size.coerceAtLeast(1)} Expected Boarders",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }
          }

          Text(
            text = stop.scheduledMorningTime,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NavyDark
          )
        }
      }
    }
  }
}
