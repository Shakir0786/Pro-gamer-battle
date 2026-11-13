package progamerbattle.com.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import progamerbattle.com.BuildConfig
import progamerbattle.com.data.RemoteConfigModel
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCard
import progamerbattle.com.ui.theme.CarbonCardElevated
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary

@Composable
fun ForceUpdateDialog(
    config: RemoteConfigModel
) {
    val context = LocalContext.current

    fun openUpdateUrl() {
        val url = config.updateUrl.ifBlank { "https://progamerbattle.com/download" }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Opening: $url", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = { /* Non-dismissible: Update is mandatory */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = NeonCyan)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F1E36),
                            CarbonCardElevated,
                            CarbonBackground
                        )
                    )
                )
                .border(1.5.dp, NeonCyan, RoundedCornerShape(24.dp))
                .padding(24.dp)
                .testTag("force_update_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Pulsing Cyber Update Icon
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.5.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "Update Icon",
                        tint = NeonCyan,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "APP UPDATE REQUIRED",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    ),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Version comparison chips
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CarbonCard)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Installed: v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("➔", color = CyberGold, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Required: v${config.minVersionStr}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonCyanLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "A new critical update is available with anti-cheat improvements and new custom room features. Please update the app now to enter tournaments.",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { openUpdateUrl() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("force_update_download_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Update,
                        contentDescription = null,
                        tint = CarbonBackground,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UPDATE NOW",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = CarbonBackground
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Fast direct download • No data loss",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextMuted
                )
            }
        }
    }
}
