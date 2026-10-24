package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SafeRideRepository
import com.example.model.AdminTab
import com.example.model.BusStatus
import com.example.model.NotificationType
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
  repository: SafeRideRepository,
  modifier: Modifier = Modifier
) {
  val buses by repository.buses.collectAsState()
  val students by repository.students.collectAsState()
  val routes by repository.routes.collectAsState()
  val notifications by repository.notifications.collectAsState()

  val activeBuses = buses.count { it.status == BusStatus.EN_ROUTE || it.status == BusStatus.AT_STOP }
  val totalBoardedStudents = students.count { it.boardingStatus == com.example.model.BoardingStatus.BOARDED }
  val emergencyAlerts = notifications.filter { it.type == NotificationType.EMERGENCY }

  var showBroadcastDialog by remember { mutableStateOf(false) }
  var broadcastText by remember { mutableStateOf("") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundSlate)
      .testTag("admin_dashboard_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Overview Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "District Transit Overview",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = NavyDark
          )
          Text(
            text = "Oakridge Central School District • Real-Time Dispatch",
            fontSize = 12.sp,
            color = TextSecondary
          )
        }

        Button(
          onClick = { showBroadcastDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("admin_broadcast_button")
        ) {
          Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("District Alert", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Four Stat Cards (Responsive Grid)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          StatCard(
            title = "Active Buses",
            value = "$activeBuses / ${buses.size}",
            subtitle = "Live on district routes",
            icon = Icons.Default.DirectionsBus,
            accentColor = SchoolBusYellowDark,
            modifier = Modifier.weight(1f).testTag("stat_active_buses")
          )
          StatCard(
            title = "Total Students",
            value = "$totalBoardedStudents / ${students.size}",
            subtitle = "Checked in / on board",
            icon = Icons.Default.People,
            accentColor = StatusBlue,
            modifier = Modifier.weight(1f).testTag("stat_total_students")
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          StatCard(
            title = "On-Time Rate",
            value = "96.4%",
            subtitle = "Morning schedule compliance",
            icon = Icons.Default.Timer,
            accentColor = StatusGreen,
            modifier = Modifier.weight(1f).testTag("stat_ontime_rate")
          )
          StatCard(
            title = "Active Alerts",
            value = "${emergencyAlerts.size}",
            subtitle = if (emergencyAlerts.isNotEmpty()) "Requires attention" else "No incidents",
            icon = Icons.Default.Warning,
            accentColor = if (emergencyAlerts.isNotEmpty()) StatusRed else NavyMuted,
            modifier = Modifier.weight(1f).testTag("stat_active_alerts")
          )
        }
      }
    }

    // Emergency Alert Banner if any
    if (emergencyAlerts.isNotEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = StatusRedLight),
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusRed)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Default.Error, contentDescription = null, tint = StatusRed)
              Text(
                text = "DISPATCH ATTENTION REQUIRED",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = StatusRed
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = emergencyAlerts.first().message,
              fontSize = 12.sp,
              color = TextPrimary,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }

    // Quick Admin Navigation Launchers
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "DISPATCH TERMINAL MODULES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
          )
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { repository.setAdminTab(AdminTab.LIVE_FLEET_MAP) },
              modifier = Modifier.weight(1f).height(48.dp).testTag("launch_fleet_map_button"),
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Live Fleet Map", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = { repository.setAdminTab(AdminTab.BUSES_DRIVERS) },
              modifier = Modifier.weight(1f).height(48.dp).testTag("launch_buses_drivers_button"),
              colors = ButtonDefaults.buttonColors(containerColor = SchoolBusYellowDark),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.DirectionsBus, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Buses & Drivers", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { repository.setAdminTab(AdminTab.ROUTES_SETUP) },
              modifier = Modifier.weight(1f).height(46.dp).testTag("launch_routes_setup_button"),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Route Setup", fontSize = 12.sp)
            }

            OutlinedButton(
              onClick = { repository.setAdminTab(AdminTab.STUDENT_ASSIGNMENT) },
              modifier = Modifier.weight(1f).height(46.dp).testTag("launch_student_assignment_button"),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Student Roster", fontSize = 12.sp)
            }
          }
        }
      }
    }

    // Active Fleet Table Preview
    item {
      Text(
        text = "ACTIVE FLEET STATUS SNAPSHOT",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 4.dp)
      )
    }

    items(buses) { b ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(SchoolBusYellowLight, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(text = "🚌", fontSize = 16.sp)
            }
            Column {
              Text(text = b.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
              Text(text = "Plate: ${b.plateNumber} • Speed: ${b.speedMph} mph", fontSize = 11.sp, color = TextSecondary)
            }
          }

          StatusBadge(status = b.status)
        }
      }
    }
  }

  // Emergency Broadcast Dialog
  if (showBroadcastDialog) {
    AlertDialog(
      onDismissRequest = { showBroadcastDialog = false },
      title = { Text(text = "District Emergency Alert Broadcast", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(text = "This notification will be transmitted instantly to all parent devices and driver in-cab terminals:")
          OutlinedTextField(
            value = broadcastText,
            onValueChange = { broadcastText = it },
            placeholder = { Text("e.g. Severe weather delay / Route 3B detour...") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (broadcastText.isNotBlank()) {
              repository.triggerEmergencyBroadcast(broadcastText)
              showBroadcastDialog = false
              broadcastText = ""
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
        ) {
          Text("Broadcast Alert")
        }
      },
      dismissButton = {
        TextButton(onClick = { showBroadcastDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
