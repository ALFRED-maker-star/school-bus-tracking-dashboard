package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SafeRideRepository
import com.example.model.*
import com.example.ui.admin.*
import com.example.ui.components.SafeRideTopBar
import com.example.ui.driver.*
import com.example.ui.parent.*
import com.example.ui.theme.*

@Composable
fun SafeRideApp(
  repository: SafeRideRepository = SafeRideRepository.getInstance()
) {
  val currentRole by repository.currentRole.collectAsState()
  val parentTab by repository.parentTab.collectAsState()
  val driverTab by repository.driverTab.collectAsState()
  val adminTab by repository.adminTab.collectAsState()
  val notifications by repository.notifications.collectAsState()
  val unreadNotificationsCount = notifications.count { !it.isRead }

  // BackHandler to revert to first tab or home
  BackHandler(enabled = true) {
    when (currentRole) {
      UserRole.PARENT -> {
        if (parentTab != ParentTab.LIVE_TRACKING) {
          repository.setParentTab(ParentTab.LIVE_TRACKING)
        }
      }
      UserRole.DRIVER -> {
        if (driverTab != DriverTab.MY_ROUTE) {
          repository.setDriverTab(DriverTab.MY_ROUTE)
        } else {
          repository.switchRole(UserRole.PARENT)
        }
      }
      UserRole.ADMIN -> {
        if (adminTab != AdminTab.DASHBOARD) {
          repository.setAdminTab(AdminTab.DASHBOARD)
        } else {
          repository.switchRole(UserRole.PARENT)
        }
      }
    }
  }

  Scaffold(
    topBar = {
      SafeRideTopBar(
        currentRole = currentRole,
        onRoleSelected = { role -> repository.switchRole(role) },
        onNotificationClick = {
          repository.switchRole(UserRole.PARENT)
          repository.setParentTab(ParentTab.NOTIFICATIONS)
        },
        unreadCount = unreadNotificationsCount
      )
    },
    bottomBar = {
      when (currentRole) {
        UserRole.PARENT -> ParentBottomNavigation(
          currentTab = parentTab,
          onSelectTab = { repository.setParentTab(it) },
          unreadCount = unreadNotificationsCount
        )
        UserRole.DRIVER -> DriverBottomNavigation(
          currentTab = driverTab,
          onSelectTab = { repository.setDriverTab(it) }
        )
        UserRole.ADMIN -> AdminBottomNavigation(
          currentTab = adminTab,
          onSelectTab = { repository.setAdminTab(it) }
        )
      }
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentRole) {
        UserRole.PARENT -> {
          when (parentTab) {
            ParentTab.LIVE_TRACKING -> ParentHomeScreen(repository)
            ParentTab.CHILD_PROFILE -> ChildProfileScreen(repository)
            ParentTab.ROUTE_INFO -> RouteStopInfoScreen(repository)
            ParentTab.NOTIFICATIONS -> ParentNotificationsScreen(repository)
          }
        }
        UserRole.DRIVER -> {
          when (driverTab) {
            DriverTab.MY_ROUTE -> DriverRouteScreen(repository)
            DriverTab.TRIP_CONTROL -> DriverTripScreen(repository)
            DriverTab.STUDENT_CHECKLIST -> StudentChecklistScreen(repository)
            DriverTab.LOGIN -> DriverLoginScreen(repository)
          }
        }
        UserRole.ADMIN -> {
          when (adminTab) {
            AdminTab.DASHBOARD -> AdminDashboardScreen(repository)
            AdminTab.LIVE_FLEET_MAP -> LiveFleetMapScreen(repository)
            AdminTab.BUSES_DRIVERS -> BusesDriversScreen(repository)
            AdminTab.ROUTES_SETUP -> RoutesSetupScreen(repository)
            AdminTab.STUDENT_ASSIGNMENT -> StudentAssignmentScreen(repository)
            AdminTab.LOGIN -> AdminLoginScreen(repository)
          }
        }
      }
    }
  }
}

