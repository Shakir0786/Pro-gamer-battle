package progamerbattle.com.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import progamerbattle.com.model.MatchType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.ui.components.Neon3DButton
import progamerbattle.com.ui.components.Neon3DStyle
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCardElevated
import progamerbattle.com.ui.theme.CarbonSurface
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.CyberGoldLight
import progamerbattle.com.ui.theme.DangerRed
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AdminCreateMatchDialog(
    adminViewModel: AdminViewModel
) {
    val state by adminViewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (!state.isCreateMatchDialogOpen) return

    val isEditing = state.editingMatchId != null

    val officialTiers = listOf(
        Pair(10.0, 5.0),
        Pair(20.0, 15.0),
        Pair(30.0, 20.0),
        Pair(50.0, 40.0),
        Pair(100.0, 80.0)
    )

    // Calendar initialized to the current scheduled time
    val scheduleCal = Calendar.getInstance().apply {
        timeInMillis = state.newMatchStartTimeMillis
    }

    val displayDate = try {
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date(state.newMatchStartTimeMillis))
    } catch (_: Exception) {
        state.newMatchDateFormatted.ifBlank { "Today" }
    }

    val displayTime = try {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(state.newMatchStartTimeMillis))
    } catch (_: Exception) {
        state.newMatchTimeFormatted.ifBlank { "09:00 PM" }
    }

    val millisDiff = state.newMatchStartTimeMillis - System.currentTimeMillis()
    val minutesUntilStart = (millisDiff / 60000L).coerceAtLeast(0)
    val relativeStartLabel = when {
        minutesUntilStart <= 0 -> "Starts Immediately"
        minutesUntilStart < 60 -> "Starts in $minutesUntilStart mins"
        minutesUntilStart < 1440 -> "Starts in ${minutesUntilStart / 60}h ${minutesUntilStart % 60}m"
        else -> "Starts in ${minutesUntilStart / 1440} days"
    }

    val activeRanks = state.newMatchRankPrizes.entries.sortedBy { it.key }
    val totalDistributed = state.newMatchRankPrizes.values.sum()
    val missingRanks = (1..10).filter { !state.newMatchRankPrizes.containsKey(it) }

    AlertDialog(
        onDismissRequest = { adminViewModel.closeCreateMatchDialog() },
        containerColor = CarbonCardElevated,
        title = {
            val dialogTitle = when {
                isEditing && state.newMatchIsSurvival -> "EDIT SURVIVAL ROOM (TOP 10)"
                isEditing -> "EDIT PER KILL CUSTOM ROOM"
                state.newMatchIsSurvival -> "CREATE SURVIVAL ROOM (TOP 10)"
                else -> "CREATE PER KILL CUSTOM ROOM"
            }
            val dialogSubtitle = if (state.newMatchIsSurvival) {
                "Championship format: Set total prize pool, rank 1 to 10 rewards, and entry fee"
            } else {
                "Bounty format: Set entry fee, per kill rewards, and room schedule"
            }
            Column {
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = if (state.newMatchIsSurvival) CyberGold else NeonCyan
                )
                Text(
                    text = dialogSubtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextMuted
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ==========================================
                // 1. DATE & TIME SELECTION SECTION
                // ==========================================
                Text(
                    text = "MATCH SCHEDULE (DATE & TIME)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    ),
                    color = CyberGold
                )

                // Live Schedule Summary Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CarbonSurface)
                        .border(1.dp, CyberGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = CyberGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$displayDate • $displayTime",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                ),
                                color = CyberGoldLight
                            )
                        }
                        Text(
                            text = relativeStartLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = NeonGreen
                        )
                    }
                }

                // Interactive Date & Time Pick Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date Picker Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CarbonSurface)
                            .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val cal = Calendar.getInstance().apply {
                                            timeInMillis = state.newMatchStartTimeMillis
                                            set(Calendar.YEAR, year)
                                            set(Calendar.MONTH, month)
                                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        }
                                        adminViewModel.onNewMatchStartTimeChanged(cal.timeInMillis)
                                    },
                                    scheduleCal.get(Calendar.YEAR),
                                    scheduleCal.get(Calendar.MONTH),
                                    scheduleCal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                            .testTag("admin_select_date_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select Date",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "SELECT DATE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = TextMuted
                                )
                                Text(
                                    text = displayDate,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    ),
                                    color = NeonCyanLight
                                )
                            }
                        }
                    }

                    // Time Picker Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CarbonSurface)
                            .border(1.dp, CyberGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        val cal = Calendar.getInstance().apply {
                                            timeInMillis = state.newMatchStartTimeMillis
                                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            set(Calendar.MINUTE, minute)
                                            set(Calendar.SECOND, 0)
                                        }
                                        adminViewModel.onNewMatchStartTimeChanged(cal.timeInMillis)
                                    },
                                    scheduleCal.get(Calendar.HOUR_OF_DAY),
                                    scheduleCal.get(Calendar.MINUTE),
                                    false
                                ).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                            .testTag("admin_select_time_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Select Time",
                                tint = CyberGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "SELECT TIME",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = TextMuted
                                )
                                Text(
                                    text = displayTime,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    ),
                                    color = CyberGoldLight
                                )
                            }
                        }
                    }
                }

                // Quick Date Presets: Today | Tomorrow | Day After
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Today" to 0, "Tomorrow" to 1, "Day After" to 2).forEach { (label, dayOffset) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CarbonSurface)
                                .border(0.8.dp, CarbonBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    val nowCal = Calendar.getInstance()
                                    val targetCal = Calendar.getInstance().apply {
                                        timeInMillis = state.newMatchStartTimeMillis
                                        set(Calendar.YEAR, nowCal.get(Calendar.YEAR))
                                        set(Calendar.DAY_OF_YEAR, nowCal.get(Calendar.DAY_OF_YEAR) + dayOffset)
                                    }
                                    adminViewModel.onNewMatchStartTimeChanged(targetCal.timeInMillis)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Quick Time Presets: +15m | +30m | +1h | +2h
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    listOf(
                        "+15m" to (15 * 60 * 1000L),
                        "+30m" to (30 * 60 * 1000L),
                        "+1h" to (60 * 60 * 1000L),
                        "+2h" to (120 * 60 * 1000L)
                    ).forEach { (label, deltaMillis) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CarbonSurface)
                                .border(0.8.dp, CarbonBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    val newStart = System.currentTimeMillis() + deltaMillis
                                    adminViewModel.onNewMatchStartTimeChanged(newStart)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = NeonCyanLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ==========================================
                // 2. ROOM TYPE / REWARD MODE SELECTOR
                // ==========================================
                Text(
                    text = "SELECT ROOM TYPE / MODE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    ),
                    color = CyberGold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: Survival (Top 10 Prize Pool)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (state.newMatchIsSurvival) CyberGold.copy(alpha = 0.25f) else CarbonSurface)
                            .border(1.5.dp, if (state.newMatchIsSurvival) CyberGold else CarbonBorder, RoundedCornerShape(10.dp))
                            .clickable { adminViewModel.onNewMatchIsSurvivalChanged(true) }
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                            .testTag("admin_select_survival_mode_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "🏆 Survival (Top 10)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = if (state.newMatchIsSurvival) CyberGoldLight else TextSecondary
                            )
                            Text(
                                "Custom Prize Pool",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = if (state.newMatchIsSurvival) NeonGreen else TextMuted
                            )
                        }
                    }

                    // Option 2: Per Kill Arena
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!state.newMatchIsSurvival) NeonCyan.copy(alpha = 0.2f) else CarbonSurface)
                            .border(1.5.dp, if (!state.newMatchIsSurvival) NeonCyan else CarbonBorder, RoundedCornerShape(10.dp))
                            .clickable { adminViewModel.onNewMatchIsSurvivalChanged(false) }
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                            .testTag("admin_select_perkill_mode_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "⚔️ Per Kill Arena",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = if (!state.newMatchIsSurvival) NeonCyanLight else TextSecondary
                            )
                            Text(
                                "Bounties per kill",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ==========================================
                // 3A. SURVIVAL ROOM CONFIGURATION (FULL CONTROL)
                // ==========================================
                if (state.newMatchIsSurvival) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CarbonSurface)
                            .border(1.dp, CyberGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = CyberGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SURVIVAL TOTAL PRIZE POOL (MANUAL INPUT)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.6.sp
                                ),
                                color = CyberGold
                            )
                        }

                        Text(
                            text = "Type any custom amount in the box below (e.g. ₹500, ₹1000, ₹2500, ₹5000, ₹10000) or tap quick presets:",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )

                        // PRIMARY MANUAL INPUT TEXT BOX (HIGHLIGHTED)
                        OutlinedTextField(
                            value = state.newMatchPrizePoolInput,
                            onValueChange = { adminViewModel.onPrizePoolInputChanged(it) },
                            label = { Text("Total Prize Pool (₹) - Type Any Amount", fontWeight = FontWeight.Bold) },
                            placeholder = { Text("e.g. 500, 1000, 2500, 5000", color = TextMuted) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = CyberGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            prefix = {
                                Text("₹ ", color = CyberGold, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberGold,
                                unfocusedBorderColor = CyberGold.copy(alpha = 0.7f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = CarbonCardElevated,
                                unfocusedContainerColor = CarbonCardElevated,
                                focusedLabelColor = CyberGoldLight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_manual_prize_pool_input")
                        )

                        // Quick 1-Tap Presets
                        Text(
                            text = "QUICK 1-TAP PRESETS (OPTIONAL):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = TextMuted
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(500.0, 1000.0, 1500.0, 2000.0, 3000.0, 5000.0).forEach { poolAmt ->
                                val isSelectedPool = state.newMatchSurvivalPrizePool == poolAmt
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelectedPool) CyberGold.copy(alpha = 0.25f) else CarbonCardElevated)
                                        .border(1.dp, if (isSelectedPool) CyberGold else CarbonBorder, RoundedCornerShape(8.dp))
                                        .clickable { adminViewModel.onNewMatchSurvivalPrizePoolChanged(poolAmt) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "₹${poolAmt.toInt()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                        color = if (isSelectedPool) CyberGoldLight else TextSecondary
                                    )
                                }
                            }
                        }

                        // Auto-Split Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { adminViewModel.onAutoDistributePrizes(state.newMatchSurvivalPrizePool) }
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "⚡ Auto-Distribute ₹${state.newMatchSurvivalPrizePool.toInt()} to Top 10 Ranks",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = NeonCyanLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // SURVIVAL ENTRY FEE
                        Text(
                            text = "SURVIVAL MATCH ENTRY FEE (₹)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = NeonCyan
                        )

                        // Quick Entry Fee Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(10.0, 20.0, 30.0, 50.0, 100.0).forEach { fee ->
                                val isSelectedFee = state.newMatchEntryFee == fee
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelectedFee) NeonCyan.copy(alpha = 0.25f) else CarbonCardElevated)
                                        .border(1.dp, if (isSelectedFee) NeonCyan else CarbonBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            adminViewModel.onNewMatchFieldChanged(entryFee = fee)
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "₹${fee.toInt()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                        color = if (isSelectedFee) NeonCyanLight else TextSecondary
                                    )
                                }
                            }
                        }

                        // Custom Entry Fee Input Field
                        OutlinedTextField(
                            value = state.newMatchEntryFeeInput,
                            onValueChange = { adminViewModel.onEntryFeeInputChanged(it) },
                            label = { Text("Custom Entry Fee (₹)") },
                            prefix = { Text("₹ ", color = NeonCyan, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = adminDialogFieldColors(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_custom_entry_fee_input")
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Live Distribution Summary Badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CarbonCardElevated)
                                .border(0.8.dp, CarbonBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isBalanced = totalDistributed == state.newMatchSurvivalPrizePool
                                Column {
                                    Text(
                                        text = "Distributed: ₹${totalDistributed.toInt()} / ₹${state.newMatchSurvivalPrizePool.toInt()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                        color = if (isBalanced) NeonGreen else CyberGoldLight
                                    )
                                    Text(
                                        text = if (isBalanced) "✓ 100% Balanced" else "⚠️ ₹${(state.newMatchSurvivalPrizePool - totalDistributed).toInt()} Remaining",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (isBalanced) NeonGreen else DangerRed
                                    )
                                }
                                Text(
                                    text = "${activeRanks.size} Paid Ranks",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = TextMuted
                                )
                            }
                        }

                        Text(
                            text = "INDIVIDUAL RANK REWARDS (FULL CONTROL - EDIT & DELETE)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = NeonCyan
                        )

                        // List of Rank Rows
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            activeRanks.forEach { (rank, amount) ->
                                val rankLabel = when (rank) {
                                    1 -> "🏆 Booyah (Rank 1)"
                                    2 -> "🥈 Rank 2"
                                    3 -> "🥉 Rank 3"
                                    else -> "Rank $rank"
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CarbonCardElevated)
                                        .border(0.8.dp, CarbonBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = rankLabel,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (rank == 1) FontWeight.Black else FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = if (rank == 1) CyberGoldLight else TextPrimary,
                                        modifier = Modifier.weight(1.3f)
                                    )

                                    // Editable Prize Amount
                                    OutlinedTextField(
                                        value = amount.toInt().toString(),
                                        onValueChange = { newVal ->
                                            val parsed = newVal.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
                                            adminViewModel.onUpdateRankPrize(rank, parsed)
                                        },
                                        label = { Text("Prize (₹)", fontSize = 9.sp) },
                                        prefix = { Text("₹", fontSize = 10.sp, color = CyberGold) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        colors = adminDialogFieldColors(),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .width(105.dp)
                                            .testTag("admin_rank_${rank}_prize_input")
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Delete Rank Button
                                    IconButton(
                                        onClick = { adminViewModel.onDeleteRankPrize(rank) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Rank $rank",
                                            tint = DangerRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Add Back Deleted Ranks
                        if (missingRanks.isNotEmpty()) {
                            Text(
                                text = "ADD DELETED RANKS BACK:",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = TextMuted
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                missingRanks.forEach { rank ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NeonGreen.copy(alpha = 0.15f))
                                            .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .clickable { adminViewModel.onAddRankPrize(rank, 30.0) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("+ Rank $rank", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = NeonGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // 3B. PER KILL ARENA BOUNTY & ENTRY FEE (FULL CONTROL)
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CarbonSurface)
                            .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "PER KILL ARENA BOUNTY & ENTRY FEE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            ),
                            color = NeonCyan
                        )

                        // Quick preset buttons for instant 1-tap setup
                        Text(
                            text = "OFFICIAL SOLO TIERS (1-TAP PRESET):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = TextMuted
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            officialTiers.forEach { (fee, kill) ->
                                val isSelected = state.newMatchEntryFee == fee && state.newMatchPerKill == kill
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) CyberGold.copy(alpha = 0.25f) else CarbonCardElevated)
                                        .border(
                                            1.dp,
                                            if (isSelected) CyberGold else CarbonBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            adminViewModel.onNewMatchFieldChanged(
                                                entryFee = fee,
                                                perKill = kill
                                            )
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "₹${fee.toInt()}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                            color = if (isSelected) CyberGoldLight else TextPrimary
                                        )
                                        Text(
                                            text = "₹${kill.toInt()}/k",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = if (isSelected) CyberGold else TextMuted
                                        )
                                    }
                                }
                            }
                        }

                        // Custom Entry Fee & Per Kill Input Fields (Allows any custom amount)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = state.newMatchEntryFeeInput,
                                onValueChange = { adminViewModel.onEntryFeeInputChanged(it) },
                                label = { Text("Custom Entry Fee (₹)") },
                                prefix = { Text("₹ ", color = NeonCyan, fontWeight = FontWeight.Bold) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = adminDialogFieldColors(),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_custom_entry_fee_input")
                            )

                            OutlinedTextField(
                                value = state.newMatchPerKillInput,
                                onValueChange = { adminViewModel.onPerKillInputChanged(it) },
                                label = { Text("Per Kill Reward (₹)") },
                                prefix = { Text("₹ ", color = CyberGold, fontWeight = FontWeight.Bold) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = adminDialogFieldColors(),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_custom_per_kill_input")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ==========================================
                // 4. ROOM FORMAT & CUSTOM MODE
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ROOM FORMAT & MODE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = NeonCyan
                    )

                    Text(
                        text = "${state.newMatchMatchType.label} (${state.newMatchTotalSlots} Slots)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = CyberGoldLight
                    )
                }

                // Format chips: 1 vs 1, 2 vs 2, 4 vs 4, Solo, Duo, Squad, Custom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Triple("1 vs 1", MatchType.ONE_VS_ONE, 2),
                        Triple("2 vs 2", MatchType.TWO_VS_TWO, 4),
                        Triple("4 vs 4", MatchType.FOUR_VS_FOUR, 8),
                        Triple("Solo", MatchType.SOLO, 48),
                        Triple("Duo", MatchType.DUO, 48),
                        Triple("Squad", MatchType.SQUAD, 48),
                        Triple("Custom", MatchType.CUSTOM, state.newMatchTotalSlots)
                    ).forEach { (label, format, defaultSlots) ->
                        val isSelected = state.newMatchMatchType == format
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.25f) else CarbonSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonCyan else CarbonBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    adminViewModel.onNewMatchFormatSelected(format, defaultSlots)
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                ),
                                color = if (isSelected) NeonCyanLight else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ==========================================
                // 5. TOTAL SLOTS (1 TO 48) - CUSTOMIZABLE
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL SLOTS (1 TO 48)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = CyberGold
                    )

                    Text(
                        text = "${state.newMatchTotalSlots} Players",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                        color = NeonGreen
                    )
                }

                // Quick Slot Presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 4, 8, 12, 16, 24, 48).forEach { s ->
                        val isSelected = state.newMatchTotalSlots == s
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyberGold.copy(alpha = 0.25f) else CarbonSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) CyberGold else CarbonBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { adminViewModel.onNewMatchSlotsChanged(s) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "$s Slots",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (isSelected) CyberGoldLight else TextMuted
                            )
                        }
                    }
                }

                // Interactive Slot Stepper & Direct Editable Input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CarbonSurface)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            val next = (state.newMatchTotalSlots - 1).coerceAtLeast(1)
                            adminViewModel.onNewMatchSlotsChanged(next)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CarbonCardElevated)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease Slots",
                            tint = if (state.newMatchTotalSlots > 1) NeonCyan else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    OutlinedTextField(
                        value = state.newMatchSlotInput,
                        onValueChange = { adminViewModel.onNewMatchSlotInputChanged(it) },
                        label = { Text("Edit Slots (1-48)", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = adminDialogFieldColors(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(130.dp)
                            .testTag("admin_slots_input")
                    )

                    IconButton(
                        onClick = {
                            val next = (state.newMatchTotalSlots + 1).coerceAtMost(48)
                            adminViewModel.onNewMatchSlotsChanged(next)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CarbonCardElevated)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase Slots",
                            tint = if (state.newMatchTotalSlots < 48) NeonGreen else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Interactive Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = state.newMatchTotalSlots.toFloat(),
                        onValueChange = { adminViewModel.onNewMatchSlotsChanged(it.toInt()) },
                        valueRange = 1f..48f,
                        steps = 46,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberGold,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = CarbonBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ==========================================
                // 6. TOURNAMENT TITLE & ROOM CREDENTIALS
                // ==========================================
                OutlinedTextField(
                    value = state.newMatchTitle,
                    onValueChange = { adminViewModel.onNewMatchFieldChanged(title = it) },
                    label = { Text("Match Title") },
                    singleLine = true,
                    colors = adminDialogFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_create_title_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.newMatchRoomId,
                        onValueChange = { adminViewModel.onNewMatchFieldChanged(roomId = it) },
                        label = { Text("Room ID") },
                        singleLine = true,
                        colors = adminDialogFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = state.newMatchPassword,
                        onValueChange = { adminViewModel.onNewMatchFieldChanged(pass = it) },
                        label = { Text("Room Password") },
                        singleLine = true,
                        colors = adminDialogFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Neon3DButton(
                text = if (isEditing) "SAVE CHANGES" else "PUBLISH TOURNAMENT",
                onClick = { adminViewModel.createTournament() },
                style = Neon3DStyle.GOLD,
                modifier = Modifier.testTag("admin_confirm_create_tournament_button")
            )
        },
        dismissButton = {
            TextButton(
                onClick = { adminViewModel.closeCreateMatchDialog() },
                modifier = Modifier.testTag("admin_cancel_create_tournament_button")
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun adminDialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = CarbonBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = CarbonSurface,
    unfocusedContainerColor = CarbonSurface
)
