package ir.dakhlokharj.app

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tx")
data class Tx(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val isExpense: Boolean,
    val category: String,
    val note: String,
    val date: Long
)

@Entity(tableName = "installment")
data class Installment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Long,
    val due: Long,
    val paid: Boolean = false
)

@Entity(tableName = "goal")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val target: Long,
    val saved: Long = 0
)

@Dao
interface AppDao {
    @Query("SELECT * FROM tx ORDER BY date DESC, id DESC")
    fun txs(): Flow<List<Tx>>
    @Insert suspend fun insertTx(t: Tx)
    @Delete suspend fun deleteTx(t: Tx)

    @Query("SELECT * FROM installment ORDER BY due ASC")
    fun installments(): Flow<List<Installment>>
    @Insert suspend fun insertInstallment(i: Installment)
    @Update suspend fun updateInstallment(i: Installment)
    @Delete suspend fun deleteInstallment(i: Installment)

    @Query("SELECT * FROM goal ORDER BY id DESC")
    fun goals(): Flow<List<Goal>>
    @Insert suspend fun insertGoal(g: Goal)
    @Update suspend fun updateGoal(g: Goal)
    @Delete suspend fun deleteGoal(g: Goal)
}

@Database(entities = [Tx::class, Installment::class, Goal::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile private var inst: AppDb? = null
        fun get(ctx: Context): AppDb = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(ctx.applicationContext, AppDb::class.java, "app.db")
                .build().also { inst = it }
        }
    }
}
