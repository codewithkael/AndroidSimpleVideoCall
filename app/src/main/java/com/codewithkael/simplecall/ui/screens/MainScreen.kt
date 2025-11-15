package com.codewithkael.simplecall.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.codewithkael.simplecall.R
import com.codewithkael.simplecall.ui.components.CallComponent
import com.codewithkael.simplecall.ui.components.IncomingCallSnackBar
import com.codewithkael.simplecall.ui.components.WhoToCall
import com.codewithkael.simplecall.ui.components.YourIdCard
import com.codewithkael.simplecall.ui.viewmodel.MainViewModel
import com.codewithkael.simplecall.utils.ConnectionState
import com.codewithkael.simplecall.utils.MyFirebaseMessagingService
import com.codewithkael.simplecall.utils.RingtonePlayer
import com.codewithkael.simplecall.utils.SimpleCallApplication

@Composable
fun MainScreen(intent: Intent?) {
    val viewModel: MainViewModel = hiltViewModel()
    val context = LocalContext.current
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (!permissions.all { it.value }) {
            Toast.makeText(
                context, "Camera And Microphone permission is required", Toast.LENGTH_SHORT
            ).show()

        } else {
            viewModel.connectSocket()
        }
    }

    val connectionStatus = viewModel.connectionState.collectAsState()

    fun handleIncomingIntent(intent: Intent?) {
        intent ?: return

        val actionType = intent.getStringExtra("actionType")
        val callerId = intent.getStringExtra("callerId") ?: return
        if (actionType == "accept") {
            RingtonePlayer.stop()
            NotificationManagerCompat.from(context)
                .cancel(MyFirebaseMessagingService.NOTIF_ID + callerId.hashCode())
            //notify for accepting the call
            Toast.makeText(context, "call accepted", Toast.LENGTH_SHORT).show()
        }
    }


    LaunchedEffect(Unit) {
        handleIncomingIntent(intent)
        val permissions = mutableListOf<String>().apply {
            add(android.Manifest.permission.RECORD_AUDIO)
            add(android.Manifest.permission.CAMERA)
            // Only add POST_NOTIFICATIONS on Android 13 (API 33) and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    LaunchedEffect(Unit) {
        viewModel.eventState.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(4.dp, 22.dp)
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(Modifier.fillMaxWidth()) {
                if (connectionStatus.value !is ConnectionState.OnCall) {
                    Row(
                        Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.webrtc),
                            contentDescription = "YouTube Channel",
                            modifier = Modifier
                                .padding(top = 30.dp)
                                .size(84.dp)
                                .clickable {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.youtube.com/@codewithkael")
                                    )
                                    context.startActivity(intent)
                                },
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(
                        text = "Simple Video Call with WebRTC",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .height(30.dp),
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Enter your friends ID and press call !!",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, start = 20.dp, end = 20.dp),
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge
                    )
                    YourIdCard(userId = SimpleCallApplication.USER_ID) {
                        Toast.makeText(context, "User ID Copied to clipboard", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            if (connectionStatus.value is ConnectionState.WaitingForCall || connectionStatus.value is ConnectionState.UserOffline) {
                WhoToCall(onCallClick = { targetId ->
                    if (targetId.isEmpty()) {
                        Toast.makeText(context, "Enter User ID to call", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.findUser(targetId)
                    }
                })
            }
        }

        if (connectionStatus.value is ConnectionState.CallingTarget ||
            connectionStatus.value is ConnectionState.OnCall){
            CallComponent(
                onSurfaceRemoteReady = {remoteRenderer ->
                    viewModel.onSurfaceRemoteReady(remoteRenderer)
                },
                onSurfaceLocalReady = {localRenderer ->
                    viewModel.onSurfaceLocalReady(localRenderer)
                },
                onSwitchCamera = {
                    viewModel.switchCamera()
                },
                onEndCall = {
                    viewModel.endCall()
                },
                onToggleMic = {enabled ->
                    viewModel.toggleMic(enabled)
                },
                onToggleCamera = {enabled->
                    viewModel.toggleCamera(enabled)
                },
                onToggleSpeaker = {isSpeaker->
                    viewModel.toggleSpeaker(isSpeaker)
                }
            )
        }

        if (connectionStatus.value is ConnectionState.ReceivedCall){
            val callerId = (connectionStatus.value as ConnectionState.ReceivedCall).sender
            IncomingCallSnackBar(
                callerId = callerId!!,
                onTimeout = {
                    viewModel.incomingCallDismissed()
                    Toast.makeText(context, "Call Timed out", Toast.LENGTH_SHORT).show()
                },
                onAccept = {target->
                    viewModel.acceptIncomingCall(target)
                },
                onReject = {target->
                    viewModel.rejectIncomingCall(target)
                }
            )
        }
    }


}