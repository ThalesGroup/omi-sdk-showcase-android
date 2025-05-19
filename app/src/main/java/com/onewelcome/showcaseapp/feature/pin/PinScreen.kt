package com.onewelcome.showcaseapp.feature.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun PinScreen(
  navController: NavController,
  viewModel: PinViewModel = hiltViewModel(),
) {
  var pin: String by remember { mutableStateOf("") }
  val maxPinLength = 7
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text("Enter PIN", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Row {
      repeat(maxPinLength) { index ->
        Box(
          modifier = Modifier
            .padding(8.dp)
            .size(20.dp)
            .background(
              if (index < pin.length) Color.Black else Color.Gray,
              shape = CircleShape
            )
        )
      }
    }

    Column {
      val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("Clear", "0", "Del")
      )

      buttons.forEach { row ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          row.forEach { label ->
            Button(
              onClick = {
                when (label) {
                  "Del" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                  "Clear" -> pin = ""
                  else -> if (pin.length < maxPinLength) pin += label
                }

                if (pin.length == maxPinLength) {
                  //viewModel.onPinEntered(pin)
                }
              },
              modifier = Modifier
                .padding(8.dp)
                .size(80.dp)
            ) {
              Text(label)
            }
          }
        }
      }
    }
  }
}
