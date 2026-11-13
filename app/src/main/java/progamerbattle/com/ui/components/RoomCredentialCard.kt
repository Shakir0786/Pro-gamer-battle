package progamerbattle.com.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.model.TournamentMatch
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCardElevated
import progamerbattle.com.ui.theme.DeepAmber
import progamerbattle.com.ui.theme.FlameOrange
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary

@Composable
fun RoomCredentialCard(
    match: TournamentMatch,
    forceUnlock: Boolean = false,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUnlocked = match.isRoomCredentialsUnlocked || forceUnlock
    val millisUntilStart = match.startTimeMillis - System.currentTimeMillis()
    val minutesUntilStart = (millisUntilStart / (60 * 1000L)).coerceAtLeast(0)

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("room_credentials_card")
            .border(
                1.dp,
                if (isUnlocked) Brush.horizontalGradient(listOf(FlameOrange, DeepAmber)) else Brush.linearGradient(listOf(CarbonBorder, CarbonBorder)),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonCardElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = "Room Status",
                        tint = if (isUnlocked) NeonGreen else DeepAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "CUSTOM ROOM CREDENTIALS",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary
                        )
                        Text(
                            text = match.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isUnlocked) {
                // Locked State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CarbonBackground)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = DeepAmber,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Room Credentials Locked",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = DeepAmber
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Room ID & Password will unlock 15 minutes before match starts (~${minutesUntilStart}m left).",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                // Unlocked State: Room ID & Password
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CarbonBackground)
                        .padding(16.dp)
                ) {
                    // Room ID Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ROOM ID",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DeepAmber
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = match.roomId,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                ),
                                color = TextPrimary
                            )
                        }
                        IconButton(
                            onClick = { copyToClipboard("Room ID", match.roomId) },
                            modifier = Modifier
                                .testTag("copy_room_id_button")
                                .clip(RoundedCornerShape(8.dp))
                                .background(CarbonCardElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Room ID",
                                tint = FlameOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(CarbonBorder)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Room Password Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ROOM PASSWORD",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DeepAmber
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = match.roomPassword,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                ),
                                color = NeonGreen
                            )
                        }
                        IconButton(
                            onClick = { copyToClipboard("Room Password", match.roomPassword) },
                            modifier = Modifier
                                .testTag("copy_room_password_button")
                                .clip(RoundedCornerShape(8.dp))
                                .background(CarbonCardElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Password",
                                tint = NeonGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fast Action: Copy Both
                Button(
                    onClick = {
                        val combined = "Room ID: ${match.roomId} | Password: ${match.roomPassword}"
                        copyToClipboard("Room Credentials", combined)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("copy_both_credentials_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = CarbonBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COPY BOTH ID & PASSWORD",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = CarbonBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step-by-step instructions card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF161F33))
                    .padding(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "How to join: Open Game > Mode Selection > Custom Room > Enter Room ID > Enter Password > Select assigned slot.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "DISMISS",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
