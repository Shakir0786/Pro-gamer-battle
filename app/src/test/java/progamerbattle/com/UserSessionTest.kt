package progamerbattle.com

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import progamerbattle.com.data.AuthRepository
import progamerbattle.com.data.UserSessionManager
import progamerbattle.com.model.User

class UserSessionTest {

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var sessionManager: UserSessionManager

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
        sessionManager = UserSessionManager(fakePrefs)
    }

    @Test
    fun testNewUserHasNoActiveSession() {
        assertFalse(sessionManager.hasActiveSession())
        assertNull(sessionManager.getUserSession())

        // Initial launch for a new user starts with null currentUser, directing to registration/login
        val authRepo = AuthRepository(sessionManager)
        assertNull(authRepo.currentUser.value)
    }

    @Test
    fun testSaveAndRestoreUserSession() {
        val testUser = User(
            uid = "user_9988776655",
            fullName = "Karan Verma",
            mobileNumber = "9988776655",
            ffIgn = "KaranPro",
            ffUid = "99887711",
            secretPin = "654321",
            depositBalance = 250.0,
            winningBalance = 800.0,
            bonusBalance = 150.0,
            totalKills = 14,
            matchesWon = 3,
            matchesPlayed = 8
        )

        sessionManager.saveUserSession(testUser)

        assertTrue(sessionManager.hasActiveSession())
        val restored = sessionManager.getUserSession()
        assertNotNull(restored)
        assertEquals("user_9988776655", restored!!.uid)
        assertEquals("Karan Verma", restored.fullName)
        assertEquals("9988776655", restored.mobileNumber)
        assertEquals("KaranPro", restored.ffIgn)
        assertEquals("99887711", restored.ffUid)
        assertEquals("654321", restored.secretPin)
        assertEquals(250.0, restored.depositBalance, 0.01)
        assertEquals(800.0, restored.winningBalance, 0.01)
        assertEquals(150.0, restored.bonusBalance, 0.01)
        assertEquals(1200.0, restored.walletBalance, 0.01)
        assertEquals(14, restored.totalKills)
        assertEquals(3, restored.matchesWon)
        assertEquals(8, restored.matchesPlayed)
    }

    @Test
    fun testSubsequentLaunchDetectsActiveSessionAndBypassesLogin() {
        val testUser = User(
            uid = "user_9123456780",
            fullName = "Rohit Sharma",
            mobileNumber = "9123456780",
            ffIgn = "HitmanFF",
            ffUid = "45454545",
            secretPin = "112233",
            depositBalance = 100.0,
            winningBalance = 500.0,
            bonusBalance = 150.0
        )

        // Save session on first login/registration
        sessionManager.saveUserSession(testUser)

        // Simulate subsequent app launch
        val subsequentAppLaunchRepo = AuthRepository(sessionManager)

        // Active session must be detected immediately without user interaction
        assertNotNull(subsequentAppLaunchRepo.currentUser.value)
        assertEquals("9123456780", subsequentAppLaunchRepo.currentUser.value?.mobileNumber)
        assertEquals("HitmanFF", subsequentAppLaunchRepo.currentUser.value?.ffIgn)
        assertEquals(750.0, subsequentAppLaunchRepo.currentUser.value?.walletBalance ?: 0.0, 0.01)
    }

    @Test
    fun testSignOutClearsPersistentSession() {
        val testUser = User(
            uid = "user_9876543210",
            fullName = "Aman Sharma",
            mobileNumber = "9876543210",
            ffIgn = "ShadowStriker",
            ffUid = "849201948",
            secretPin = "123456",
            depositBalance = 100.0,
            winningBalance = 650.0,
            bonusBalance = 150.0
        )

        sessionManager.saveUserSession(testUser)
        assertTrue(sessionManager.hasActiveSession())

        val authRepo = AuthRepository(sessionManager)
        assertEquals("9876543210", authRepo.currentUser.value?.mobileNumber)

        // User signs out
        authRepo.signOut()

        // Persistent session must be erased
        assertNull(authRepo.currentUser.value)
        assertFalse(sessionManager.hasActiveSession())
        assertNull(sessionManager.getUserSession())

        // Re-launching app now presents registration/login screen
        val freshLaunchRepo = AuthRepository(sessionManager)
        assertNull(freshLaunchRepo.currentUser.value)
    }

    @Test
    fun testBalanceUpdateSyncsToSession() {
        val testUser = User(
            uid = "user_9998887770",
            fullName = "Viper",
            mobileNumber = "9998887770",
            ffIgn = "ViperGod",
            ffUid = "11223344",
            secretPin = "999000",
            depositBalance = 50.0,
            winningBalance = 200.0,
            bonusBalance = 150.0
        )

        sessionManager.saveUserSession(testUser)
        val authRepo = AuthRepository(sessionManager)

        // User adds a deposit of ₹100
        authRepo.addDeposit(100.0)
        assertEquals(150.0, authRepo.currentUser.value?.depositBalance ?: 0.0, 0.01)

        // Restoring session on next launch has updated deposit
        val newLaunchRepo = AuthRepository(sessionManager)
        assertEquals(150.0, newLaunchRepo.currentUser.value?.depositBalance ?: 0.0, 0.01)
    }

    @Test
    fun testReferralCodeAndEarningsPersistence() {
        val testUser = User(
            uid = "user_9811223344",
            fullName = "Sahil Khan",
            mobileNumber = "9811223344",
            ffIgn = "SniperKing",
            ffUid = "55667788",
            secretPin = "654321",
            depositBalance = 0.0,
            winningBalance = 0.0,
            bonusBalance = 10.0,
            referralCode = "PGBSNIP3344",
            referredBy = "9800000001",
            totalReferrals = 3,
            referralEarnings = 30.0
        )

        sessionManager.saveUserSession(testUser)

        val restored = sessionManager.getUserSession()
        assertNotNull(restored)
        assertEquals("PGBSNIP3344", restored!!.referralCode)
        assertEquals("9800000001", restored.referredBy)
        assertEquals(3, restored.totalReferrals)
        assertEquals(30.0, restored.referralEarnings, 0.01)
        assertEquals(10.0, restored.bonusBalance, 0.01)
        assertEquals(10.0, restored.walletBalance, 0.01)
    }
}

