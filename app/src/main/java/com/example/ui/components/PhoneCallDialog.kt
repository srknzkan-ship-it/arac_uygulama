package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

data class PhoneContact(
    val name: String,
    val number: String,
    val type: String
)

@Composable
fun PhoneCallDialog(
    isConnected: Boolean,
    onPlayDtmfTone: (Char) -> Unit,
    onClose: () -> Unit
) {
    var phoneNumberInput by remember { mutableStateOf("") }
    var isInCall by remember { mutableStateOf(false) }
    var activeCallName by remember { mutableStateOf("") }
    var activeCallNumber by remember { mutableStateOf("") }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var callSeconds by remember { mutableStateOf(0) }

    val recentContacts = listOf(
        PhoneContact("Ev & Aile", "+90 532 100 20 30", "Favori"),
        PhoneContact("Ofis & Asistan", "+90 212 345 67 89", "İş"),
        PhoneContact("Yol Yardım (7/24)", "0850 222 0 222", "Acil Servis"),
        PhoneContact("Mehmet Usta (Servis)", "+90 544 555 44 33", "Mobil")
    )

    LaunchedEffect(isInCall) {
        if (isInCall) {
            callSeconds = 0
            while (isInCall) {
                delay(1000)
                callSeconds++
            }
        }
    }

    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .testTag("phone_call_dialog")
                .fillMaxWidth(0.90f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFF0284C7), RoundedCornerShape(20.dp)),
            color = Color(0xFF0F172A)
        ) {
            if (isInCall) {
                // Active Call Screen
                ActiveCallView(
                    contactName = activeCallName,
                    phoneNumber = activeCallNumber,
                    callDurationSeconds = callSeconds,
                    isMuted = isMuted,
                    isSpeakerOn = isSpeakerOn,
                    onToggleMute = { isMuted = !isMuted },
                    onToggleSpeaker = { isSpeakerOn = !isSpeakerOn },
                    onEndCall = {
                        isInCall = false
                    }
                )
            } else {
                // Dialer & Contacts View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Hands-Free Araç Telefonu",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isConnected) "● Bluetooth Bağlı: HD Voice Aktif" else "○ Bluetooth Eşleşmesi Bekleniyor",
                                    fontSize = 11.sp,
                                    color = if (isConnected) Color(0xFF22C55E) else Color(0xFF94A3B8)
                                )
                            }
                        }

                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left: Contacts List
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Hızlı Arama & Rehber",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(recentContacts) { contact ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                activeCallName = contact.name
                                                activeCallNumber = contact.number
                                                isInCall = true
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(contact.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text(contact.number, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                            }
                                            Icon(Icons.Default.Call, contentDescription = "Ara", tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Right: Interactive Keypad with DTMF Tone Generation
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Phone Number Display
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = phoneNumberInput.ifEmpty { "Numara tuşlayın..." },
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (phoneNumberInput.isEmpty()) Color(0xFF64748B) else Color.White
                                    )
                                    if (phoneNumberInput.isNotEmpty()) {
                                        IconButton(onClick = { phoneNumberInput = phoneNumberInput.dropLast(1) }) {
                                            Icon(Icons.Default.Backspace, contentDescription = "Sil", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3x4 Keypad Grid
                            val keys = listOf(
                                listOf('1', '2', '3'),
                                listOf('4', '5', '6'),
                                listOf('7', '8', '9'),
                                listOf('*', '0', '#')
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (row in keys) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        for (k in row) {
                                            KeypadButton(
                                                char = k,
                                                onClick = {
                                                    phoneNumberInput += k
                                                    onPlayDtmfTone(k)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Call Button
                            Button(
                                onClick = {
                                    if (phoneNumberInput.isNotEmpty()) {
                                        activeCallName = "Arama"
                                        activeCallNumber = phoneNumberInput
                                        isInCall = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Aramayı Başlat", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    char: Char,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .size(46.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = char.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ActiveCallView(
    contactName: String,
    phoneNumber: String,
    callDurationSeconds: Int,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit
) {
    val minutes = callDurationSeconds / 60
    val seconds = callDurationSeconds % 60
    val formattedDuration = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0284C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(contactName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(phoneNumber, fontSize = 14.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(6.dp))
            Text(formattedDuration, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
        }

        // Active Call Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (isMuted) Color(0xFFEF4444) else Color(0xFF1E293B))
            ) {
                Icon(if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = "Mute", tint = Color.White)
            }

            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "Kapat", tint = Color.White, modifier = Modifier.size(30.dp))
            }

            IconButton(
                onClick = onToggleSpeaker,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (isSpeakerOn) Color(0xFF0284C7) else Color(0xFF1E293B))
            ) {
                Icon(if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown, contentDescription = "Speaker", tint = Color.White)
            }
        }
    }
}
