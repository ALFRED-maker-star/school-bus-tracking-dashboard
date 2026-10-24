package com.example.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.model.BoardingStatus
import com.example.model.Student
import com.example.ui.theme.*

@Composable
fun StudentChecklistScreen(
  repository: SafeRideRepository,
  modifier: Modifier = Modifier
) {
  val students by repository.students.collectAsState()
  val routes by repository.routes.collectAsState()
  val buses by repository.buses.collectAsState()

  val bus = buses.firstOrNull { it.id == "BUS-14" } ?: buses.first()
  val route = routes.firstOrNull { it.id == bus.routeId } ?: routes.first()

  // Filter by stop
  var selectedStopFilter by remember { mutableStateOf<String?>("ST-4") } // Default to Stop 4 (Elm St)

  val filteredStudents = remember(students, selectedStopFilter) {
    if (selectedStopFilter == null) {
      students.filter { it.busId == bus.id }
    } else {
      students.filter { it.busId == bus.id && it.stopId == selectedStopFilter }
    }
  }

  val totalOnBus = students.count { it.busId == bus.id && it.boardingStatus == BoardingStatus.BOARDED }
  val totalRoster = students.count { it.busId == bus.id }

  var studentToFlagMissing by remember { mutableStateOf<Student?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundSlate)
      .testTag("student_checklist_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Boarding Progress Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("boarding_counter_card"),
        colors = CardDefaults.cardColors(containerColor = NavyPrimary),
        shape = RoundedCornerShape(18.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "BOARDING MANIFEST",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                letterSpacing = 0.5.sp
              )
              Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "$totalOnBus",
                  fontSize = 28.sp,
                  fontWeight = FontWeight.Black,
                  color = SchoolBusYellow
                )
                Text(
                  text = "/ $totalRoster On Board",
                  fontSize = 14.sp,
                  color = Color.White,
                  modifier = Modifier.padding(bottom = 4.dp)
                )
              }
            }

            Box(
              modifier = Modifier
                .background(SchoolBusYellowLight, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "Route 3B Active",
                color = NavyDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          LinearProgressIndicator(
            progress = { if (totalRoster > 0) totalOnBus.toFloat() / totalRoster else 0f },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp),
            color = SchoolBusYellow,
            trackColor = Color(0xFF334155),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
          )
        }
      }
    }

    // Stop Selector Tabs
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "FILTER BY SCHEDULED STOP",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 0.5.sp
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          item {
            FilterChip(
              selected = (selectedStopFilter == null),
              onClick = { selectedStopFilter = null },
              label = { Text("All Stops", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyPrimary, selectedLabelColor = Color.White),
              modifier = Modifier.testTag("filter_all_stops")
            )
          }

          items(route.stops) { stop ->
            FilterChip(
              selected = (selectedStopFilter == stop.id),
              onClick = { selectedStopFilter = stop.id },
              label = { Text("Stop ${stop.order}: ${stop.name.split(" ")[0]}", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyPrimary, selectedLabelColor = Color.White),
              modifier = Modifier.testTag("filter_stop_${stop.id}")
            )
          }
        }
      }
    }

    // Student Items
    items(filteredStudents) { student ->
      StudentBoardingCard(
        student = student,
        onBoardToggle = {
          val next = if (student.boardingStatus == BoardingStatus.BOARDED) BoardingStatus.WAITING else BoardingStatus.BOARDED
          repository.updateStudentBoarding(student.id, next)
        },
        onFlagMissing = { studentToFlagMissing = student },
        onMarkAbsent = { repository.updateStudentBoarding(student.id, BoardingStatus.ABSENT) }
      )
    }
  }

  // Flag Missing Student Confirmation
  studentToFlagMissing?.let { student ->
    AlertDialog(
      onDismissRequest = { studentToFlagMissing = null },
      icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed, modifier = Modifier.size(32.dp)) },
      title = { Text(text = "Flag ${student.name} as Missing?", fontWeight = FontWeight.Black) },
      text = {
        Text(
          text = "Are you sure ${student.name} was not present at ${student.stopName}? This will immediately notify parents and send an alert to School Dispatch."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            repository.updateStudentBoarding(student.id, BoardingStatus.MISSING)
            studentToFlagMissing = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
        ) {
          Text("Confirm Missing Flag")
        }
      },
      dismissButton = {
        TextButton(onClick = { studentToFlagMissing = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun StudentBoardingCard(
  student: Student,
  onBoardToggle: () -> Unit,
  onFlagMissing: () -> Unit,
  onMarkAbsent: () -> Unit
) {
  val isBoarded = student.boardingStatus == BoardingStatus.BOARDED
  val isMissing = student.boardingStatus == BoardingStatus.MISSING

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("student_card_${student.id}"),
    colors = CardDefaults.cardColors(
      containerColor = when {
        isMissing -> StatusRedLight
        isBoarded -> StatusGreenLight.copy(alpha = 0.5f)
        else -> SurfaceWhite
      }
    ),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(
      if (isMissing) 1.5.dp else 1.dp,
      when {
        isMissing -> StatusRed
        isBoarded -> StatusGreen
        else -> BorderSubtle
      }
    )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .background(SchoolBusYellowLight, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(text = student.avatarEmoji, fontSize = 22.sp)
          }

          Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = student.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
              )
              Text(
                text = student.grade.split("•")[0].trim(),
                fontSize = 10.sp,
                color = TextSecondary
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Stop: ${student.stopName}",
              fontSize = 11.sp,
              color = TextSecondary
            )
          }
        }

        // Checkbox Boarded Button
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(
            checked = isBoarded,
            onCheckedChange = { onBoardToggle() },
            colors = CheckboxDefaults.colors(
              checkedColor = StatusGreen,
              uncheckedColor = NavyMuted
            ),
            modifier = Modifier.testTag("boarded_checkbox_${student.id}")
          )
          Text(
            text = if (isBoarded) "Boarded" else "Check In",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isBoarded) StatusGreen else NavyPrimary
          )
        }
      }

      // Special alert note if any
      if (student.specialNotes.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFEF9C3), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, tint = SchoolBusYellowDark, modifier = Modifier.size(14.dp))
            Text(text = student.specialNotes, fontSize = 11.sp, color = NavyDark, fontWeight = FontWeight.Medium)
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      HorizontalDivider(color = BorderSubtle.copy(alpha = 0.6f))
      Spacer(modifier = Modifier.height(6.dp))

      // Bottom quick actions: Flag Missing, Mark Absent
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (student.boardingStatus != BoardingStatus.ABSENT) {
          TextButton(
            onClick = onMarkAbsent,
            modifier = Modifier.testTag("mark_absent_${student.id}")
          ) {
            Text("Excused Absent", fontSize = 11.sp, color = TextSecondary)
          }
        }

        if (student.boardingStatus != BoardingStatus.MISSING && student.boardingStatus != BoardingStatus.BOARDED) {
          TextButton(
            onClick = onFlagMissing,
            modifier = Modifier.testTag("flag_missing_${student.id}")
          ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Flag Missing", fontSize = 11.sp, color = StatusRed, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
