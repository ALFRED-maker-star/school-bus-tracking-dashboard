package com.example.ui.admin

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
fun AdminLoginScreen(
  repository: SafeRideRepository,
  modifier: Modifier = Modifier
) {
  val isAdminLoggedIn by repository.isAdminLoggedIn.collectAsState()
  var email by remember { mutableStateOf("dispatcher@saferide.school") }
  var password by remember { mutableStateOf("admin2026") }

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
        .widthIn(max = 480.dp)
        .testTag("admin_login_card"),
      colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
      shape = RoundedCornerShape(24.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .background(NavyPrimary, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AdminPanelSettings,
            contentDescription = null,
            tint = SchoolBusYellow,
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = if (isAdminLoggedIn) "District Dispatch Console" else "Admin Portal Authentication",
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          color = NavyDark
        )
        Text(
          text = "Oakridge Central Transport Authority",
          fontSize = 12.sp,
          color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isAdminLoggedIn) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
            shape = RoundedCornerShape(14.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(text = "Logged in as Dispatch Superuser", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
              Text(text = "Role: Transportation Director • All Fleet Controls Enabled", fontSize = 12.sp, color = TextSecondary)
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = { repository.setAdminTab(com.example.model.AdminTab.DASHBOARD) },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("admin_go_to_dashboard_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(14.dp)
          ) {
            Icon(Icons.Default.Dashboard, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Go to Admin Dashboard", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedButton(
            onClick = { repository.logoutAdmin() },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("admin_logout_button"),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text("Switch / Log Out", fontSize = 13.sp, color = NavyPrimary)
          }
        } else {
          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Administrator Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("admin_email_input"),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("admin_password_input"),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = { repository.loginAdmin(email) },
            enabled = email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("admin_login_submit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text("Sign In to Admin Console", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
