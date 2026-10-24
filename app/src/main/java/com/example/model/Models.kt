package com.example.model

enum class UserRole(val label: String) {
  PARENT("Parent"),
  DRIVER("Driver"),
  ADMIN("Admin Portal")
}

enum class ParentTab(val label: String) {
  LIVE_TRACKING("Live Map"),
  CHILD_PROFILE("Child Profile"),
  ROUTE_INFO("Route & Stops"),
  NOTIFICATIONS("Alerts")
}

enum class DriverTab(val label: String) {
  MY_ROUTE("My Route"),
  TRIP_CONTROL("Trip Run"),
  STUDENT_CHECKLIST("Students"),
  LOGIN("Profile")
}

enum class AdminTab(val label: String) {
  DASHBOARD("Dashboard"),
  LIVE_FLEET_MAP("Live Fleet"),
  BUSES_DRIVERS("Buses & Drivers"),
  ROUTES_SETUP("Routes Setup"),
  STUDENT_ASSIGNMENT("Students"),
  LOGIN("Admin Access")
}

enum class BusStatus(val label: String) {
  EN_ROUTE("En Route"),
  AT_STOP("At Stop"),
  IDLE("Depot Idle"),
  DELAYED("Delayed"),
  COMPLETED("Route Complete")
}

enum class BoardingStatus(val label: String) {
  WAITING("Waiting"),
  BOARDED("Boarded"),
  ABSENT("Absent"),
  MISSING("Flagged Missing")
}

enum class NotificationType {
  DEPARTED,
  APPROACHING,
  BOARDED,
  DELAY,
  ARRIVED,
  EMERGENCY
}

data class BusStop(
  val id: String,
  val name: String,
  val address: String,
  val scheduledMorningTime: String,
  val scheduledAfternoonTime: String,
  val xNorm: Float, // 0.0f .. 1.0f for vector map coordinate
  val yNorm: Float, // 0.0f .. 1.0f for vector map coordinate
  val order: Int,
  val isChildStop: Boolean = false,
  val studentIds: List<String> = emptyList()
)

data class Route(
  val id: String,
  val name: String,
  val schoolName: String,
  val colorHex: String,
  val totalMiles: Double,
  val estimatedMinutes: Int,
  val stops: List<BusStop>,
  val isActive: Boolean = true
)

data class Bus(
  val id: String,
  val name: String,
  val plateNumber: String,
  val driverId: String,
  val routeId: String,
  val capacity: Int,
  val studentsOnBoard: Int,
  val status: BusStatus,
  val speedMph: Int,
  val currentProgress: Float, // 0.0f .. 1.0f along the route
  val currentStopIndex: Int,
  val etaMinutesToChildStop: Int,
  val batteryFuelLevel: Int,
  val lastPing: String
)

data class Driver(
  val id: String,
  val name: String,
  val phone: String,
  val email: String,
  val rating: Float,
  val experienceYears: Int,
  val licenseNumber: String,
  val assignedBusId: String,
  val isInspectedToday: Boolean,
  val avatarInitials: String
)

data class Student(
  val id: String,
  val name: String,
  val grade: String,
  val avatarEmoji: String,
  val busId: String,
  val routeId: String,
  val stopId: String,
  val stopName: String,
  val school: String,
  val emergencyContact: String,
  val specialNotes: String,
  val boardingStatus: BoardingStatus,
  val boardedAtTime: String? = null
)

data class NotificationItem(
  val id: String,
  val title: String,
  val message: String,
  val timestamp: String,
  val type: NotificationType,
  val isRead: Boolean = false
)

data class AdminStatSummary(
  val activeBusesCount: Int,
  val totalBusesCount: Int,
  val totalStudentsInTransit: Int,
  val totalEnrolledStudents: Int,
  val onTimeRate: Double,
  val activeAlertsCount: Int
)
