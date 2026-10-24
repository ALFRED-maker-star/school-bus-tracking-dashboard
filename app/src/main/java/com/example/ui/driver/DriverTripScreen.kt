package com.example.ui.driver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*

@Composable
fun DriverTripScreen(
  repository: SafeRideRepository,
  modifier: Modifier = Modifier
) {
  val isTripActive by repository.isTripActive.collectAsState()
  val buses by repository.buses.collectAsState()
  val students by repository.students.collectAsState()
  val isSafetySweepDone by repository.isSafetySweepDone.collectAsState()

  val bus = buses.firstOrNull { it.id == "BUS-14" } ?: buses.first()
  val boardedStudents = students.filter { it.busId == bus.id && it.boardingStatus == com.example.model.BoardingStatus.BOARDED }

  var showEndTripDialog by remember { mutableStateOf(false) }
  var safetySweepConfirmed by remember { mutableStateOf(false) }
  var showDelayDialog by remember { mutableStateOf(false) }

  val infiniteTransition = rememberInfiniteTransition(label = "driverRadar")
  val radarPulse by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 1.3f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "radarScale"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundSlate)
      .testTag("driver_trip_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Large Trip Status Header Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("trip_status_card"),
        colors = CardDefaults.cardColors(
          containerColor = if (isTripActive) NavyPrimary else SurfaceWhite
        ),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Status Ring with live radar indicator
          Box(
            modifier = Modifier
              .size(80.dp)
              .background(
                if (isTripActive) StatusGreen.copy(alpha = 0.2f) else SurfaceContainer,
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(if (isTripActive) (54f * radarPulse).dp else 54.dp)
                .background(
                  if (isTripActive) StatusGreen else Color(0xFF94A3B8),
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isTripActive) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = if (isTripActive) "LIVE GPS SHARING ACTIVE" else "TRIP INACTIVE / PAUSED",
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = if (isTripActive) SchoolBusYellow else NavyDark
          )

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (isTripActive)
              "Broadcasting real-time vehicle telemetry to parents and district dispatch"
            else
              "Press 'Start Trip' to begin live route tracking and stop notifications",
            fontSize = 12.sp,
            color = if (isTripActive) Color(0xFFCBD5E1) else TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Live Telemetry Grid
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                if (isTripActive) NavyDark.copy(alpha = 0.6f) else SurfaceContainer,
                RoundedCornerShape(14.dp)
              )
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            TripMetricItem(
              title = "SPEED",
              value = "${bus.speedMph} mph",
              textColor = if (isTripActive) Color.White else TextPrimary
            )
            TripMetricItem(
              title = "ON BOARD",
              value = "${boardedStudents.size}/${bus.capacity}",
              textColor = if (isTripActive) Color.White else TextPrimary
            )
            TripMetricItem(
              title = "PROGRESS",
              value = "${(bus.currentProgress * 100).toInt()}%",
              textColor = if (isTripActive) Color.White else TextPrimary
            )
            TripMetricItem(
              title = "BATTERY",
              value = "${bus.batteryFuelLevel}%",
              textColor = if (isTripActive) Color.White else TextPrimary
            )
          }
        }
      }
    }

    // Big Action Buttons: Start Trip & End Trip
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!isTripActive) {
          Button(
            onClick = { repository.startDriverTrip() },
            modifier = Modifier
              .fillMaxWidth()
              .height(64.dp)
              .testTag("start_trip_button"),
            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
            shape = RoundedCornerShape(18.dp)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("START TRIP (BEGIN GPS SHARING)", fontSize = 16.sp, fontWeight = FontWeight.Black)
          }
        } else {
          Button(
            onClick = { showEndTripDialog = true },
            modifier = Modifier
              .fillMaxWidth()
              .height(64.dp)
              .testTag("end_trip_button"),
            colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
            shape = RoundedCornerShape(18.dp)
          ) {
            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("END TRIP & SAFETY CHECK", fontSize = 16.sp, fontWeight = FontWeight.Black)
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { repository.pauseTrip() },
              modifier = Modifier.weight(1f).testTag("pause_trip_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
            ) {
              Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Pause Trip", fontSize = 13.sp)
            }

            Button(
              onClick = { showDelayDialog = true },
              modifier = Modifier.weight(1f).testTag("report_delay_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = StatusOrange)
            ) {
              Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Report Delay", fontSize = 13.sp)
            }
          }
        }
      }
    }

    // Safety Sweep Status Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("safety_sweep_card"),
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = if (isSafetySweepDone) StatusGreen else NavyMuted)
              Text(
                text = "CHILD SAFETY SEAT SWEEP",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
              )
            }
            Box(
              modifier = Modifier
                .background(if (isSafetySweepDone) StatusGreenLight else SurfaceContainer, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = if (isSafetySweepDone) "VERIFIED" else "PENDING AT END",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSafetySweepDone) StatusGreen else TextMuted
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "State safety regulations mandate that drivers physically walk to the back of the bus and inspect every seat before logging out.",
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 15.sp
          )
        }
      }
    }
  }

  // End Trip & Safety Sweep Confirmation Dialog
  if (showEndTripDialog) {
    AlertDialog(
      onDismissRequest = { showEndTripDialog = false },
      icon = { Icon(Icons.Default.ChecklistRtl, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(32.dp)) },
      title = { Text(text = "End Trip & Post-Route Sweep", fontWeight = FontWeight.Black) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(text = "Are you sure you want to conclude Route 3B at Oakridge Elementary?")
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(SchoolBusYellowLight, RoundedCornerShape(10.dp))
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = safetySweepConfirmed,
              onCheckedChange = { safetySweepConfirmed = it },
              modifier = Modifier.testTag("sweep_confirm_checkbox")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "I have physically walked the aisle and confirmed NO students or belongings remain on board.",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NavyDark
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            repository.endDriverTrip(safetySweepConfirmed)
            showEndTripDialog = false
          },
          enabled = safetySweepConfirmed,
          colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
        ) {
          Text("End Trip & Broadcast Complete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEndTripDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Report Delay Dialog
  if (showDelayDialog) {
    var delayMinutes by remember { mutableStateOf("5") }
    var delayReason by remember { mutableStateOf("Heavy road construction") }

    AlertDialog(
      onDismissRequest = { showDelayDialog = false },
      title = { Text(text = "Report Traffic Delay", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = delayMinutes,
            onValueChange = { delayMinutes = it },
            label = { Text("Estimated Delay (Minutes)") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = delayReason,
            onValueChange = { delayReason = it },
            label = { Text("Reason (Traffic, Weather, Detour)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            repository.pauseTrip()
            showDelayDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusOrange)
        ) {
          Text("Send Delay Alert to Parents")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDelayDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun TripMetricItem(title: String, value: String, textColor: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = textColor)
    Spacer(modifier = Modifier.height(2.dp))
    Text(text = title, fontSize = 10.sp, color = TextMuted)
  }
}
