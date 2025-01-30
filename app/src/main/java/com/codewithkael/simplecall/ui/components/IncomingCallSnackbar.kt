package com.codewithkael.simplecall.ui.components

import android.media.RingtoneManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codewithkael.simplecall.utils.Constants.TIME_OUT_DURATION_MS
import kotlinx.coroutines.delay

@Composable
fun IncomingCallSnackBar(
    callerId: String,
    onTimeout: () -> Unit,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSnackBar by remember { mutableStateOf(true) }
    val ringtone = remember {
        RingtoneManager.getRingtone(
            context,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        )
    }

    if (showSnackBar) {
        // Play ringtone when the snackbar is shown
        LaunchedEffect(Unit) {
            ringtone.play()
            delay(TIME_OUT_DURATION_MS) // Wait for 5 seconds
            if (showSnackBar) {
                showSnackBar = false
                ringtone.stop()
                onTimeout() // Trigger the timeout callback
            }
        }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Incoming Call",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "From: $callerId",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            ringtone.stop()
                            showSnackBar = false
                            onReject(callerId) // Trigger the reject callback
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text("No")
                    }

                    Button(
                        onClick = {
                            ringtone.stop()
                            showSnackBar = false
                            onAccept(callerId) // Trigger the accept callback
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Text("Yes")
                    }
                }
            }
        }
    }
}