@Composable
private fun ParentBottomNavigation(
  currentTab: ParentTab,
  onSelectTab: (ParentTab) -> Unit,
  unreadCount: Int
) {
  NavigationBar(
    containerColor = NavyDark,
    contentColor = Color.White,
    modifier = Modifier
      .testTag("parent_bottom_nav")
      .navigationBarsPadding()
  ) {
    NavigationBarItem(
      selected = (currentTab == ParentTab.LIVE_TRACKING),
      onClick = { onSelectTab(ParentTab.LIVE_TRACKING) },
      icon = { Icon(Icons.Default.Map, contentDescription = "Live Map") },
      label = { Text("Live Map", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = SchoolBusYellow,
        indicatorColor = SchoolBusYellow,
        unselectedIconColor = Color(0xFF94A3B8),
        unselectedTextColor = Color(0xFF94A3B8)
      ),
      modifier = Modifier.testTag("nav_parent_live_tracking")
    )

    NavigationBarItem(
      selected = (currentTab == ParentTab.CHILD_PROFILE),
      onClick = { onSelectTab(ParentTab.CHILD_PROFILE) },
      icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Child Profile") },
      label = { Text("Child", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = SchoolBusYellow,
        indicatorColor = SchoolBusYellow,
        unselectedIconColor = Color(0xFF94A3B8),
        unselectedTextColor = Color(0xFF94A3B8)
      ),
      modifier = Modifier.testTag("nav_parent_child_profile")
    )

    NavigationBarItem(
      selected = (currentTab == ParentTab.ROUTE_INFO),
      onClick = { onSelectTab(ParentTab.ROUTE_INFO) },
      icon = { Icon(Icons.Default.AltRoute, contentDescription = "Route Info") },
      label = { Text("Route", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = SchoolBusYellow,
        indicatorColor = SchoolBusYellow,
        unselectedIconColor = Color(0xFF94A3B8),
        unselectedTextColor = Color(0xFF94A3B8)
      ),
      modifier = Modifier.testTag("nav_parent_route_info")
    )

    NavigationBarItem(
      selected = (currentTab == ParentTab.NOTIFICATIONS),
      onClick = { onSelectTab(ParentTab.NOTIFICATIONS) },
      icon = {
        BadgedBox(badge = {
          if (unreadCount > 0) {
            Badge(containerColor = StatusRed) { Text("$unreadCount") }
          }
        }) {
          Icon(Icons.Default.Notifications, contentDescription = "Alerts")
        }
      },
      label = { Text("Alerts", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = SchoolBusYellow,
        indicatorColor = SchoolBusYellow,
        unselectedIconColor = Color(0xFF94A3B8),
        unselectedTextColor = Color(0xFF94A3B8)
      ),
      modifier = Modifier.testTag("nav_parent_notifications")
    )
  }
}

@Composable
private fun DriverBottomNavigation(
  currentTab: DriverTab,
  onSelectTab: (DriverTab) -> Unit
) {
  NavigationBar(
    containerColor = NavyDark,
    contentColor = Color.White,
    modifier = Modifier
      .testTag("driver_bottom_nav")
      .navigationBarsPadding()
  ) {
    NavigationBarItem(
      selected = (currentTab == DriverTab.MY_ROUTE),
      onClick = { onSelectTab(DriverTab.MY_ROUTE) },
      icon = { Icon(Icons.Default.AltRoute, contentDescription = "My Route") },
      label = { Text("My Route", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = SchoolBusYellow,
        indicatorColor = SchoolBusYellow,
        unselectedIconColor = Color(0xFF94A3B8),
        unselectedTextColor = Color(0xFF94A3B8)
      ),
      modifier = Modifier.testTag("nav_driver_my_route")
    )

    NavigationBarItem(
      selected = (currentTab == DriverTab.TRIP_CONTROL),
      onClick = { onSelectTab(DriverTab.TRIP_CONTROL) },
      icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Trip Run") },
      label = { Text("Trip Run", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = SchoolBusYellow,
        indicatorColor = SchoolBusYellow,
        unselectedIconColor = Color(0xFF94A3B8),
        unselectedTextColor = Color(0xFF94A3B8)
      ),
      modifier = Modifier.testTag("nav_driver_trip_control")
    )

    NavigationBarItem(
      selected = (currentTab == DriverTab.STUDENT_CHECKLIST),
      onClick = { onSelectTab(DriverTab.STUDENT_CHECKLIST) },
      icon = { Icon(Icons.Default.Checklist, contentDescription = "Students") },
      label = { Text("Students", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = SchoolBusYellow,
        indicatorColor = SchoolBusYellow,
        unselectedIconColor = Color(0xFF94A3B8),
        unselectedTextColor = Color(0xFF94A3B8)
      ),
      modifier = Modifier.testTag("nav_driver_students")
    )

    NavigationBarItem(
      selected = (currentTab == DriverTab.LOGIN),
      onClick = { onSelectTab(DriverTab.LOGIN) },
      icon = { Icon(Icons.Default.Badge, contentDescription = "Shift Profile") },
      label = { Text("Shift", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = SchoolBusYellow,
        indicatorColor = SchoolBusYellow,
        unselectedIconColor = Color(0xFF94A3B8),
        unselectedTextColor = Color(0xFF94A3B8)
      ),
      modifier = Modifier.testTag("nav_driver_profile")
    )
  }
}

@Composable
private fun AdminBottomNavigation(
  currentTab: AdminTab,
  onSelectTab: (AdminTab) -> Unit
) {
  NavigationBar(
    containerColor = NavyDark,
    contentColor = Color.White,
    modifier = Modifier
      .testTag("admin_bottom_nav")
      .navigationBarsPadding()
  ) {
    NavigationBarItem(
      selected = (currentTab == AdminTab.DASHBOARD),
      onClick = { onSelectTab(AdminTab.DASHBOARD) },
      icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
      label = { Text("Overview", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = SchoolBusYellow,
        indicatorColor = SchoolBusYellow,
        unselectedIconColor = Color(0xFF94A3B8),
        unselectedTextColor = Color(0xFF94A3B8)
      ),
      modifier = Modifier.testTag("nav_admin_dashboard")
    )

    NavigationBarItem(
      selected = (currentTab == AdminTab.LIVE_FLEET_MAP),
      onClick = { onSelectTab(AdminTab.LIVE_FLEET_MAP) },
      icon = { Icon(Icons.Default.Map, contentDescription = "Live Fleet") },
      label = { Text("Live Fleet", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = SchoolBusYellow,
        indicatorColor = SchoolBusYellow,
        unselectedIconColor = Color(0xFF94A3B8),
        unselectedTextColor = Color(0xFF94A3B8)
      ),
      modifier = Modifier.testTag("nav_admin_fleet_map")
    )

    NavigationBarItem(
      selected = (currentTab == AdminTab.BUSES_DRIVERS),
      onClick = { onSelectTab(AdminTab.BUSES_DRIVERS) },
      icon = { Icon(Icons.Default.DirectionsBus, contentDescription = "Buses & Staff") },
      label = { Text("Buses", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = SchoolBusYellow,
        indicatorColor = SchoolBusYellow,
        unselectedIconColor = Color(0xFF94A3B8),
        unselectedTextColor = Color(0xFF94A3B8)
      ),
      modifier = Modifier.testTag("nav_admin_buses")
    )

    NavigationBarItem(
      selected = (currentTab == AdminTab.ROUTES_SETUP),
      onClick = { onSelectTab(AdminTab.ROUTES_SETUP) },
      icon = { Icon(Icons.Default.AltRoute, contentDescription = "Routes Setup") },
      label = { Text("Routes", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = SchoolBusYellow,
        indicatorColor = SchoolBusYellow,
        unselectedIconColor = Color(0xFF94A3B8),
        unselectedTextColor = Color(0xFF94A3B8)
      ),
      modifier = Modifier.testTag("nav_admin_routes")
    )

    NavigationBarItem(
      selected = (currentTab == AdminTab.STUDENT_ASSIGNMENT),
      onClick = { onSelectTab(AdminTab.STUDENT_ASSIGNMENT) },
      icon = { Icon(Icons.Default.People, contentDescription = "Students") },
      label = { Text("Students", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = SchoolBusYellow,
        indicatorColor = SchoolBusYellow,
        unselectedIconColor = Color(0xFF94A3B8),
        unselectedTextColor = Color(0xFF94A3B8)
      ),
      modifier = Modifier.testTag("nav_admin_students")
    )
  }
}
