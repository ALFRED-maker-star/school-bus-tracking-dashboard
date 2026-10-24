package com.example.data

import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SafeRideRepository private constructor() {

  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  // Navigation & Role State
  private val _currentRole = MutableStateFlow(UserRole.PARENT)
  val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

  private val _parentTab = MutableStateFlow(ParentTab.LIVE_TRACKING)
  val parentTab: StateFlow<ParentTab> = _parentTab.asStateFlow()

  private val _driverTab = MutableStateFlow(DriverTab.MY_ROUTE)
  val driverTab: StateFlow<DriverTab> = _driverTab.asStateFlow()

  private val _adminTab = MutableStateFlow(AdminTab.DASHBOARD)
  val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

  // Authentication State
  private val _isDriverLoggedIn = MutableStateFlow(true) // Pre-authenticated for instant demo
  val isDriverLoggedIn: StateFlow<Boolean> = _isDriverLoggedIn.asStateFlow()

  private val _isAdminLoggedIn = MutableStateFlow(true) // Pre-authenticated for instant demo
  val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

  // Active Trip State
  private val _isTripActive = MutableStateFlow(true)
  val isTripActive: StateFlow<Boolean> = _isTripActive.asStateFlow()

  private val _isSafetySweepDone = MutableStateFlow(false)
  val isSafetySweepDone: StateFlow<Boolean> = _isSafetySweepDone.asStateFlow()

  // Master Initial Data
  private val defaultStopsRoute3B = listOf(
    BusStop("ST-1", "Central Bus Depot", "100 Transport Way", "07:15 AM", "03:15 PM", 0.10f, 0.20f, 1),
    BusStop("ST-2", "Maple Grove Ave", "142 Maple Grove Ave", "07:22 AM", "03:22 PM", 0.25f, 0.35f, 2),
    BusStop("ST-3", "Oak & Pine Crossing", "380 Pine Crest Rd", "07:30 AM", "03:30 PM", 0.42f, 0.25f, 3),
    BusStop("ST-4", "Elm St & 4th Ave", "844 Elm Street", "07:38 AM", "03:42 PM", 0.60f, 0.50f, 4, isChildStop = true, studentIds = listOf("STU-1", "STU-2", "STU-3")),
    BusStop("ST-5", "Cedar Ridge Blvd", "1200 Cedar Ridge", "07:45 AM", "03:50 PM", 0.75f, 0.40f, 5),
    BusStop("ST-6", "Hillside Park Gate", "45 Parkview Terrace", "07:52 AM", "03:58 PM", 0.85f, 0.65f, 6),
    BusStop("ST-7", "West End Plaza", "890 West End Ave", "07:58 AM", "04:05 PM", 0.70f, 0.80f, 7),
    BusStop("ST-8", "Oakridge Elementary", "500 Horizon Blvd", "08:08 AM", "04:15 PM", 0.45f, 0.85f, 8)
  )

  private val defaultStopsRoute1A = listOf(
    BusStop("ST-101", "East Bus Depot", "200 East Rail Rd", "07:10 AM", "03:10 PM", 0.15f, 0.75f, 1),
    BusStop("ST-102", "Sunrise Valley", "55 Meadow Lane", "07:25 AM", "03:25 PM", 0.30f, 0.60f, 2),
    BusStop("ST-103", "Brookside Estates", "110 Brook Way", "07:40 AM", "03:40 PM", 0.50f, 0.70f, 3),
    BusStop("ST-104", "Oakridge Elementary", "500 Horizon Blvd", "08:00 AM", "04:00 PM", 0.45f, 0.85f, 4)
  )

  private val defaultStopsRoute2C = listOf(
    BusStop("ST-201", "South Terminus", "400 South Expwy", "07:12 AM", "03:12 PM", 0.20f, 0.88f, 1),
    BusStop("ST-202", "Willow Springs", "30 Willow Way", "07:28 AM", "03:30 PM", 0.40f, 0.82f, 2),
    BusStop("ST-203", "Lakeside Commons", "78 Harbor Dr", "07:44 AM", "03:48 PM", 0.65f, 0.88f, 3),
    BusStop("ST-204", "Oakridge Elementary", "500 Horizon Blvd", "08:02 AM", "04:10 PM", 0.45f, 0.85f, 4)
  )

  private val _routes = MutableStateFlow(
    listOf(
      Route("RT-3B", "Route 3B - North Loop", "Oakridge Elementary", "#F59E0B", 6.4, 53, defaultStopsRoute3B, true),
      Route("RT-1A", "Route 1A - East Hills", "Oakridge Elementary", "#3B82F6", 5.2, 45, defaultStopsRoute1A, true),
      Route("RT-2C", "Route 2C - South Valley", "Oakridge Elementary", "#10B981", 7.1, 58, defaultStopsRoute2C, true)
    )
  )
  val routes: StateFlow<List<Route>> = _routes.asStateFlow()

  private val _drivers = MutableStateFlow(
    listOf(
      Driver("DRV-101", "Marcus Reynolds", "(555) 234-8910", "marcus.r@saferide.school", 4.9f, 8, "CDL-992384", "BUS-14", true, "MR"),
      Driver("DRV-102", "Elena Rostova", "(555) 891-4432", "elena.r@saferide.school", 4.8f, 5, "CDL-441029", "BUS-07", true, "ER"),
      Driver("DRV-103", "David Kim", "(555) 302-8871", "david.k@saferide.school", 5.0f, 11, "CDL-119842", "BUS-22", true, "DK"),
      Driver("DRV-104", "Angela Gomez", "(555) 774-2198", "angela.g@saferide.school", 4.9f, 6, "CDL-883719", "BUS-03", false, "AG")
    )
  )
  val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

  private val _buses = MutableStateFlow(
    listOf(
      Bus("BUS-14", "Bus 14 (The Bumblebee)", "SCL-4029", "DRV-101", "RT-3B", 36, 18, BusStatus.EN_ROUTE, 24, 0.48f, 3, 6, 88, "Just now"),
      Bus("BUS-07", "Bus 07 (Falcon Blue)", "SCL-1104", "DRV-102", "RT-1A", 42, 26, BusStatus.EN_ROUTE, 19, 0.65f, 2, 11, 74, "1 min ago"),
      Bus("BUS-22", "Bus 22 (Emerald Cruiser)", "SCL-7721", "DRV-103", "RT-2C", 38, 22, BusStatus.AT_STOP, 0, 0.35f, 1, 15, 92, "Just now"),
      Bus("BUS-03", "Bus 03 (Morning Lark)", "SCL-3390", "DRV-104", "RT-3B", 36, 0, BusStatus.IDLE, 0, 0.0f, 0, 45, 98, "5 min ago")
    )
  )
  val buses: StateFlow<List<Bus>> = _buses.asStateFlow()

  private val _students = MutableStateFlow(
    listOf(
      Student("STU-1", "Leo Davis", "Grade 3 • Room 12", "👦", "BUS-14", "RT-3B", "ST-4", "Elm St & 4th Ave", "Oakridge Elementary", "Sarah Davis (Mom) • (555) 349-1120", "Carries EpiPen, Seat near front", BoardingStatus.WAITING),
      Student("STU-2", "Sophia Patel", "Grade 4 • Room 18", "👧", "BUS-14", "RT-3B", "ST-4", "Elm St & 4th Ave", "Oakridge Elementary", "Anita Patel (Mother) • (555) 441-2099", "No dietary alerts", BoardingStatus.WAITING),
      Student("STU-3", "Lucas Miller", "Grade 2 • Room 07", "🧒", "BUS-14", "RT-3B", "ST-4", "Elm St & 4th Ave", "Oakridge Elementary", "Dan Miller (Father) • (555) 782-9011", "Asthma inhaler in backpack", BoardingStatus.WAITING),
      Student("STU-4", "Emma Johnson", "Grade 5 • Room 24", "👧", "BUS-14", "RT-3B", "ST-2", "Maple Grove Ave", "Oakridge Elementary", "Jessica Johnson • (555) 991-3829", "Safety patrol sash", BoardingStatus.BOARDED, "07:23 AM"),
      Student("STU-5", "Liam Smith", "Grade 3 • Room 12", "👦", "BUS-14", "RT-3B", "ST-3", "Oak & Pine Crossing", "Oakridge Elementary", "Karen Smith • (555) 662-8190", "Allergic to peanuts", BoardingStatus.BOARDED, "07:31 AM"),
      Student("STU-6", "Chloe Brown", "Grade 1 • Room 03", "👧", "BUS-14", "RT-3B", "ST-5", "Cedar Ridge Blvd", "Oakridge Elementary", "Peter Brown • (555) 431-8902", "First grade buddy tag", BoardingStatus.WAITING),
      Student("STU-7", "Noah Garcia", "Grade 4 • Room 18", "👦", "BUS-07", "RT-1A", "ST-102", "Sunrise Valley", "Oakridge Elementary", "Maria Garcia • (555) 120-4491", "Standard rider", BoardingStatus.BOARDED, "07:26 AM"),
      Student("STU-8", "Mia Williams", "Grade 2 • Room 07", "👧", "BUS-22", "RT-2C", "ST-202", "Willow Springs", "Oakridge Elementary", "Tom Williams • (555) 887-3401", "Standard rider", BoardingStatus.WAITING)
    )
  )
  val students: StateFlow<List<Student>> = _students.asStateFlow()

  private val _notifications = MutableStateFlow(
    listOf(
      NotificationItem("NT-1", "Bus Approaching Stop", "Bus #14 is 2 stops away (~6 mins to Elm St & 4th Ave). Please have Leo ready!", "07:32 AM", NotificationType.APPROACHING, false),
      NotificationItem("NT-2", "Route 3B Departed", "Driver Marcus Reynolds started Route 3B from Central Bus Depot on schedule.", "07:15 AM", NotificationType.DEPARTED, true),
      NotificationItem("NT-3", "Morning Schedule On-Time", "Traffic conditions are normal today. Expected school arrival: 08:08 AM.", "07:05 AM", NotificationType.BOARDED, true),
      NotificationItem("NT-4", "Yesterday Afternoon Arrival", "Leo was safely dropped off at Elm St & 4th Ave at 03:43 PM.", "Yesterday", NotificationType.ARRIVED, true)
    )
  )
  val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

  // Selected child for Parent View
  private val _selectedChildId = MutableStateFlow("STU-1")
  val selectedChildId: StateFlow<String> = _selectedChildId.asStateFlow()

  // Live simulation ticker for bus movement
  init {
    coroutineScope.launch {
      while (true) {
        delay(4000)
        if (_isTripActive.value) {
          advanceBusSimulation()
        }
      }
    }
  }

  private fun advanceBusSimulation() {
    _buses.value = _buses.value.map { bus ->
      if (bus.id == "BUS-14" && bus.status == BusStatus.EN_ROUTE) {
        val newProgress = (bus.currentProgress + 0.04f).let { if (it > 0.95f) 0.15f else it }
        val newEta = (12 * (0.60f - newProgress).coerceAtLeast(0.05f) / 0.60f).toInt() + 1
        bus.copy(
          currentProgress = newProgress,
          etaMinutesToChildStop = newEta,
          speedMph = if (newProgress in 0.58f..0.62f) 0 else (22..28).random(),
          status = if (newProgress in 0.58f..0.62f) BusStatus.AT_STOP else BusStatus.EN_ROUTE,
          lastPing = "Just now"
        )
      } else if (bus.id == "BUS-07") {
        val p = (bus.currentProgress + 0.03f).let { if (it > 0.95f) 0.1f else it }
        bus.copy(currentProgress = p, lastPing = "Just now")
      } else {
        bus
      }
    }
  }

  // Role switching
  fun switchRole(role: UserRole) {
    _currentRole.value = role
  }

  fun setParentTab(tab: ParentTab) {
    _parentTab.value = tab
  }

  fun setDriverTab(tab: DriverTab) {
    _driverTab.value = tab
  }

  fun setAdminTab(tab: AdminTab) {
    _adminTab.value = tab
  }

  // Driver actions
  fun loginDriver(driverId: String) {
    _isDriverLoggedIn.value = true
    _driverTab.value = DriverTab.MY_ROUTE
  }

  fun logoutDriver() {
    _isDriverLoggedIn.value = false
    _driverTab.value = DriverTab.LOGIN
  }

  fun startDriverTrip() {
    _isTripActive.value = true
    _buses.value = _buses.value.map {
      if (it.id == "BUS-14") it.copy(status = BusStatus.EN_ROUTE, speedMph = 25) else it
    }
    addNotification(
      NotificationItem(
        id = UUID.randomUUID().toString(),
        title = "Driver Started Route 3B",
        message = "Bus #14 live GPS sharing is now broadcasting to parents and school dispatch.",
        timestamp = getCurrentTime(),
        type = NotificationType.DEPARTED
      )
    )
  }

  fun pauseTrip() {
    _isTripActive.value = false
    _buses.value = _buses.value.map {
      if (it.id == "BUS-14") it.copy(status = BusStatus.DELAYED, speedMph = 0) else it
    }
    addNotification(
      NotificationItem(
        id = UUID.randomUUID().toString(),
        title = "Traffic Delay Reported",
        message = "Bus #14 paused temporarily due to road obstruction. Expected +5 min delay.",
        timestamp = getCurrentTime(),
        type = NotificationType.DELAY
      )
    )
  }

  fun endDriverTrip(sweepDone: Boolean) {
    _isSafetySweepDone.value = sweepDone
    _isTripActive.value = false
    _buses.value = _buses.value.map {
      if (it.id == "BUS-14") it.copy(status = BusStatus.COMPLETED, speedMph = 0, currentProgress = 1.0f) else it
    }
    addNotification(
      NotificationItem(
        id = UUID.randomUUID().toString(),
        title = "Route 3B Completed Safely",
        message = "Bus #14 arrived at Oakridge Elementary. Safety seat sweep confirmed complete.",
        timestamp = getCurrentTime(),
        type = NotificationType.ARRIVED
      )
    )
  }

  fun updateStudentBoarding(studentId: String, status: BoardingStatus) {
    val time = if (status == BoardingStatus.BOARDED) getCurrentTime() else null
    _students.value = _students.value.map {
      if (it.id == studentId) it.copy(boardingStatus = status, boardedAtTime = time) else it
    }

    val student = _students.value.firstOrNull { it.id == studentId } ?: return
    when (status) {
      BoardingStatus.BOARDED -> {
        addNotification(
          NotificationItem(
            id = UUID.randomUUID().toString(),
            title = "${student.name} Boarded Bus #14",
            message = "${student.name} safely scanned badge and boarded at ${student.stopName} at ${time ?: "now"}.",
            timestamp = getCurrentTime(),
            type = NotificationType.BOARDED
          )
        )
      }
      BoardingStatus.MISSING -> {
        addNotification(
          NotificationItem(
            id = UUID.randomUUID().toString(),
            title = "⚠️ Student Flagged Missing: ${student.name}",
            message = "Driver Marcus reported ${student.name} was not present at ${student.stopName}. Parent & Dispatch notified.",
            timestamp = getCurrentTime(),
            type = NotificationType.EMERGENCY
          )
        )
      }
      BoardingStatus.ABSENT -> {
        addNotification(
          NotificationItem(
            id = UUID.randomUUID().toString(),
            title = "Absence Marked: ${student.name}",
            message = "Marked as excused absent for today's ride.",
            timestamp = getCurrentTime(),
            type = NotificationType.BOARDED
          )
        )
      }
      BoardingStatus.WAITING -> {}
    }
  }

  // Admin Actions
  fun loginAdmin(user: String) {
    _isAdminLoggedIn.value = true
    _adminTab.value = AdminTab.DASHBOARD
  }

  fun logoutAdmin() {
    _isAdminLoggedIn.value = false
    _adminTab.value = AdminTab.LOGIN
  }

  fun addOrUpdateBus(bus: Bus) {
    val existing = _buses.value.indexOfFirst { it.id == bus.id }
    if (existing >= 0) {
      _buses.value = _buses.value.toMutableList().apply { set(existing, bus) }
    } else {
      _buses.value = _buses.value + bus
    }
  }

  fun addOrUpdateDriver(driver: Driver) {
    val existing = _drivers.value.indexOfFirst { it.id == driver.id }
    if (existing >= 0) {
      _drivers.value = _drivers.value.toMutableList().apply { set(existing, driver) }
    } else {
      _drivers.value = _drivers.value + driver
    }
  }

  fun addOrUpdateRoute(route: Route) {
    val existing = _routes.value.indexOfFirst { it.id == route.id }
    if (existing >= 0) {
      _routes.value = _routes.value.toMutableList().apply { set(existing, route) }
    } else {
      _routes.value = _routes.value + route
    }
  }

  fun assignStudent(studentId: String, busId: String, routeId: String, stopId: String, stopName: String) {
    _students.value = _students.value.map {
      if (it.id == studentId) {
        it.copy(busId = busId, routeId = routeId, stopId = stopId, stopName = stopName)
      } else it
    }
  }

  fun markNotificationRead(id: String) {
    _notifications.value = _notifications.value.map {
      if (it.id == id) it.copy(isRead = true) else it
    }
  }

  fun markAllNotificationsRead() {
    _notifications.value = _notifications.value.map { it.copy(isRead = true) }
  }

  fun triggerEmergencyBroadcast(text: String) {
    addNotification(
      NotificationItem(
        id = UUID.randomUUID().toString(),
        title = "🚨 District Safety Alert",
        message = text,
        timestamp = getCurrentTime(),
        type = NotificationType.EMERGENCY
      )
    )
  }

  private fun addNotification(item: NotificationItem) {
    _notifications.value = listOf(item) + _notifications.value
  }

  private fun getCurrentTime(): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
  }

  companion object {
    @Volatile
    private var INSTANCE: SafeRideRepository? = null

    fun getInstance(): SafeRideRepository {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: SafeRideRepository().also { INSTANCE = it }
      }
    }
  }
}
