package com.example.ui.parent

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SafeRideRepository
import com.example.model.BusStatus
import com.example.ui.components.SafeRideLiveMap
import com.example.ui.theme.*

@Composable
fun ParentHomeScreen(
  repository: SafeRideRepository,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val buses by repository.buses.collectAsState()
  val routes by repository.routes.collectAsState()
  val drivers by repository.drivers.collectAsState()
  val students by repository.students.collectAsState()
  val selectedChildId by repository.selectedChildId.collectAsState()

  val child = students.firstOrNull { it.id == selectedChildId } ?: students.first()
  val bus = buses.firstOrNull { it.id == child.busId } ?: buses.first()
  val route = routes.firstOrNull { it.id == child.routeId } ?: routes.first()
  val driver = drivers.firstOrNull { it.id == bus.driverId } ?: drivers.first()

  var showContactDialog by remember { mutableStateOf(false) }
  var showSosConfirmDialog by remember { mutableStateOf(false) }
  var showAbsenceReportDialog by remember { mutableStateOf(false) }
  var absenceReason by remember { mutableStateOf("Feeling unwell / doctor visit") }
  var absenceSubmitted by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundSlate)
      .testTag("parent_home_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Child Quick Status Bar
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("child_quick_status_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
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
                .size(46.dp)
                .background(SchoolBusYellowLight, CircleShape)
                .border(2.dp, SchoolBusYellow, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(text = child.avatarEmoji, fontSize = 24.sp)
            }
            Column {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = child.name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = TextPrimary
                )
                Box(
                  modifier = Modifier
                    .background(SchoolBusYellowLight, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = child.grade.split("•")[0].trim(),
                    fontSize = 10.sp,
                    color = SchoolBusYellowDark,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Stop: ${child.stopName}",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }
          }

          // Live status badge
          Box(
            modifier = Modifier
              .background(
                when (child.boardingStatus) {
                  com.example.model.BoardingStatus.BOARDED -> StatusGreenLight
                  com.example.model.BoardingStatus.WAITING -> SchoolBusYellowLight
                  com.example.model.BoardingStatus.MISSING -> StatusRedLight
                  com.example.model.BoardingStatus.ABSENT -> SurfaceContainer
                },
                RoundedCornerShape(8.dp)
              )
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = when (child.boardingStatus) {
                com.example.model.BoardingStatus.BOARDED -> "On Board"
                com.example.model.BoardingStatus.WAITING -> "At Stop Soon"
                com.example.model.BoardingStatus.MISSING -> "Alert Flagged"
                com.example.model.BoardingStatus.ABSENT -> "Excused"
              },
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = when (child.boardingStatus) {
                com.example.model.BoardingStatus.BOARDED -> StatusGreen
                com.example.model.BoardingStatus.WAITING -> SchoolBusYellowDark
                com.example.model.BoardingStatus.MISSING -> StatusRed
                com.example.model.BoardingStatus.ABSENT -> TextSecondary
              }
            )
          }
        }
      }
    }

    // Live Map & ETA Card Section
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("live_map_container_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // ETA Banner Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "ESTIMATED ARRIVAL AT YOUR STOP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
              )
              Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = "${bus.etaMinutesToChildStop}",
                  fontSize = 32.sp,
                  fontWeight = FontWeight.Black,
                  color = NavyDark
                )
                Text(
                  text = "MINS",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = SchoolBusYellowDark,
                  modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                  text = "(~07:38 AM)",
                  fontSize = 12.sp,
                  color = TextMuted,
                  modifier = Modifier.padding(bottom = 6.dp)
                )
              }
            }

            Box(
              modifier = Modifier
                .background(
                  if (bus.status == BusStatus.DELAYED) StatusRedLight else StatusGreenLight,
                  RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .background(if (bus.status == BusStatus.DELAYED) StatusRed else StatusGreen, CircleShape)
                )
                Text(
                  text = if (bus.status == BusStatus.DELAYED) "Delay +5m" else "On Schedule",
                  color = if (bus.status == BusStatus.DELAYED) StatusRed else StatusGreen,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // The Live Canvas Map
          SafeRideLiveMap(
            route = route,
            bus = bus,
            modifier = Modifier
              .fillMaxWidth()
              .height(260.dp),
            isDriverView = false
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Live Telemetry bar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(SurfaceContainer, RoundedCornerShape(12.dp))
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Speed, contentDescription = null, tint = NavyMuted, modifier = Modifier.size(16.dp))
              Text(text = "Speed: ${bus.speedMph} mph", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.PinDrop, contentDescription = null, tint = NavyMuted, modifier = Modifier.size(16.dp))
              Text(text = "Next: Stop 4 (${child.stopName})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
          }
        }
      }
    }

    // Bus & Driver Info Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("bus_driver_info_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "ASSIGNED BUS & CERTIFIED DRIVER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
          )
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              Box(
                modifier = Modifier
                  .size(50.dp)
                  .background(NavyPrimary, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = driver.avatarInitials,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
                )
              }
              Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(
                    text = driver.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                  )
                  Icon(
                    Icons.Default.Verified,
                    contentDescription = "Background Verified",
                    tint = StatusBlue,
                    modifier = Modifier.size(16.dp)
                  )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "⭐ ${driver.rating} • ${driver.experienceYears} yrs safe driving",
                  fontSize = 12.sp,
                  color = TextSecondary
                )
                Text(
                  text = "${bus.name} • License Plate: ${bus.plateNumber}",
                  fontSize = 11.sp,
                  color = TextMuted
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = BorderSubtle)
          Spacer(modifier = Modifier.height(12.dp))

          // Action Buttons: Call Driver, Message Dispatch, Report Absence
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { showContactDialog = true },
              modifier = Modifier
                .weight(1f)
                .testTag("contact_driver_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
            ) {
              Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(text = "Call Driver", fontSize = 12.sp)
            }

            Button(
              onClick = { showAbsenceReportDialog = true },
              modifier = Modifier
                .weight(1f)
                .testTag("report_absence_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
              Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(text = "Report Absence", fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Emergency SOS Button
          OutlinedButton(
            onClick = { showSosConfirmDialog = true },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("emergency_sos_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = StatusRedLight,
              contentColor = StatusRed
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed.copy(alpha = 0.4f))
          ) {
            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Emergency Dispatch SOS Assistance", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }

  // Dialogs
  if (showContactDialog) {
    AlertDialog(
      onDismissRequest = { showContactDialog = false },
      title = {
        Text(text = "Contact Transport Dispatch", fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(text = "Driver: ${driver.name} (Direct In-Vehicle Phone)")
          Text(text = "Phone: ${driver.phone}", fontWeight = FontWeight.SemiBold, color = NavyPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "For child safety, drivers cannot answer phone calls while vehicle is actively moving.",
            fontSize = 11.sp,
            color = TextSecondary
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${driver.phone}"))
            context.startActivity(intent)
            showContactDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("Dial Number")
        }
      },
      dismissButton = {
        TextButton(onClick = { showContactDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  if (showSosConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showSosConfirmDialog = false },
      icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed) },
      title = { Text(text = "Trigger Emergency Assistance?", fontWeight = FontWeight.Bold) },
      text = {
        Text(text = "This will immediately alert Oakridge School Dispatch, Driver Marcus Reynolds, and campus security regarding ${child.name}'s safety status.")
      },
      confirmButton = {
        Button(
          onClick = {
            repository.triggerEmergencyBroadcast("Parent reported urgent concern for ${child.name} on ${bus.name}.")
            showSosConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
        ) {
          Text("Send Urgent Alert")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSosConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  if (showAbsenceReportDialog) {
    AlertDialog(
      onDismissRequest = { showAbsenceReportDialog = false },
      title = { Text(text = "Report Child Absence", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(text = "Notify the driver not to stop for ${child.name} today:")
          OutlinedTextField(
            value = absenceReason,
            onValueChange = { absenceReason = it },
            label = { Text("Reason") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            repository.updateStudentBoarding(child.id, com.example.model.BoardingStatus.ABSENT)
            showAbsenceReportDialog = false
            absenceSubmitted = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = SchoolBusYellowDark)
        ) {
          Text("Confirm Absence")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAbsenceReportDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
