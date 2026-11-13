package progamerbattle.com.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCardElevated
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.CyberGoldLight
import progamerbattle.com.ui.theme.LocalCyberSurfaces
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary

@Composable
fun AnnouncementBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    if (message.isBlank()) return

    var isDismissed by rememberSaveable(message) { mutableStateOf(false) }
    val cyberSurfaces = LocalCyberSurfaces.current
    val isDark = cyberSurfaces.isDark

    AnimatedVisibility(
        visible = !isDismissed,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(14.dp),
                    ambientColor = CyberGold.copy(alpha = 0.3f),
                    spotColor = Color.Black
                )
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        if (isDark) listOf(
                            Color(0xFF241A05),
                            CarbonCardElevated,
                            Color(0xFF161E2E)
                        ) else listOf(
                            Color(0xFFFFFBEB),
                            Color(0xFFFEF3C7),
                            Color(0xFFF1F5F9)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(CyberGold.copy(alpha = 0.7f), NeonCyan.copy(alpha = 0.4f))
                    ),
                    RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("remote_config_announcement_banner")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Megaphone Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CyberGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Announcement",
                        tint = if (isDark) CyberGoldLight else Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "OFFICIAL ANNOUNCEMENT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = if (isDark) CyberGoldLight else Color(0xFFB45309)
                        )
                    }
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        color = cyberSurfaces.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Dismiss Button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable { isDismissed = true }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Announcement",
                        tint = cyberSurfaces.textSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
