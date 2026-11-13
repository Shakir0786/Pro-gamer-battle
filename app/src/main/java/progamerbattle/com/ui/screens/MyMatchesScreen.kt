package progamerbattle.com.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import progamerbattle.com.model.MatchStatus
import progamerbattle.com.model.TournamentMatch
import progamerbattle.com.model.User
import progamerbattle.com.ui.components.GoToRoomDialog
import progamerbattle.com.ui.components.RoomCredentialCard
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCard
import progamerbattle.com.ui.theme.CarbonCardElevated
import progamerbattle.com.ui.theme.DangerRed
import progamerbattle.com.ui.theme.DeepAmber
import progamerbattle.com.ui.theme.FlameOrange
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary
import progamerbattle.com.viewmodel.MatchViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyMatchesScreen(
    matchViewModel: MatchViewModel,
    currentUser: User?,
    modifier: Modifier = Modifier
) {
    val allMatches by matchViewModel.allMatches.collectAsState()
    val forceUnlock by matchViewModel.forceUnlockCredentials.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Upcoming", "Ongoing", "Completed")

    var activeCredentialMatch by remember { mutableStateOf<TournamentMatch?>(null) }

    // Filter matches according to the tab
    val filteredMatches = remember(allMatches, selectedTabIndex, currentUser) {
        val userUid = currentUser?.uid ?: ""
        val joinedIds = currentUser?.joinedMatchIds ?: emptyList()

        when (selectedTabIndex) {
            0 -> allMatches.filter { match ->
                match.status == MatchStatus.UPCOMING &&
                        (joinedIds.contains(match.id) || match.joinedPlayerUids.contains(userUid))
            }
            1 -> allMatches.filter { match ->
                match.status == MatchStatus.ONGOING
            }
            2 -> allMatches.filter { match ->
                match.status == MatchStatus.COMPLETED
            }
            else -> emptyList()
        }
    }

    val cyberSurfaces = progamerbattle.com.ui.theme.LocalCyberSurfaces.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(cyberSurfaces.background)
            .testTag("my_matches_screen")
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = cyberSurfaces.card,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = FlameOrange,
                    height = 3.dp
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    modifier = Modifier.testTag("tab_${title.lowercase()}"),
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selectedTabIndex == index) FlameOrange else TextSecondary
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (filteredMatches.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = when (selectedTabIndex) {
                                    0 -> "No upcoming matches joined yet."
                                    1 -> "No ongoing matches currently."
                                    else -> "No match history recorded."
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Head to the Home lobby to join tournaments and claim bounties!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredMatches, key = { it.id }) { match ->
                    when (match.status) {
                        MatchStatus.COMPLETED -> CompletedMatchCard(match = match)
                        MatchStatus.ONGOING -> OngoingMatchCard(
                            match = match,
                            onViewCredentials = { activeCredentialMatch = match }
                        )
                        MatchStatus.UPCOMING -> UpcomingEnrolledCard(
                            match = match,
                            forceUnlock = forceUnlock,
                            onViewCredentials = { activeCredentialMatch = match }
                        )
                        MatchStatus.CANCELLED -> CancelledMatchCard(match = match)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    // Modal dialog for room credentials / Go To Room
    activeCredentialMatch?.let { match ->
        GoToRoomDialog(
            match = match,
            onDismiss = { activeCredentialMatch = null }
        )
    }
}

@Composable
private fun UpcomingEnrolledCard(
    match: TournamentMatch,
    forceUnlock: Boolean,
    onViewCredentials: () -> Unit
) {
    val context = LocalContext.current
    val formattedTime = match.formattedDateTime()

    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val millisUntilStart = match.startTimeMillis - currentTimeMillis
    val isInActiveRoomPhase = millisUntilStart <= 7 * 60 * 1000L || match.status == MatchStatus.ONGOING || forceUnlock

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FlameOrange.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FlameOrange.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ENROLLED",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = FlameOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = DeepAmber,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Starts: $formattedTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isInActiveRoomPhase) {
                Button(
                    onClick = onViewCredentials,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = CarbonBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GO TO ROOM",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = CarbonBackground
                    )
                }
            } else {
                val countdownText = match.formatMinusCountdown(currentTimeMillis)
                Button(
                    onClick = {
                        Toast.makeText(
                            context,
                            "Match starts in $countdownText. Room unlocks 7 minutes before match start.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CarbonBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WAIT FOR START ($countdownText)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun OngoingMatchCard(
    match: TournamentMatch,
    onViewCredentials: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFF1744).copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFF1744).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFFF1744), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "LIVE NOW",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFFFF1744)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Match is currently ongoing in custom arena room. Match results will sync upon host completion.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onViewCredentials,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "REVEAL ROOM ID & PASSWORD",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                    color = CarbonBackground
                )
            }
        }
    }
}

@Composable
private fun CompletedMatchCard(match: TournamentMatch) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "COMPLETED",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = NeonGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CarbonBackground)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = DeepAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Winner: ${match.winnerName}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsKabaddi,
                            contentDescription = null,
                            tint = FlameOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Your Kills: ${match.userKillCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Your Winnings",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = if (match.userWinnings > 0) "+₹${match.userWinnings.toInt()}" else "₹0",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = if (match.userWinnings > 0) NeonGreen else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CancelledMatchCard(match: TournamentMatch) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DangerRed.copy(alpha = 0.15f))
                        .border(1.dp, DangerRed, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "CANCELLED",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = DangerRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DangerRed.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Match Cancelled • ₹${match.entryFee.toInt()} Entry fee 100% refunded to your wallet.",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = DangerRed
                )
            }
        }
    }
}
