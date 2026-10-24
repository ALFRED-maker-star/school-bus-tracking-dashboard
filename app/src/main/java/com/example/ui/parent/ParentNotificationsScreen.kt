package com.example.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.NotificationItem
import com.example.model.NotificationType
import com.example.ui.theme.*

@Composable
fun ParentNotificationsScreen(
  repository: SafeRideRepository,
  modifier: Modifier = Modifier
) {
  val notifications by repository.notifications.collectAsState()
  var selectedFilter by remember { mutableStateOf("All") }

  val filteredNotifications = remember(notifications, selectedFilter) {
    when (selectedFilter) {
      "Approaching" -> notifications.filter { it.type == NotificationType.APPROACHING }
      "Delays" -> notifications.filter { it.type == NotificationType.DELAY }
      "Boarding" -> notifications.filter { it.type == NotificationType.BOARDED }
      "Emergency" -> notifications.filter { it.type == NotificationType.EMERGENCY }
      else -> notifications
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundSlate)
      .testTag("parent_notifications_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top summary & actions
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Transit Alerts",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = NavyDark
          )
          Text(
            text = "Real-time safety and movement updates",
            fontSize = 12.sp,
            color = TextSecondary
          )
        }

        TextButton(
          onClick = { repository.markAllNotificationsRead() },
          modifier = Modifier.testTag("mark_all_read_button")
        ) {
          Text("Mark All Read", fontSize = 12.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Filter Chips
    item {
      val filters = listOf("All", "Approaching", "Boarding", "Delays", "Emergency")
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(filters) { filter ->
          FilterChip(
            selected = (selectedFilter == filter),
            onClick = { selectedFilter = filter },
            label = { Text(text = filter, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = NavyPrimary,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("filter_chip_$filter")
          )
        }
      }
    }

    // Empty state
    if (filteredNotifications.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              Icons.Default.NotificationsNone,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No alerts in this category",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = TextPrimary
            )
            Text(
              text = "Everything is running smoothly on Route 3B.",
              fontSize = 12.sp,
              color = TextSecondary
            )
          }
        }
      }
    }

    // Notification Cards
    items(filteredNotifications) { item ->
      NotificationCard(item = item, onClick = { repository.markNotificationRead(item.id) })
    }
  }
}

@Composable
private fun NotificationCard(item: NotificationItem, onClick: () -> Unit) {
  val (icon, iconColor, bgColor) = when (item.type) {
    NotificationType.APPROACHING -> Triple(Icons.Default.NearMe, SchoolBusYellowDark, SchoolBusYellowLight)
    NotificationType.BOARDED -> Triple(Icons.Default.CheckCircle, StatusGreen, StatusGreenLight)
    NotificationType.DEPARTED -> Triple(Icons.Default.DirectionsBus, StatusBlue, StatusBlueLight)
    NotificationType.DELAY -> Triple(Icons.Default.HourglassTop, StatusOrange, Color(0xFFFFF7ED))
    NotificationType.ARRIVED -> Triple(Icons.Default.School, NavyPrimary, SurfaceContainer)
    NotificationType.EMERGENCY -> Triple(Icons.Default.Warning, StatusRed, StatusRedLight)
  }

  Card(
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("notification_card_${item.id}"),
    colors = CardDefaults.cardColors(
      containerColor = if (item.isRead) SurfaceWhite else Color.White
    ),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(
      if (item.isRead) 1.dp else 1.5.dp,
      if (item.isRead) BorderSubtle else SchoolBusYellow
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .background(bgColor, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
      }

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = item.title,
            fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.Bold,
            fontSize = 14.sp,
            color = if (item.type == NotificationType.EMERGENCY) StatusRed else TextPrimary
          )
          Text(
            text = item.timestamp,
            fontSize = 11.sp,
            color = TextMuted
          )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = item.message,
          fontSize = 12.sp,
          color = TextSecondary,
          lineHeight = 16.sp
        )

        if (!item.isRead) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .background(SchoolBusYellowDark, CircleShape)
            )
            Text(
              text = "Unread Alert",
              fontSize = 10.sp,
              color = SchoolBusYellowDark,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
