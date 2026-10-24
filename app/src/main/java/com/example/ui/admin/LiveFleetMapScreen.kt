package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.model.Bus
import com.example.model.BusStatus
import com.example.ui.components.LiveFleetDistrictMap
import com.example.ui.theme.*

@Composable
fun LiveFleetMapScreen(
  repository: SafeRideRepository,
  modifier: Modifier = Modifier
) {
  val buses by repository.buses.collectAsState()
  val routes by repository.routes.collectAsState()
  val drivers by repository.drivers.collectAsState()

  var selectedStatusFilter by remember { mutableStateOf("All") }
  var selectedBusId by remember { mutableStateOf<String?>(buses.firstOrNull()?.id) }

  val filteredBuses = remember(buses, selectedStatusFilter) {
    when (selectedStatusFilter) {
      "En Route" -> buses.filter { it.status == BusStatus.EN_ROUTE }
      "At Stop" -> buses.filter { it.status == BusStatus.AT_STOP }
      "Delayed" -> buses.filter { it.status == BusStatus.DELAYED }
      else -> buses
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundSlate)
      .padding(16.dp)
      .testTag("live_fleet_map_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header & Filter Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "District Live Fleet Radar",
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          color = NavyDark
        )
        Text(
          text = "Multi-vehicle GPS tracking & telematics across all district corridors",
          fontSize = 12.sp,
          color = TextSecondary
        )
      }

      // Live pulse dot
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
          .background(StatusGreenLight, RoundedCornerShape(20.dp))
          .padding(horizontal = 10.dp, vertical = 5.dp)
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(StatusGreen, CircleShape)
        )
        Text(
          text = "Pinging Live",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = StatusGreen
        )
      }
    }

    // Status Filter Chips
    val filters = listOf("All", "En Route", "At Stop", "Delayed")
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(filters) { f ->
        FilterChip(
          selected = (selectedStatusFilter == f),
          onClick = { selectedStatusFilter = f },
          label = { Text(f, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = NavyPrimary,
            selectedLabelColor = Color.White
          ),
          modifier = Modifier.testTag("fleet_filter_$f")
        )
      }
    }

    // High fidelity Interactive Multi-Bus Live Map
    LiveFleetDistrictMap(
      buses = filteredBuses,
      routes = routes,
      drivers = drivers,
      selectedBusId = selectedBusId,
      onSelectBus = { selectedBusId = it.id },
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    )

    // Quick Bus Pills list to select any bus
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(buses) { b ->
        val isSelected = (b.id == selectedBusId)
        Surface(
          onClick = { selectedBusId = b.id },
          color = if (isSelected) NavyPrimary else SurfaceWhite,
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NavyPrimary else BorderSubtle),
          modifier = Modifier.testTag("bus_pill_${b.id}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .background(
                  when (b.status) {
                    BusStatus.EN_ROUTE -> StatusGreen
                    BusStatus.DELAYED -> StatusRed
                    else -> SchoolBusYellow
                  },
                  CircleShape
                )
            )
            Text(
              text = b.name.split("(")[0].trim(),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) Color.White else TextPrimary
            )
            Text(
              text = "${b.speedMph} mph",
              fontSize = 11.sp,
              color = if (isSelected) SchoolBusYellow else TextSecondary
            )
          }
        }
      }
    }
  }
}
