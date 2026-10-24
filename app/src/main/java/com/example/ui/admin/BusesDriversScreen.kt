package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SafeRideRepository
import com.example.model.Bus
import com.example.model.BusStatus
import com.example.model.Driver
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun BusesDriversScreen(
  repository: SafeRideRepository,
  modifier: Modifier = Modifier
) {
  val buses by repository.buses.collectAsState()
  val drivers by repository.drivers.collectAsState()
  val routes by repository.routes.collectAsState()

  var showAddBusDialog by remember { mutableStateOf(false) }
  var busToEditDriver by remember { mutableStateOf<Bus?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundSlate)
      .testTag("buses_drivers_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header & Add Bus Button
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Buses & Certified Drivers",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = NavyDark
          )
          Text(
            text = "${buses.size} Fleet Vehicles • ${drivers.size} Licensed Staff",
            fontSize = 12.sp,
            color = TextSecondary
          )
        }

        Button(
          onClick = { showAddBusDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("add_bus_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Bus", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Bus Cards List
    items(buses) { bus ->
      val driver = drivers.firstOrNull { it.id == bus.driverId }
      val route = routes.firstOrNull { it.id == bus.routeId }

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("bus_card_${bus.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Bus Title & Status
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .background(SchoolBusYellowLight, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(text = "🚌", fontSize = 20.sp)
              }
              Column {
                Text(
                  text = bus.name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = TextPrimary
                )
                Text(
                  text = "Plate: ${bus.plateNumber} • Capacity: ${bus.capacity} seats",
                  fontSize = 11.sp,
                  color = TextSecondary
                )
              }
            }

            StatusBadge(status = bus.status)
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = BorderSubtle)
          Spacer(modifier = Modifier.height(12.dp))

          // Driver & Route Info
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "ASSIGNED DRIVER",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
              )
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = driver?.name ?: "Unassigned Driver",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = NavyDark
                )
                if (driver != null) {
                  Icon(Icons.Default.Verified, contentDescription = null, tint = StatusBlue, modifier = Modifier.size(14.dp))
                }
              }
              Text(
                text = driver?.phone ?: "No contact phone",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "ROUTE ASSIGNMENT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
              )
              Text(
                text = route?.name ?: "None",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = SchoolBusYellowDark
              )
              Text(
                text = "Battery: ${bus.batteryFuelLevel}%",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Action Button: Reassign / Edit Driver
          OutlinedButton(
            onClick = { busToEditDriver = bus },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reassign_driver_${bus.id}"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Assign / Edit Driver & Route", fontSize = 12.sp)
          }
        }
      }
    }
  }

  // Add Bus Modal
  if (showAddBusDialog) {
    var newBusName by remember { mutableStateOf("Bus 29 (Golden Eagle)") }
    var newPlate by remember { mutableStateOf("SCL-9912") }
    var newCapacity by remember { mutableStateOf("36") }

    AlertDialog(
      onDismissRequest = { showAddBusDialog = false },
      title = { Text(text = "Add New District School Bus", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = newBusName,
            onValueChange = { newBusName = it },
            label = { Text("Bus Name / Call-Sign") },
            modifier = Modifier.fillMaxWidth().testTag("new_bus_name_input")
          )
          OutlinedTextField(
            value = newPlate,
            onValueChange = { newPlate = it },
            label = { Text("State License Plate") },
            modifier = Modifier.fillMaxWidth().testTag("new_bus_plate_input")
          )
          OutlinedTextField(
            value = newCapacity,
            onValueChange = { newCapacity = it },
            label = { Text("Seating Capacity") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("new_bus_capacity_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val newBus = Bus(
              id = "BUS-${(10..99).random()}",
              name = newBusName,
              plateNumber = newPlate,
              driverId = drivers.first().id,
              routeId = routes.first().id,
              capacity = newCapacity.toIntOrNull() ?: 36,
              studentsOnBoard = 0,
              status = BusStatus.IDLE,
              speedMph = 0,
              currentProgress = 0f,
              currentStopIndex = 0,
              etaMinutesToChildStop = 40,
              batteryFuelLevel = 100,
              lastPing = "Ready for service"
            )
            repository.addOrUpdateBus(newBus)
            showAddBusDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("Save Bus")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddBusDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Edit / Reassign Driver Modal
  busToEditDriver?.let { bus ->
    var selectedDriverId by remember { mutableStateOf(bus.driverId) }
    var selectedRouteId by remember { mutableStateOf(bus.routeId) }

    AlertDialog(
      onDismissRequest = { busToEditDriver = null },
      title = { Text(text = "Assign Driver for ${bus.name}", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(text = "Select Certified Staff Member:", fontSize = 12.sp, color = TextSecondary)
          drivers.forEach { d ->
            Surface(
              onClick = { selectedDriverId = d.id },
              color = if (selectedDriverId == d.id) SchoolBusYellowLight else SurfaceContainer,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(text = d.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                  Text(text = "Rating: ⭐ ${d.rating} • ${d.licenseNumber}", fontSize = 11.sp, color = TextSecondary)
                }
                if (selectedDriverId == d.id) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = SchoolBusYellowDark)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            repository.addOrUpdateBus(
              bus.copy(driverId = selectedDriverId, routeId = selectedRouteId)
            )
            busToEditDriver = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("Save Assignment")
        }
      },
      dismissButton = {
        TextButton(onClick = { busToEditDriver = null }) {
          Text("Cancel")
        }
      }
    )
  }
}
