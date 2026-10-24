package com.example.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun RouteStopInfoScreen(
  repository: SafeRideRepository,
  modifier: Modifier = Modifier
) {
  val routes by repository.routes.collectAsState()
  val buses by repository.buses.collectAsState()
  val students by repository.students.collectAsState()
  val selectedChildId by repository.selectedChildId.collectAsState()

  val child = students.firstOrNull { it.id == selectedChildId } ?: students.first()
  val route = routes.firstOrNull { it.id == child.routeId } ?: routes.first()
  val bus = buses.firstOrNull { it.id == child.busId } ?: buses.first()

  var isMorningSession by remember { mutableStateOf(true) }
  var selectedStopForDetails by remember { mutableStateOf<BusStop?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundSlate)
      .testTag("route_stop_info_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header & Session Toggle (Morning vs Afternoon)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = route.name,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                color = NavyDark
              )
              Text(
                text = "Destination: ${route.schoolName} • ${route.totalMiles} mi",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }

            Box(
              modifier = Modifier
                .background(SchoolBusYellowLight, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "${route.stops.size} Stops",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = SchoolBusYellowDark
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Morning / Afternoon Segmented Buttons
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(SurfaceContainer, RoundedCornerShape(12.dp))
              .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Button(
              onClick = { isMorningSession = true },
              modifier = Modifier.weight(1f).testTag("select_morning_tab"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isMorningSession) NavyPrimary else Color.Transparent,
                contentColor = if (isMorningSession) Color.White else TextSecondary
              ),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
              Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Morning Pickup", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
              onClick = { isMorningSession = false },
              modifier = Modifier.weight(1f).testTag("select_afternoon_tab"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (!isMorningSession) NavyPrimary else Color.Transparent,
                contentColor = if (!isMorningSession) Color.White else TextSecondary
              ),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
              Icon(Icons.Default.NightsStay, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Afternoon Drop-off", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }

    // Full Route Interactive Map
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "COMPLETE ROUTE CORRIDOR MAP",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
          )
          Spacer(modifier = Modifier.height(10.dp))
          SafeRideLiveMap(
            route = route,
            bus = bus,
            selectedStop = selectedStopForDetails,
            onStopClick = { selectedStopForDetails = it },
            modifier = Modifier
              .fillMaxWidth()
              .height(220.dp),
            isDriverView = false
          )
        }
      }
    }

    // Ordered Stop Timeline
    item {
      Text(
        text = "ORDERED STOPS & TIMETABLE (${if (isMorningSession) "MORNING" else "AFTERNOON"})",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 4.dp)
      )
    }

    itemsIndexed(route.stops) { index, stop ->
      val isChild = stop.isChildStop
      val isPassed = (bus.currentProgress >= stop.order.toFloat() / route.stops.size)
      val time = if (isMorningSession) stop.scheduledMorningTime else stop.scheduledAfternoonTime

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("stop_item_${stop.id}"),
        colors = CardDefaults.cardColors(
          containerColor = if (isChild) SchoolBusYellowLight.copy(alpha = 0.4f) else SurfaceWhite
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
          if (isChild) 1.5.dp else 1.dp,
          if (isChild) SchoolBusYellow else BorderSubtle
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
            // Stop order badge
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(
                  when {
                    isChild -> SchoolBusYellow
                    isPassed -> StatusGreenLight
                    else -> SurfaceContainer
                  },
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              if (isPassed && !isChild) {
                Icon(Icons.Default.Check, contentDescription = "Passed", tint = StatusGreen, modifier = Modifier.size(18.dp))
              } else {
                Text(
                  text = "${stop.order}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = if (isChild) NavyDark else NavyPrimary
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
                if (isChild) {
                  Box(
                    modifier = Modifier
                      .background(SchoolBusYellow, RoundedCornerShape(4.dp))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "Leo's Stop",
                      color = NavyDark,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = stop.address,
                fontSize = 11.sp,
                color = TextSecondary
              )
            }
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = time,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = if (isChild) SchoolBusYellowDark else NavyDark
            )
            Text(
              text = if (isPassed) "Completed" else "Upcoming",
              fontSize = 10.sp,
              color = if (isPassed) StatusGreen else TextMuted
            )
          }
        }
      }
    }
  }
}
