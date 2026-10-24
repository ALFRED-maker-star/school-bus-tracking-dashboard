package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SafeRideRepository
import com.example.model.BoardingStatus
import com.example.model.BusStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context matches SafeRide`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SafeRide", appName)
  }

  @Test
  fun `verify SafeRide repository initializes with active fleet and boarding logic`() {
    val repository = SafeRideRepository.getInstance()
    assertTrue(repository.buses.value.isNotEmpty())
    assertTrue(repository.routes.value.isNotEmpty())
    assertTrue(repository.students.value.isNotEmpty())

    // Test student boarding toggle
    val student = repository.students.value.first()
    repository.updateStudentBoarding(student.id, BoardingStatus.BOARDED)
    val updated = repository.students.value.first { it.id == student.id }
    assertEquals(BoardingStatus.BOARDED, updated.boardingStatus)
  }
}
