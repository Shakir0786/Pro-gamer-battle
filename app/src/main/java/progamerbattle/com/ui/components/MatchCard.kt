package progamerbattle.com.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import progamerbattle.com.model.MatchStatus
import progamerbattle.com.model.TournamentMatch
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCard
import progamerbattle.com.ui.theme.CarbonCardElevated
import progamerbattle.com.ui.theme.CarbonSurface
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.CyberGoldLight
import progamerbattle.com.ui.theme.DangerRed
import progamerbattle.com.ui.theme.ElectricPurple
import progamerbattle.com.ui.theme.LocalCyberSurfaces
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.NeonGreenLight
import progamerbattle.com.ui.theme.NeonMagenta
import progamerbattle.com.ui.theme.SpecularHighlightBright
import progamerbattle.com.ui.theme.TextSecondary

@Composable
fun TournamentMatchCard(
    match: TournamentMatch,
    isUserEnrolled: Boolean,
    forceUnlock: Boolean = false,
    onJoinClick: (TournamentMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cyberSurfaces = LocalCyberSurfaces.current
    val isDark = cyberSurfaces.isDark
    val formattedTime = match.formattedDateTime()

    var isBreakdownDialogOpen by remember { mutableStateOf(false) }
    var isGoToRoomDialogOpen by remember { mutableStateOf(false) }

    // Live countdown timer state (recalculated every second)
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val isMatchCompleted = match.status == MatchStatus.COMPLETED
    val millisUntilStart = match.startTimeMillis - currentTimeMillis
    val isRegistrationLocked = match.isRegistrationClosed || currentTimeMillis >= match.startTimeMillis

    // 7-minute threshold (7 * 60 * 1000 = 420,000 ms)
    val isInActiveRoomPhase = millisUntilStart <= 7 * 60 * 1000L || match.status == MatchStatus.ONGOING || forceUnlock

    val borderBrush = if (isMatchCompleted) {
        Brush.horizontalGradient(listOf(NeonGreen, CyberGold))
    } else if (isUserEnrolled) {
        Brush.horizontalGradient(listOf(NeonCyan, NeonMagenta))
    } else {
        Brush.verticalGradient(
            if (isDark) {
                listOf(
                    SpecularHighlightBright.copy(alpha = 0.4f),
                    cyberSurfaces.border,
                    cyberSurfaces.border.copy(alpha = 0.2f)
                )
            } else {
                listOf(Color.White, cyberSurfaces.border)
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("match_card_${match.id}")
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (isMatchCompleted) NeonGreen.copy(alpha = 0.3f) else if (isUserEnrolled) NeonCyan.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    if (isDark) listOf(CarbonCardElevated, CarbonCard, CarbonSurface)
                    else listOf(Color.White, cyberSurfaces.card, cyberSurfaces.cardElevated)
                )
            )
            .border(
                width = if (isUserEnrolled || isMatchCompleted) 1.5.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(20.dp)
            )
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Time + Status / Registration Lock Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.3.sp
                        ),
                        color = cyberSurfaces.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Match Time",
                            tint = if (isDark) NeonCyan else Color(0xFF007A8A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelMedium,
                            color = cyberSurfaces.textSecondary
                        )
                    }
                }

                // Status or Enrolled / Lock Pill
                if (isMatchCompleted) {
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = NeonGreen)
                            .clip(RoundedCornerShape(20.dp))
                            .background(NeonGreen.copy(alpha = 0.2f))
                            .border(1.2.dp, NeonGreen, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = NeonGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "COMPLETED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = if (isDark) NeonGreenLight else Color(0xFF047857)
                            )
                        }
                    }
                } else if (isUserEnrolled) {
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = NeonCyan)
                            .clip(RoundedCornerShape(20.dp))
                            .background(NeonCyan.copy(alpha = 0.18f))
                            .border(1.2.dp, NeonCyan, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Enrolled",
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "ENROLLED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = if (isDark) NeonCyanLight else Color(0xFF007A8A)
                            )
                        }
                    }
                } else if (isRegistrationLocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(DangerRed.copy(alpha = 0.18f))
                            .border(1.dp, DangerRed, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "REGISTRATION CLOSED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black
