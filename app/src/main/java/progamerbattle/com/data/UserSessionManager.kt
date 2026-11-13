package progamerbattle.com.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import progamerbattle.com.model.User
import java.nio.charset.StandardCharsets

/**
 * Manages persistent local user sessions using private secure SharedPreferences.
 * Stores active login credentials, profile information, and balances.
 * Enables automatic bypass of the login screen on subsequent app launches.
 */
class UserSessionManager(
    private val prefs: SharedPreferences
) {

    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    fun hasActiveSession(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) &&
                !prefs.getString(KEY_MOBILE_NUMBER, null).isNullOrBlank()
    }

    fun saveUserSession(user: User) {
        try {
            val encodedPin = encodeString(user.secretPin)
            prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_ID, user.uid)
                .putString(KEY_FULL_NAME, user.fullName)
                .putString(KEY_MOBILE_NUMBER, user.mobileNumber)
                .putString(KEY_FF_IGN, user.ffIgn)
                .putString(KEY_FF_UID, user.ffUid)
                .putString(KEY_SECRET_PIN, encodedPin)
                .putFloat(KEY_DEPOSIT_BALANCE, user.depositBalance.toFloat())
                .putFloat(KEY_WINNING_BALANCE, user.winningBalance.toFloat())
                .putFloat(KEY_BONUS_BALANCE, user.bonusBalance.toFloat())
                .putInt(KEY_TOTAL_KILLS, user.totalKills)
                .putInt(KEY_MATCHES_WON, user.matchesWon)
                .putInt(KEY_MATCHES_PLAYED, user.matchesPlayed)
                .putString(KEY_REFERRAL_CODE, user.myReferralCode)
                .putString(KEY_REFERRED_BY, user.referredBy)
                .putInt(KEY_TOTAL_REFERRALS, user.totalReferrals)
                .putFloat(KEY_REFERRAL_EARNINGS, user.referralEarnings.toFloat())
                .putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
                .putStringSet(KEY_JOINED_MATCHES, user.joinedMatchIds.toSet())
                .apply()
            try {
                Log.d(TAG, "Saved persistent user session for ${user.mobileNumber}")
            } catch (_: Throwable) {}
        } catch (e: Exception) {
            try {
                Log.e(TAG, "Failed to save user session", e)
            } catch (_: Throwable) {}
        }
    }

    fun getUserSession(): User? {
        if (!hasActiveSession()) return null

        return try {
            val uid = prefs.getString(KEY_USER_ID, "") ?: ""
            val fullName = prefs.getString(KEY_FULL_NAME, "Gamer") ?: "Gamer"
            val mobileNumber = prefs.getString(KEY_MOBILE_NUMBER, "") ?: ""
            val ffIgn = prefs.getString(KEY_FF_IGN, "ProGamer") ?: "ProGamer"
            val ffUid = prefs.getString(KEY_FF_UID, "000000") ?: "000000"
            val encodedPin = prefs.getString(KEY_SECRET_PIN, "") ?: ""
            val decodedPin = decodeString(encodedPin)
            val deposit = prefs.getFloat(KEY_DEPOSIT_BALANCE, 0f).toDouble()
            val winning = prefs.getFloat(KEY_WINNING_BALANCE, 0f).toDouble()
            val bonus = prefs.getFloat(KEY_BONUS_BALANCE, 10f).toDouble()
            val totalKills = prefs.getInt(KEY_TOTAL_KILLS, 0)
            val matchesWon = prefs.getInt(KEY_MATCHES_WON, 0)
            val matchesPlayed = prefs.getInt(KEY_MATCHES_PLAYED, 0)
            val refCode = prefs.getString(KEY_REFERRAL_CODE, "") ?: ""
            val referredBy = prefs.getString(KEY_REFERRED_BY, "") ?: ""
            val totalRefs = prefs.getInt(KEY_TOTAL_REFERRALS, 0)
            val refEarnings = prefs.getFloat(KEY_REFERRAL_EARNINGS, 0f).toDouble()
            val joinedMatches = prefs.getStringSet(KEY_JOINED_MATCHES, emptySet())?.toList() ?: emptyList()

            if (mobileNumber.isBlank()) {
                null
            } else {
                User(
                    uid = uid.ifBlank { "user_$mobileNumber" },
                    fullName = fullName,
                    mobileNumber = mobileNumber,
                    ffIgn = ffIgn,
                    ffUid = ffUid,
                    secretPin = decodedPin,
                    depositBalance = deposit,
                    winningBalance = winning,
                    bonusBalance = bonus,
                    joinedMatchIds = joinedMatches,
                    totalKills = totalKills,
                    matchesWon = matchesWon,
                    matchesPlayed = matchesPlayed,
                    referralCode = refCode,
                    referredBy = referredBy,
                    totalReferrals = totalRefs,
                    referralEarnings = refEarnings
                )
            }
        } catch (e: Exception) {
            try {
                Log.e(TAG, "Failed reading user session", e)
            } catch (_: Throwable) {}
            null
        }
    }

    fun clearSession() {
        try {
            prefs.edit().clear().apply()
            try {
                Log.d(TAG, "Cleared persistent user session")
            } catch (_: Throwable) {}
        } catch (e: Exception) {
            try {
                Log.e(TAG, "Failed clearing user session", e)
            } catch (_: Throwable) {}
        }
    }

    private fun encodeString(value: String): String {
        return try {
            java.util.Base64.getEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))
        } catch (_: Throwable) {
            try {
                android.util.Base64.encodeToString(value.toByteArray(StandardCharsets.UTF_8), android.util.Base64.NO_WRAP)
            } catch (_: Throwable) {
                value
            }
        }
    }

    private fun decodeString(encoded: String): String {
        return try {
            String(java.util.Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8)
        } catch (_: Throwable) {
            try {
                String(android.util.Base64.decode(encoded, android.util.Base64.NO_WRAP), StandardCharsets.UTF_8)
            } catch (_: Throwable) {
                encoded
            }
        }
    }

    companion object {
        private const val TAG = "UserSessionManager"
        const val PREFS_NAME = "pgb_user_session_pref"

        const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        const val KEY_USER_ID = "key_user_id"
        const val KEY_FULL_NAME = "key_full_name"
        const val KEY_MOBILE_NUMBER = "key_mobile_number"
        const val KEY_FF_IGN = "key_ff_ign"
        const val KEY_FF_UID = "key_ff_uid"
        const val KEY_SECRET_PIN = "key_secret_pin"
        const val KEY_DEPOSIT_BALANCE = "key_deposit_balance"
        const val KEY_WINNING_BALANCE = "key_winning_balance"
        const val KEY_BONUS_BALANCE = "key_bonus_balance"
        const val KEY_TOTAL_KILLS = "key_total_kills"
        const val KEY_MATCHES_WON = "key_matches_won"
        const val KEY_MATCHES_PLAYED = "key_matches_played"
        const val KEY_JOINED_MATCHES = "key_joined_matches"
        const val KEY_REFERRAL_CODE = "key_referral_code"
        const val KEY_REFERRED_BY = "key_referred_by"
        const val KEY_TOTAL_REFERRALS = "key_total_referrals"
        const val KEY_REFERRAL_EARNINGS = "key_referral_earnings"
        const val KEY_LAST_LOGIN_TIME = "key_last_login_time"
    }
}
