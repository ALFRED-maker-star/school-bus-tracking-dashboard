package com.example.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SafeRideRepository
import com.example.model.BoardingStatus
import com.example.ui.theme.*

@Composable
fun ChildProfileScreen(
  repository: SafeRideRepository,
  modifier: Modifier = Modifier
) {
  val students by repository.students.collectAsState()
  val buses by repository.buses.collectAsState()
  val routes by repository.routes.collectAsState()
  val selectedChildId by repository.selectedChildId.collectAsState()

  val child = students.firstOrNull { it.id == selectedChildId } ?: students.first()
  val bus = buses.firstOrNull { it.id == child.busId } ?: buses.first()
  val route = routes.firstOrNull { it.id == child.routeId } ?: routes.first()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundSlate)
      .testTag("child_profile_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Child Profile Hero Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("child_hero_card"),
        colors = CardDefaults.cardColors(containerColor = NavyPrimary),
        shape = RoundedCornerShape(20.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .background(SchoolBusYellowLight, CircleShape)
              .border(3.dp, SchoolBusYellow, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(text = child.avatarEmoji, fontSize = 42.sp)
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = child.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
          )
          Text(
            text = "${child.grade} • ${child.school}",
            fontSize = 13.sp,
            color = Color(0xFF94A3B8)
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Boarding status tag
          Box(
            modifier = Modifier
              .background(
                when (child.boardingStatus) {
                  BoardingStatus.BOARDED -> StatusGreen
                  BoardingStatus.WAITING -> SchoolBusYellow
                  BoardingStatus.MISSING -> StatusRed
                  BoardingStatus.ABSENT -> TextSecondary
                },
                RoundedCornerShape(20.dp)
              )
              .padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Text(
              text = "TODAY: ${child.boardingStatus.label.uppercase()}",
              color = if (child.boardingStatus == BoardingStatus.WAITING) NavyDark else Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Digital SafeRide NFC / QR Transit Pass Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("digital_transit_pass_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Default.QrCode2, contentDescription = null, tint = NavyPrimary)
              Text(
                text = "STUDENT DIGITAL BUS PASS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
              )
            }
            Text(
              text = "ID: #SR-98214",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = SchoolBusYellowDark
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Stylized QR code representation
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(110.dp)
              .background(SurfaceContainer, RoundedCornerShape(12.dp))
              .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Bus Scanner QR",
                tint = NavyPrimary,
                modifier = Modifier.size(54.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Tap or present to bus door scanner on boarding",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }
          }
        }
      }
    }

    // Transit Assignment Details
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("transit_assignment_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "TRANSIT & ROUTE ASSIGNMENT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
          )

          ProfileInfoRow(
            label = "Assigned Bus",
            value = "${bus.name} (${bus.plateNumber})",
            icon = Icons.Default.DirectionsBus
          )
          ProfileInfoRow(
            label = "Route Name",
            value = route.name,
            icon = Icons.Default.AltRoute
          )
          ProfileInfoRow(
            label = "Designated Stop",
            value = child.stopName,
            icon = Icons.Default.LocationOn
          )
          ProfileInfoRow(
            label = "Morning Pickup",
            value = "07:38 AM (Stop #4)",
            icon = Icons.Default.Schedule
          )
          ProfileInfoRow(
            label = "Afternoon Drop-off",
            value = "03:42 PM (Stop #4)",
            icon = Icons.Default.Schedule
          )
        }
      }
    }

    // Safety & Medical Notes Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("safety_medical_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "SAFETY & HEALTH PROTOCOLS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
          )
          Spacer(modifier = Modifier.height(10.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(SchoolBusYellowLight, RoundedCornerShape(12.dp))
              .padding(12.dp)
          ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Icon(Icons.Default.MedicalServices, contentDescription = null, tint = SchoolBusYellowDark)
              Column {
                Text(
                  text = "Medical Notes for Driver",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = NavyDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = child.specialNotes,
                  fontSize = 12.sp,
                  color = TextPrimary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Emergency Contacts",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = child.emergencyContact,
            fontSize = 12.sp,
            color = TextSecondary
          )
        }
      }
    }
  }
}

@Composable
private fun ProfileInfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Icon(icon, contentDescription = null, tint = NavyMuted, modifier = Modifier.size(16.dp))
      Text(text = label, fontSize = 12.sp, color = TextSecondary)
    }
    Text(
      text = value,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = TextPrimary
    )
  }
}
