package progamerbattle.com.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import progamerbattle.com.model.MatchPlayer
import progamerbattle.com.model.MatchStatus
import progamerbattle.com.model.MatchType
import progamerbattle.com.model.ParticipantRecord
import progamerbattle.com.model.TournamentMatch

class MatchRepository {

    private val realtimeDatabase: FirebaseDatabase? = try {
        FirebaseDatabase.getInstance("https://new-project-bf7df-default-rtdb.firebaseio.com")
    } catch (_: Throwable) {
        null
    }

    // Production clean start: zero demo/sample matches
    private val _matches = MutableStateFlow<List<TournamentMatch>>(emptyList())
    val matches: StateFlow<List<TournamentMatch>> = _matches.asStateFlow()

    init {
        attachRealtimeMatchesListener()
    }

    private fun attachRealtimeMatchesListener() {
        val ref = realtimeDatabase?.getReference(RTDB_MATCHES_NODE) ?: return
        try {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        _matches.value = emptyList()
                        return
                    }
                    val loaded = mutableListOf<TournamentMatch>()
                    for (child in snapshot.children) {
                        parseMatchSnapshot(child)?.let { loaded.add(it) }
                    }
                    _matches.value = loaded.sortedBy { it.startTimeMillis }
                }

                override fun onCancelled(error: DatabaseError) {
                    try {
                        Log.w(TAG, "Matches RTDB listener cancelled: ${error.message}")
                    } catch (_: Throwable) {}
                }
            })
        } catch (e: Throwable) {
            try {
                Log.w(TAG, "Failed attaching matches listener: ${e.message}")
            } catch (_: Throwable) {}
        }
    }

    private fun parseMatchSnapshot(child: DataSnapshot): TournamentMatch? {
        return try {
            val id = child.child("id").getValue(String::class.java) ?: child.key ?: return null
            val title = child.child("title").getValue(String::class.java) ?: return null
            val map = child.child("map").getValue(String::class.java) ?: "Bermuda"
            val matchTypeName = child.child("matchType").getValue(String::class.java) ?: MatchType.SOLO.name
            val matchType = try { MatchType.valueOf(matchTypeName) } catch (_: Exception) { MatchType.SOLO }
            val perspective = child.child("perspective").getValue(String::class.java) ?: "TPP"
            val startTimeMillis = (child.child("startTimeMillis").value as? Number)?.toLong() ?: System.currentTimeMillis()
            val entryFee = (child.child("entryFee").value as? Number)?.toDouble() ?: 0.0
            val perKillReward = (child.child("perKillReward").value as? Number)?.toDouble() ?: 0.0
            val totalSlots = (child.child("totalSlots").value as? Number)?.toInt() ?: 48
            val joinedPlayersCount = (child.child("joinedPlayersCount").value as? Number)?.toInt() ?: 0
            val roomId = child.child("roomId").getValue(String::class.java) ?: ""
            val roomPassword = child.child("roomPassword").getValue(String::class.java) ?: ""
            val statusName = child.child("status").getValue(String::class.java) ?: MatchStatus.UPCOMING.name
            val status = try { MatchStatus.valueOf(statusName) } catch (_: Exception) { MatchStatus.UPCOMING }
            val winnerName = child.child("winnerName").getValue(String::class.java) ?: ""
            val isSurvivalMode = child.child("isSurvivalMode").getValue(Boolean::class.java) ?: false
            val survivalPrizePool = (child.child("survivalPrizePool").value as? Number)?.toDouble() ?: 1000.0
            val customPrizePool = (child.child("customPrizePool").value as? Number)?.toDouble()

            val joinedUids = child.child("joinedPlayerUids").children.mapNotNull {
                it.getValue(String::class.java)
            }

            val enrolledPlayers = child.child("enrolledPlayers").children.mapNotNull { p ->
                val uid = p.child("uid").getValue(String::class.java) ?: ""
                val ign = p.child("ign").getValue(String::class.java) ?: ""
                val uidNum = p.child("uidNumber").getValue(String::class.java) ?: ""
                val mobile = p.child("mobile").getValue(String::class.java) ?: ""
                val joinedAt = (p.child("joinedAt").value as? Number)?.toLong() ?: System.currentTimeMillis()
                if (uid.isBlank() && ign.isBlank()) null else MatchPlayer(uid, ign, uidNum, mobile, joinedAt)
            }

            val customParticipants = child.child("customParticipants").children.mapNotNull { p ->
                val uid = p.child("uid").getValue(String::class.java) ?: ""
                val name = p.child("name").getValue(String::class.java) ?: ""
                val uidNum = p.child("uidNumber").getValue(String::class.java) ?: ""
                val kills = (p.child("kills").value as? Number)?.toInt() ?: 0
                if (name.isBlank()) null else ParticipantRecord(uid, name, uidNum, kills)
            }.takeIf { it.isNotEmpty() }

            val customRankPrizes = child.child("customRankPrizes").children.mapNotNull { entry ->
                val rank = entry.key?.toIntOrNull() ?: return@mapNotNull null
                val prize = (entry.value as? Number)?.toDouble() ?: return@mapNotNull null
                rank to prize
            }.toMap().takeIf { it.isNotEmpty() }

            TournamentMatch(
                id = id,
                title = title,
                map = map,
                matchType = matchType,
                perspective = perspective,
                startTimeMillis = startTimeMillis,
                entryFee = entryFee,
                perKillReward = perKillReward,
                totalSlots = totalSlots,
                joinedPlayersCount = joinedPlayersCount,
                joinedPlayerUids = joinedUids,
                enrolledPlayers = enrolledPlayers,
                roomId = roomId,
                roomPassword = roomPassword,
                status = status,
                winnerName = winnerName,
                customPrizePool = customPrizePool,
                customParticipants = customParticipants,
                isSurvivalMode = isSurvivalMode,
                survivalPrizePool = survivalPrizePool,
                customRankPrizes = customRankPrizes
            )
        } catch (_: Throwable) {
            null
        }
    }

    private fun syncMatchToCloud(match: TournamentMatch) {
        try {
            val ref = realtimeDatabase?.getReference(RTDB_MATCHES_NODE)?.child(match.id) ?: return
            val map = hashMapOf<String, Any?>(
                "id" to match.id,
                "title" to match.title,
                "map" to match.map,
                "matchType" to match.matchType.name,
                "perspective" to match.perspective,
                "startTimeMillis" to match.startTimeMillis,
                "entryFee" to match.entryFee,
                "perKillReward" to match.perKillReward,
                "totalSlots" to match.totalSlots,
                "joinedPlayersCount" to match.joinedPlayersCount,
                "joinedPlayerUids" to match.joinedPlayerUids,
                "enrolledPlayers" to match.enrolledPlayers.map {
                    mapOf(
                        "uid" to it.uid,
                        "ign" to it.ign,
                        "uidNumber" to it.uidNumber,
                        "mobile" to it.mobile,
                        "joinedAt" to it.joinedAt
                    )
                },
                "roomId" to match.roomId,
                "roomPassword" to match.roomPassword,
                "status" to match.status.name,
                "winnerName" to match.winnerName,
                "isSurvivalMode" to match.isSurvivalMode,
                "survivalPrizePool" to match.survivalPrizePool,
                "customPrizePool" to match.customPrizePool,
                "customParticipants" to match.customParticipants?.map {
                    mapOf(
                        "uid" to it.uid,
                        "name" to it.name,
                        "uidNumber" to it.uidNumber,
                        "kills" to it.kills
                    )
                },
                "customRankPrizes" to match.customRankPrizes?.mapKeys { it.key.toString() }
            )
            ref.setValue(map)
        } catch (_: Throwable) {}
    }

    fun joinMatch(matchId: String, player: MatchPlayer): Boolean {
        var success = false
        var updatedMatch: TournamentMatch? = null
        _matches.update { currentList ->
            currentList.map { match ->
                if (match.id == matchId) {
                    if (!match.isRegistrationClosed && !match.joinedPlayerUids.contains(player.uid)) {
                        success = true
                        val updated = match.copy(
                            joinedPlayersCount = match.joinedPlayersCount + 1,
                            joinedPlayerUids = match.joinedPlayerUids + player.uid,
                            enrolledPlayers = match.enrolledPlayers + player
                        )
                        updatedMatch = updated
                        updated
                    } else {
                        match
                    }
                } else {
                    match
                }
            }
        }
        updatedMatch?.let { syncMatchToCloud(it) }
        return success
    }

    // --- ADMIN METHODS ---

    fun createMatch(match: TournamentMatch): Boolean {
        _matches.update { listOf(match) + it }
        syncMatchToCloud(match)
        return true
    }

    fun updateMatch(updatedMatch: TournamentMatch): Boolean {
        var updated = false
        _matches.update { list ->
            list.map { match ->
                if (match.id == updatedMatch.id) {
                    updated = true
                    updatedMatch
                } else {
                    match
                }
            }
        }
        if (updated) syncMatchToCloud(updatedMatch)
        return updated
    }

    fun updateRoomCredentials(matchId: String, roomId: String, roomPassword: String): Boolean {
        var updatedMatch: TournamentMatch? = null
        _matches.update { list ->
            list.map { match ->
                if (match.id == matchId) {
                    val m = match.copy(roomId = roomId.trim(), roomPassword = roomPassword.trim())
                    updatedMatch = m
                    m
                } else {
                    match
                }
            }
        }
        updatedMatch?.let { syncMatchToCloud(it) }
        return updatedMatch != null
    }

    fun updateMatchStatus(matchId: String, newStatus: MatchStatus): Boolean {
        var updatedMatch: TournamentMatch? = null
        _matches.update { list ->
            list.map { match ->
                if (match.id == matchId) {
                    val m = match.copy(status = newStatus)
                    updatedMatch = m
                    m
                } else {
                    match
                }
            }
        }
        updatedMatch?.let { syncMatchToCloud(it) }
        return updatedMatch != null
    }

    fun declareMatchResults(
        matchId: String,
        winnerName: String,
        participants: List<ParticipantRecord>
    ): Boolean {
        var updatedMatch: TournamentMatch? = null
        _matches.update { list ->
            list.map { match ->
                if (match.id == matchId) {
                    val m = match.copy(
                        status = MatchStatus.COMPLETED,
                        winnerName = winnerName.trim(),
                        customParticipants = participants
                    )
                    updatedMatch = m
                    m
                } else {
                    match
                }
            }
        }
        updatedMatch?.let { syncMatchToCloud(it) }
        return updatedMatch != null
    }

    fun cancelMatch(matchId: String): TournamentMatch? {
        var cancelledMatch: TournamentMatch? = null
        _matches.update { list ->
            list.map { match ->
                if (match.id == matchId) {
                    val updated = match.copy(status = MatchStatus.CANCELLED)
                    cancelledMatch = updated
                    updated
                } else {
                    match
                }
            }
        }
        cancelledMatch?.let { syncMatchToCloud(it) }
        return cancelledMatch
    }

    fun deleteMatch(matchId: String): Boolean {
        var removed = false
        _matches.update { list ->
            val filtered = list.filterNot { it.id == matchId }
            removed = filtered.size != list.size
            filtered
        }
        if (removed) {
            try {
                realtimeDatabase?.getReference(RTDB_MATCHES_NODE)?.child(matchId)?.removeValue()
            } catch (_: Throwable) {}
        }
        return removed
    }

    companion object {
        private const val TAG = "MatchRepository"
        private const val RTDB_MATCHES_NODE = "custom_matches"

        @Volatile
        private var instance: MatchRepository? = null

        fun getInstance(): MatchRepository {
            return instance ?: synchronized(this) {
                instance ?: MatchRepository().also { instance = it }
            }
        }
    }
}
