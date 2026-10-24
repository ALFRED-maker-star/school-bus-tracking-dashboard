package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.model.BusStop
import com.example.model.Route
import com.example.ui.theme.*

@Composable
fun RoutesSetupScreen(
  repository: SafeRideRepository,
  modifier: Modifier = Modifier
) {
  val routes by repository.routes.collectAsState()
  var showAddRouteDialog by remember { mutableStateOf(false) }
  var routeToAddStopTo by remember { mutableStateOf<Route?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundSlate)
      .testTag("routes_setup_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Header & Add Route Button
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "District Routes Setup",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = NavyDark
          )
          Text(
            text = "Geofenced bus paths and scheduled stop sequences",
            fontSize = 12.sp,
            color = TextSecondary
          )
        }

        Button(
          onClick = { showAddRouteDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("add_route_button")
        ) {
          Icon(Icons.Default.AddRoad, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("New Route", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // List of Defined Routes
    items(routes) { route ->
      val routeColor = try {
        Color(android.graphics.Color.parseColor(route.colorHex))
      } catch (_: Exception) {
        NavyPrimary
      }

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("route_setup_card_${route.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Route title row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Box(
                modifier = Modifier
                  .size(16.dp)
                  .background(routeColor, CircleShape)
              )
              Column {
                Text(
                  text = route.name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = TextPrimary
                )
                Text(
                  text = "${route.schoolName} • ${route.totalMiles} Miles • ~${route.estimatedMinutes} mins",
                  fontSize = 12.sp,
                  color = TextSecondary
                )
              }
            }

            Box(
              modifier = Modifier
                .background(SurfaceContainer, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = "${route.stops.size} Stops",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NavyDark
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = BorderSubtle)
          Spacer(modifier = Modifier.height(10.dp))

          // Stops Ordered Timeline
          Text(
            text = "ORDERED STOPS SEQUENCE:",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp
          )
          Spacer(modifier = Modifier.height(8.dp))

          route.stops.forEach { stop ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                  modifier = Modifier
                    .size(22.dp)
                    .background(routeColor.copy(alpha = 0.15f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = "${stop.order}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = routeColor)
                }
                Text(text = stop.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
              }
              Text(text = "${stop.scheduledMorningTime} / ${stop.scheduledAfternoonTime}", fontSize = 11.sp, color = TextSecondary)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Add Stop button
          OutlinedButton(
            onClick = { routeToAddStopTo = route },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("add_stop_to_route_${route.id}"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Stop to ${route.name}", fontSize = 12.sp)
          }
        }
      }
    }
  }

  // Add Route Dialog
  if (showAddRouteDialog) {
    var routeName by remember { mutableStateOf("Route 4D - River District") }
    var schoolDest by remember { mutableStateOf("Oakridge Elementary") }
    var miles by remember { mutableStateOf("5.8") }

    AlertDialog(
      onDismissRequest = { showAddRouteDialog = false },
      title = { Text(text = "Define New Transit Route", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = routeName,
            onValueChange = { routeName = it },
            label = { Text("Route Name") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = schoolDest,
            onValueChange = { schoolDest = it },
            label = { Text("Destination School") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = miles,
            onValueChange = { miles = it },
            label = { Text("Total Distance (Miles)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val newRoute = Route(
              id = "RT-${(10..99).random()}",
              name = routeName,
              schoolName = schoolDest,
              colorHex = "#8B5CF6", // Purple
              totalMiles = miles.toDoubleOrNull() ?: 5.0,
              estimatedMinutes = 48,
              stops = listOf(
                BusStop("ST-301", "West Terminal", "90 River Road", "07:15 AM", "03:15 PM", 0.12f, 0.45f, 1),
                BusStop("ST-302", "Oakridge Elementary", "500 Horizon Blvd", "08:05 AM", "04:10 PM", 0.45f, 0.85f, 2)
              ),
              isActive = true
            )
            repository.addOrUpdateRoute(newRoute)
            showAddRouteDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("Create Route")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddRouteDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Add Stop Dialog
  routeToAddStopTo?.let { targetRoute ->
    var stopName by remember { mutableStateOf("Highland & 7th St") }
    var address by remember { mutableStateOf("220 Highland Ave") }
    var amTime by remember { mutableStateOf("07:34 AM") }
    var pmTime by remember { mutableStateOf("03:36 PM") }

    AlertDialog(
      onDismissRequest = { routeToAddStopTo = null },
      title = { Text(text = "Add Stop to ${targetRoute.name}", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = stopName,
            onValueChange = { stopName = it },
            label = { Text("Stop Intersection Name") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Street Address") },
            modifier = Modifier.fillMaxWidth()
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = amTime,
              onValueChange = { amTime = it },
              label = { Text("AM Time") },
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = pmTime,
              onValueChange = { pmTime = it },
              label = { Text("PM Time") },
              modifier = Modifier.weight(1f)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val newOrder = targetRoute.stops.size + 1
            val newStop = BusStop(
              id = "ST-${(100..999).random()}",
              name = stopName,
              address = address,
              scheduledMorningTime = amTime,
              scheduledAfternoonTime = pmTime,
              xNorm = 0.5f,
              yNorm = 0.5f,
              order = newOrder
            )
            val updated = targetRoute.copy(stops = targetRoute.stops + newStop)
            repository.addOrUpdateRoute(updated)
            routeToAddStopTo = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("Append Stop")
        }
      },
      dismissButton = {
        TextButton(onClick = { routeToAddStopTo = null }) {
          Text("Cancel")
        }
      }
    )
  }
}
