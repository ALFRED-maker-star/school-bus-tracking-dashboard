package com.example.ui.admin

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
import com.example.ui.components.BoardingBadge
import com.example.ui.theme.*

@Composable
fun StudentAssignmentScreen(
  repository: SafeRideRepository,
  modifier: Modifier = Modifier
) {
  val students by repository.students.collectAsState()
  val buses by repository.buses.collectAsState()
  val routes by repository.routes.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var selectedGradeFilter by remember { mutableStateOf("All") }
  var studentToAssign by remember { mutableStateOf<Student?>(null) }

  val filteredStudents = remember(students, searchQuery, selectedGradeFilter) {
    students.filter { s ->
      (searchQuery.isBlank() || s.name.contains(searchQuery, ignoreCase = true) || s.stopName.contains(searchQuery, ignoreCase = true)) &&
      (selectedGradeFilter == "All" || s.grade.contains(selectedGradeFilter, ignoreCase = true))
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundSlate)
      .testTag("student_assignment_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Column {
        Text(
          text = "Student Transit Assignments",
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          color = NavyDark
        )
        Text(
          text = "Allocate students to buses, routes, and designated neighborhood stops",
          fontSize = 12.sp,
          color = TextSecondary
        )
      }
    }

    // Search and Grade Filter
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search by student name or stop...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Close, contentDescription = "Clear")
              }
            }
          },
          modifier = Modifier.fillMaxWidth().testTag("student_search_input"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = SurfaceWhite,
            focusedContainerColor = SurfaceWhite
          )
        )

        // Grade Filter Chips
        val grades = listOf("All", "Grade 1", "Grade 2", "Grade 3", "Grade 4", "Grade 5")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(grades) { g ->
            FilterChip(
              selected = (selectedGradeFilter == g),
              onClick = { selectedGradeFilter = g },
              label = { Text(g, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NavyPrimary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("grade_filter_$g")
            )
          }
        }
      }
    }

    // Student Assignment Cards
    items(filteredStudents) { student ->
      val assignedBus = buses.firstOrNull { it.id == student.busId }
      val assignedRoute = routes.firstOrNull { it.id == student.routeId }

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("student_assignment_card_${student.id}"),
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .background(SchoolBusYellowLight, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(text = student.avatarEmoji, fontSize = 22.sp)
              }
              Column {
                Text(
                  text = student.name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = TextPrimary
                )
                Text(
                  text = "${student.grade} • ${student.school}",
                  fontSize = 11.sp,
                  color = TextSecondary
                )
              }
            }

            BoardingBadge(status = student.boardingStatus)
          }

          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider(color = BorderSubtle)
          Spacer(modifier = Modifier.height(10.dp))

          // Current Transit Assignment
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "ASSIGNED BUS & ROUTE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
              )
              Text(
                text = "${assignedBus?.name ?: "No Bus"} • ${assignedRoute?.name ?: "No Route"}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyDark
              )
              Text(
                text = "Designated Stop: ${student.stopName}",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }

            Button(
              onClick = { studentToAssign = student },
              modifier = Modifier.testTag("reassign_student_button_${student.id}"),
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Reassign", fontSize = 11.sp)
            }
          }
        }
      }
    }
  }

  // Student Reassignment Modal
  studentToAssign?.let { student ->
    var selectedBusId by remember { mutableStateOf(student.busId) }
    var selectedRouteId by remember { mutableStateOf(student.routeId) }
    val currentRoute = routes.firstOrNull { it.id == selectedRouteId } ?: routes.first()
    var selectedStopId by remember { mutableStateOf(student.stopId) }
    var selectedStopName by remember { mutableStateOf(student.stopName) }

    AlertDialog(
      onDismissRequest = { studentToAssign = null },
      title = { Text(text = "Assign Transit for ${student.name}", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(text = "Select Bus Vehicle:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          buses.forEach { b ->
            Surface(
              onClick = {
                selectedBusId = b.id
                selectedRouteId = b.routeId
              },
              color = if (selectedBusId == b.id) SchoolBusYellowLight else SurfaceContainer,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = "${b.name} (${b.capacity} seats)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                if (selectedBusId == b.id) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = SchoolBusYellowDark, modifier = Modifier.size(16.dp))
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "Select Designated Bus Stop:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          currentRoute.stops.take(4).forEach { st ->
            Surface(
              onClick = {
                selectedStopId = st.id
                selectedStopName = st.name
              },
              color = if (selectedStopId == st.id) SchoolBusYellowLight else SurfaceContainer,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = "Stop ${st.order}: ${st.name}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                if (selectedStopId == st.id) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = SchoolBusYellowDark, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            repository.assignStudent(student.id, selectedBusId, selectedRouteId, selectedStopId, selectedStopName)
            studentToAssign = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("Save Transit Plan")
        }
      },
      dismissButton = {
        TextButton(onClick = { studentToAssign = null }) {
          Text("Cancel")
        }
      }
    )
  }
}
