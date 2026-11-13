package progamerbattle.com.data

import android.content.Context
import android.util.Log
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import progamerbattle.com.BuildConfig

data class RemoteConfigModel(
    val whatsappLink: String = "https://wa.me/message/X53ULJ34V2FZA1",
    val telegramLink: String = "https://t.me/ProGamerBattle",
    val isMaintenanceMode: Boolean = false,
    val announcementMessage: String = "",
    val minVersion: Long = 1L,
    val minVersionStr: String = "1.0",
    val updateUrl: String = "https://progamerbattle.com/download",
    val isUpdateRequired: Boolean = false,
    val lastFetchedAt: Long = 0L,
    val isFetching: Boolean = false,
    val fetchErrorMessage: String? = null
)

/**
 * Real-time Firebase Remote Config Manager.
 * Syncs user parameters defined in Firebase Console:
 * - whatsapp link (whatsapp_link, whatsapp_url, whatsapp)
 * - telegram link (telegram_link, teligram_link, telegram_url, telegram)
 * - maintenance mode (maintenance_mode, maintenance_mod, maintenance)
 * - announcement message (announcement_message, announcement_massege, announcement)
 * - min_version (min_version, min_version_code, minVersion)
 * - update url (update_url, updateUrl, app_update_url)
 */
class RemoteConfigManager private constructor(
    private val appContext: Context?
) {

    private val _config = MutableStateFlow(RemoteConfigModel())
    val config: StateFlow<RemoteConfigModel> = _config.asStateFlow()

    private val remoteConfig: FirebaseRemoteConfig? = try {
        FirebaseRemoteConfig.getInstance()
    } catch (e: Throwable) {
        Log.w(TAG, "FirebaseRemoteConfig unavailable: ${e.message}")
        null
    }

    private val currentAppVersionCode: Long = BuildConfig.VERSION_CODE.toLong()
    private val currentAppVersionName: String = BuildConfig.VERSION_NAME

    init {
        setupRemoteConfig()
    }

    private fun setupRemoteConfig() {
        val rc = remoteConfig ?: return

        try {
            // Configure 0 second fetch interval for instant sync with Firebase console
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0L)
                .setFetchTimeoutInSeconds(15L)
                .build()
            rc.setConfigSettingsAsync(configSettings)

            // In-app fallback defaults
            val defaults = mapOf<String, Any>(
                KEY_WHATSAPP_LINK to "https://wa.me/message/X53ULJ34V2FZA1",
                KEY_WHATSAPP_URL to "https://wa.me/message/X53ULJ34V2FZA1",
                KEY_TELEGRAM_LINK to "https://t.me/ProGamerBattle",
                KEY_TELIGRAM_LINK to "https://t.me/ProGamerBattle",
                KEY_MAINTENANCE_MODE to false,
                KEY_MAINTENANCE_MOD to false,
                KEY_ANNOUNCEMENT_MESSAGE to "",
                KEY_ANNOUNCEMENT_MASSEGE to "",
                KEY_MIN_VERSION to 1L,
                KEY_UPDATE_URL to "https://progamerbattle.com/download"
            )
            rc.setDefaultsAsync(defaults)

            // Initial fetch and activate
            fetchNow()

            // Real-time Remote Config updates listener (listens for changes published in Firebase Console)
            rc.addOnConfigUpdateListener(object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    Log.i(TAG, "Remote Config update received: ${configUpdate.updatedKeys}")
                    rc.activate().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            applyConfigValues()
                        }
                    }
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    Log.w(TAG, "Config update listener error: ${error.message}")
                }
            })
        } catch (e: Throwable) {
            Log.e(TAG, "Error setting up Remote Config", e)
        }
    }

    /**
     * Manually or programmatically triggers a fresh fetch and activation.
     */
    fun fetchNow() {
        val rc = remoteConfig ?: return
        _config.update { it.copy(isFetching = true, fetchErrorMessage = null) }

        rc.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val updated = task.result
                Log.i(TAG, "Remote Config fetch succeeded. Config activated: $updated")
                applyConfigValues()
            } else {
                val err = task.exception?.message ?: "Fetch failed"
                Log.w(TAG, "Remote Config fetch failed: $err")
                _config.update {
                    it.copy(
                        isFetching = false,
                        fetchErrorMessage = err
                    )
                }
            }
        }
    }

    /**
     * Admin Control: Updates remote config values locally and broadcasts immediately.
     */
    fun updateConfig(
        maintenanceMode: Boolean? = null,
        announcementMessage: String? = null,
        minVersionStr: String? = null,
        minVersionCode: Long? = null,
        updateUrl: String? = null,
        whatsappLink: String? = null,
        telegramLink: String? = null
    ) {
        _config.update { cur ->
            val newMinVerStr = minVersionStr ?: cur.minVersionStr
            val newMinVerLong = minVersionCode ?: newMinVerStr.toLongOrNull() ?: cur.minVersion
            val updated = cur.copy(
                isMaintenanceMode = maintenanceMode ?: cur.isMaintenanceMode,
                announcementMessage = announcementMessage ?: cur.announcementMessage,
                minVersion = newMinVerLong,
                minVersionStr = newMinVerStr,
                updateUrl = updateUrl ?: cur.updateUrl,
                whatsappLink = whatsappLink ?: cur.whatsappLink,
                telegramLink = telegramLink ?: cur.telegramLink,
                isUpdateRequired = isAppOutdated(newMinVerLong, newMinVerStr)
            )
            updated
        }
    }

    private fun applyConfigValues() {
        val rc = remoteConfig ?: return

        // 1. WhatsApp Link
        val whatsapp = getFirstNonBlankString(
            rc,
            KEY_WHATSAPP_LINK,
            KEY_WHATSAPP_URL,
            "whatsapp",
            "whatsappLink",
            fallback = "https://wa.me/message/X53ULJ34V2FZA1"
        )

        // 2. Telegram Link
        val telegram = getFirstNonBlankString(
            rc,
            KEY_TELEGRAM_LINK,
            KEY_TELIGRAM_LINK,
            KEY_TELEGRAM_URL,
            "telegram",
            "teligram",
            "telegramLink",
            "teligramLink",
            fallback = "https://t.me/ProGamerBattle"
        )

        // 3. Maintenance Mode
        val maintenance = getEffectiveBoolean(
            rc,
            KEY_MAINTENANCE_MODE,
            KEY_MAINTENANCE_MOD,
            "maintenanceMode",
            "maintenanceMod",
            "maintenance",
            "is_maintenance",
            fallback = false
        )

        // 4. Announcement Message
        val announcement = getFirstNonBlankString(
            rc,
            KEY_ANNOUNCEMENT_MESSAGE,
            KEY_ANNOUNCEMENT_MASSEGE,
            "announcementMessage",
            "announcementMassege",
            "announcement",
            "announcement_msg",
            fallback = ""
        )

        // 5. Min Version (checks version code or semver string)
        val minVerLong = getEffectiveLong(
            rc,
            KEY_MIN_VERSION,
            "min_version_code",
            "minVersion",
            "minVersionCode",
            fallback = 1L
        )

        val minVerStr = getFirstNonBlankString(
            rc,
            KEY_MIN_VERSION,
            "min_version_name",
            "minVersionStr",
            fallback = minVerLong.toString()
        )

        // 6. Update URL
        val updateUrl = getFirstNonBlankString(
            rc,
            KEY_UPDATE_URL,
            "updateUrl",
            "app_update_url",
            "download_url",
            fallback = "https://progamerbattle.com/download"
        )

        // Determine if Force Update is strictly required
        val isUpdateRequired = isAppOutdated(minVerLong, minVerStr)

        _config.update {
            it.copy(
                whatsappLink = whatsapp,
                telegramLink = telegram,
                isMaintenanceMode = maintenance,
                announcementMessage = announcement.trim(),
                minVersion = minVerLong,
                minVersionStr = minVerStr,
                updateUrl = updateUrl,
                isUpdateRequired = isUpdateRequired,
                lastFetchedAt = System.currentTimeMillis(),
                isFetching = false,
                fetchErrorMessage = null
            )
        }

        Log.i(TAG, "Applied Remote Config -> Maintenance: $maintenance, MinVer: $minVerStr, Announcement: '${announcement.take(30)}'")
    }

    private fun isAppOutdated(minVersionCode: Long, minVersionStr: String): Boolean {
        // Compare versionCode first
        if (minVersionCode > currentAppVersionCode) {
            return true
        }

        // Check if minVersionStr is in "1.1", "2.0" format
        try {
            val requiredParts = minVersionStr.split(".").mapNotNull { it.trim().toIntOrNull() }
            val currentParts = currentAppVersionName.split(".").mapNotNull { it.trim().toIntOrNull() }
            if (requiredParts.isNotEmpty() && currentParts.isNotEmpty()) {
                val maxLen = maxOf(requiredParts.size, currentParts.size)
                for (i in 0 until maxLen) {
                    val req = requiredParts.getOrElse(i) { 0 }
                    val cur = currentParts.getOrElse(i) { 0 }
                    if (req > cur) return true
                    if (req < cur) return false
                }
            }
        } catch (_: Exception) {}

        return false
    }

    private fun getFirstNonBlankString(rc: FirebaseRemoteConfig, vararg keys: String, fallback: String): String {
        for (key in keys) {
            try {
                val value = rc.getString(key).trim()
                if (value.isNotBlank()) {
                    return value
                }
            } catch (_: Exception) {}
        }
        return fallback
    }

    private fun getEffectiveBoolean(rc: FirebaseRemoteConfig, vararg keys: String, fallback: Boolean): Boolean {
        for (key in keys) {
            try {
                val boolVal = rc.getBoolean(key)
                if (boolVal) return true
                val strVal = rc.getString(key).trim().lowercase()
                if (strVal == "true" || strVal == "1" || strVal == "yes" || strVal == "enabled") {
                    return true
                }
            } catch (_: Exception) {}
        }
        return fallback
    }

    private fun getEffectiveLong(rc: FirebaseRemoteConfig, vararg keys: String, fallback: Long): Long {
        for (key in keys) {
            try {
                val longVal = rc.getLong(key)
                if (longVal > 0) return longVal
                val strVal = rc.getString(key).trim()
                val parsed = strVal.toLongOrNull()
                if (parsed != null && parsed > 0) return parsed
            } catch (_: Exception) {}
        }
        return fallback
    }

    companion object {
        private const val TAG = "RemoteConfigManager"

        // Canonical Remote Config Keys (supports both standard and user spellings)
        const val KEY_WHATSAPP_LINK = "whatsapp_link"
        const val KEY_WHATSAPP_URL = "whatsapp_url"
        const val KEY_TELEGRAM_LINK = "telegram_link"
        const val KEY_TELIGRAM_LINK = "teligram_link"
        const val KEY_TELEGRAM_URL = "telegram_url"
        const val KEY_MAINTENANCE_MODE = "maintenance_mode"
        const val KEY_MAINTENANCE_MOD = "maintenance_mod"
        const val KEY_ANNOUNCEMENT_MESSAGE = "announcement_message"
        const val KEY_ANNOUNCEMENT_MASSEGE = "announcement_massege"
        const val KEY_MIN_VERSION = "min_version"
        const val KEY_UPDATE_URL = "update_url"

        @Volatile
        private var instance: RemoteConfigManager? = null

        fun getInstance(context: Context? = null): RemoteConfigManager {
            return instance ?: synchronized(this) {
                instance ?: RemoteConfigManager(context?.applicationContext).also { instance = it }
            }
        }
    }
}
