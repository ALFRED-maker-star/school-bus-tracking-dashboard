package com.example.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SafeRideRepository
import com.example.ui.theme.*

@Composable
fun DriverLoginScreen(
  repository: SafeRideRepository,
  modifier: Modifier = Modifier
) {
  val isDriverLoggedIn by repository.isDriverLoggedIn.collectAsState()
  var driverBadgeId by remember { mutableStateOf("DRV-101") }
  var pinCode by remember { mutableStateOf("4829") }
  var preInspectionChecked by remember { mutableStateOf(true) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundSlate)
      .padding(20.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("driver_login_card"),
      colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
      shape = RoundedCornerShape(24.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // SafeRide Driver Badge
        Box(
          modifier = Modifier
            .size(72.dp)
            .background(SchoolBusYellowLight, CircleShape)
            .border(2.dp, SchoolBusYellow, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.DirectionsBus,
            contentDescription = null,
            tint = SchoolBusYellowDark,
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = if (isDriverLoggedIn) "Driver Active Duty" else "Driver Portal Login",
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          color = NavyDark
        )
        Text(
          text = "School District Transportation Terminal",
          fontSize = 12.sp,
          color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isDriverLoggedIn) {
          // Logged in profile view
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
            shape = RoundedCornerShape(14.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .background(NavyPrimary, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(text = "MR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
              }
              Column {
                Text(text = "Marcus Reynolds", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text(text = "Badge ID: DRV-101 • Bus #14", fontSize = 12.sp, color = TextSecondary)
                Text(text = "CDL Class B Passenger Certified", fontSize = 11.sp, color = StatusGreen, fontWeight = FontWeight.SemiBold)
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Inspection status badge
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(StatusGreenLight, RoundedCornerShape(12.dp))
              .padding(12.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen)
              Column {
                Text(text = "Pre-Trip Inspection Completed", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                Text(text = "Brakes, Tires, Emergency Exit & First-Aid Verified", fontSize = 11.sp, color = TextSecondary)
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = { repository.setDriverTab(com.example.model.DriverTab.TRIP_CONTROL) },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("driver_go_to_trip_button"),
            colors = ButtonDefaults.buttonColors(containerColor = SchoolBusYellowDark),
            shape = RoundedCornerShape(14.dp)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Trip Run Dashboard", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedButton(
            onClick = { repository.logoutDriver() },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("driver_logout_button"),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text("Switch / Log Out", fontSize = 13.sp, color = NavyPrimary)
          }
        } else {
          // Login Form
          OutlinedTextField(
            value = driverBadgeId,
            onValueChange = { driverBadgeId = it },
            label = { Text("Driver Badge ID") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("driver_badge_input"),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = pinCode,
            onValueChange = { pinCode = it },
            label = { Text("4-Digit Security PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("driver_pin_input"),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Pre-trip inspection checklist agreement
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(SurfaceContainer, RoundedCornerShape(12.dp))
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = preInspectionChecked,
              onCheckedChange = { preInspectionChecked = it },
              modifier = Modifier.testTag("inspection_checkbox")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "I certify pre-trip safety inspection (brakes, lights, mirrors, safety gear) is completed.",
              fontSize = 11.sp,
              color = TextPrimary,
              lineHeight = 15.sp
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = { repository.loginDriver(driverBadgeId) },
            enabled = driverBadgeId.isNotBlank() && pinCode.length >= 4 && preInspectionChecked,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("driver_login_submit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text("Sign In & Start Shift", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
