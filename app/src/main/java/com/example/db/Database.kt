package com.example.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Хеширование паролей (SHA-256).
 *
 * Поле [UserAccount.password] ранее хранилось открытым текстом и нигде
 * не проверялось (вход идёт через Firebase Auth), но утекало через
 * резервные копии / adb. Теперь хранится только SHA-256 хеш.
 */
object PasswordHasher {
    private const val ALGORITHM = "SHA-256"
    private const val FIXED_SALT = "block_tetris_local_v1"

    fun hash(raw: String): String {
        return try {
            val md = java.security.MessageDigest.getInstance(ALGORITHM)
            val digest = md.digest((raw + FIXED_SALT).toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /** Проверка пароля против сохранённого хеша. */
    fun verify(raw: String, storedHash: String): Boolean {
        if (storedHash.isEmpty()) return false
        return hash(raw) == storedHash
    }
}

@Entity(tableName = "high_scores")
data class HighScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerName: String,
    val score: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val hasGradient: Boolean = false
) {
    @androidx.room.Ignore
    var customTag: String = ""
}

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val username: String,
    // Хранится только SHA-256 хеш (см. PasswordHasher). Не plaintext.
    val password: String = PasswordHasher.hash("1111333322"),
    val avatarColor: String = "indigo",
    val onlineTier: String = "BRONZE",
    val credits: Int = 750,
    val creationTime: Long = System.currentTimeMillis(),
    val hasGradient: Boolean = false,
    val bonusXp: Int = 0
)

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC, timestamp DESC LIMIT 50")
    fun getTopScores(): Flow<List<HighScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: HighScore)

    @Query("DELETE FROM high_scores")
    suspend fun deleteAllScores()
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY creationTime DESC")
    fun getAllAccounts(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts WHERE username = :username LIMIT 1")
    suspend fun getAccount(username: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserAccount)

    @Query("DELETE FROM user_accounts WHERE username = :username")
    suspend fun deleteAccount(username: String)

    @Query("DELETE FROM user_accounts WHERE username != 'FsFq'")
    suspend fun deleteAllNonAdminAccounts()
}

@Database(entities = [HighScore::class, UserAccount::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun highScoreDao(): HighScoreDao
    abstract fun userAccountDao(): UserAccountDao
}

class ScoreRepository(private val highScoreDao: HighScoreDao) {
    val topScores: Flow<List<HighScore>> = highScoreDao.getTopScores()
    suspend fun insert(score: HighScore) = highScoreDao.insertScore(score)
    suspend fun clearAll() = highScoreDao.deleteAllScores()
}

class AccountRepository(private val userAccountDao: UserAccountDao) {
    val allAccounts: Flow<List<UserAccount>> = userAccountDao.getAllAccounts()
    suspend fun insert(account: UserAccount) = userAccountDao.insertAccount(account)
    suspend fun delete(username: String) = userAccountDao.deleteAccount(username)
    suspend fun getAccount(username: String): UserAccount? = userAccountDao.getAccount(username)
    suspend fun clearAllNonAdmin() = userAccountDao.deleteAllNonAdminAccounts()
}