/**
 * Lightweight, deterministic in-memory SharedPreferences for JVM tests.
 */
class FakeSharedPreferences : SharedPreferences {

    private val data = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = data

    override fun getString(key: String?, defValue: String?): String? = (data[key] as? String) ?: defValue

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        (data[key] as? Set<String>)?.toMutableSet() ?: defValues

    override fun getInt(key: String?, defValue: Int): Int = (data[key] as? Int) ?: defValue

    override fun getLong(key: String?, defValue: Long): Long = (data[key] as? Long) ?: defValue

    override fun getFloat(key: String?, defValue: Float): Float = (data[key] as? Float) ?: defValue

    override fun getBoolean(key: String?, defValue: Boolean): Boolean = (data[key] as? Boolean) ?: defValue

    override fun contains(key: String?): Boolean = data.containsKey(key)

    override fun edit(): SharedPreferences.Editor = FakeEditor(data)

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    class FakeEditor(private val target: MutableMap<String, Any?>) : SharedPreferences.Editor {

        private val changes = mutableMapOf<String, Any?>()
        private var clearAll = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            key?.let { changes[it] = value }
            return this
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
            key?.let { changes[it] = values }
            return this
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            key?.let { changes[it] = value }
            return this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            key?.let { changes[it] = value }
            return this
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            key?.let { changes[it] = value }
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            key?.let { changes[it] = value }
            return this
        }

        override fun remove(key: String?): SharedPreferences.Editor {
            key?.let { changes.remove(it); target.remove(it) }
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            clearAll = true
            return this
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (clearAll) {
                target.clear()
            }
            target.putAll(changes)
        }
    }
}
