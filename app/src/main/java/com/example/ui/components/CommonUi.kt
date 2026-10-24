package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoardingStatus
import com.example.model.BusStatus
import com.example.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeRideTopBar(
  currentRole: UserRole,
  onRoleSelected: (UserRole) -> Unit,
  onNotificationClick: (() -> Unit)? = null,
  unreadCount: Int = 0,
  modifier: Modifier = Modifier
) {
  var showRoleMenu by remember { mutableStateOf(false) }

  TopAppBar(
    modifier = modifier.testTag("saferide_top_bar"),
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = NavyDark,
      titleContentColor = Color.White
    ),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .background(SchoolBusYellow, RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "🚌", fontSize = 16.sp)
        }
        Column {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "SafeRide",
              fontWeight = FontWeight.Black,
              fontSize = 17.sp,
              color = Color.White
            )
            Box(
              modifier = Modifier
                .background(SchoolBusYellow.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = currentRole.label.uppercase(),
                color = SchoolBusYellow,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
          Text(
            text = "School Transit Safety",
            fontSize = 10.sp,
            color = Color(0xFF94A3B8)
          )
        }
      }
    },
    actions = {
      // Role Switcher Button
      Box {
        Surface(
          onClick = { showRoleMenu = true },
          color = NavyPrimary,
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier.testTag("role_switcher_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = when (currentRole) {
                UserRole.PARENT -> Icons.Default.FamilyRestroom
                UserRole.DRIVER -> Icons.Default.DirectionsBus
                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
              },
              contentDescription = null,
              tint = SchoolBusYellow,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Switch",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Icon(
              Icons.Default.ArrowDropDown,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        DropdownMenu(
          expanded = showRoleMenu,
          onDismissRequest = { showRoleMenu = false },
          modifier = Modifier.background(NavyPrimary)
        ) {
          UserRole.values().forEach { role ->
            DropdownMenuItem(
              text = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(
                    imageVector = when (role) {
                      UserRole.PARENT -> Icons.Default.FamilyRestroom
                      UserRole.DRIVER -> Icons.Default.DirectionsBus
                      UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                    },
                    contentDescription = null,
                    tint = if (role == currentRole) SchoolBusYellow else Color.White,
                    modifier = Modifier.size(18.dp)
                  )
                  Text(
                    text = role.label,
                    color = if (role == currentRole) SchoolBusYellow else Color.White,
                    fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal
                  )
                }
              },
              onClick = {
                showRoleMenu = false
                onRoleSelected(role)
              },
              modifier = Modifier.testTag("switch_to_${role.name.lowercase()}")
            )
          }
        }
      }

      // Notifications Bell (if provided)
      if (onNotificationClick != null) {
        IconButton(
          onClick = onNotificationClick,
          modifier = Modifier.testTag("top_bar_notifications_button")
        ) {
          BadgedBox(
            badge = {
              if (unreadCount > 0) {
                Badge(containerColor = StatusRed) {
                  Text(text = "$unreadCount")
                }
              }
            }
          ) {
            Icon(
              Icons.Default.Notifications,
              contentDescription = "Alerts",
              tint = Color.White
            )
          }
        }
      }
    }
  )
}

@Composable
fun StatCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title.uppercase(),
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 0.5.sp
        )
        Box(
          modifier = Modifier
            .size(32.dp)
            .background(accentColor.copy(alpha = 0.15f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        fontSize = 24.sp,
        fontWeight = FontWeight.Black,
        color = TextPrimary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = TextSecondary
      )
    }
  }
}

@Composable
fun StatusBadge(status: BusStatus) {
  val (bgColor, textColor) = when (status) {
    BusStatus.EN_ROUTE -> Pair(StatusGreenLight, StatusGreen)
    BusStatus.AT_STOP -> Pair(SchoolBusYellowLight, SchoolBusYellowDark)
    BusStatus.DELAYED -> Pair(StatusRedLight, StatusRed)
    BusStatus.IDLE -> Pair(SurfaceContainer, TextSecondary)
    BusStatus.COMPLETED -> Pair(StatusBlueLight, StatusBlue)
  }

  Box(
    modifier = Modifier
      .background(bgColor, RoundedCornerShape(6.dp))
      .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Text(
      text = status.label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = textColor
    )
  }
}

@Composable
fun BoardingBadge(status: BoardingStatus) {
  val (bgColor, textColor) = when (status) {
    BoardingStatus.BOARDED -> Pair(StatusGreenLight, StatusGreen)
    BoardingStatus.WAITING -> Pair(SchoolBusYellowLight, SchoolBusYellowDark)
    BoardingStatus.MISSING -> Pair(StatusRedLight, StatusRed)
    BoardingStatus.ABSENT -> Pair(SurfaceContainer, TextMuted)
  }

  Box(
    modifier = Modifier
      .background(bgColor, RoundedCornerShape(6.dp))
      .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Text(
      text = status.label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = textColor
    )
  }
}
