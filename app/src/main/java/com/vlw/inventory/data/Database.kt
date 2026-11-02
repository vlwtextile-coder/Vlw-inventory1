package com.vlwtextile.inventory.data

import androidx.room.*
import android.content.Context
import net.sqlcipher.database.SupportFactory
import net.sqlcipher.database.SQLiteDatabase

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val pinHash: String,
    val role: String
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val productId: String,
    val sku: String,
    val name: String,
    val barcode: String,
    val availableQuantity: Int,
    val defaultSellingPrice: Double,
    val minSellingPrice: Double,
    val purchasePrice: Double,
    val imageUri: String?
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val transactionId: String,
    val productId: String,
    val employeeId: String,
    val soldQuantity: Int,
    val sellingPricePerPiece: Double,
    val discount: Double,
    val totalSaleAmount: Double,
    val paymentMode: String,
    val timestamp: Long
)

@Entity(tableName = "employee_activity")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityId: String,
    val employeeId: String,
    val actionType: String,
    val productId: String,
    val details: String,
    val timestamp: Long
)

@Entity(tableName = "stock_alerts")
data class StockAlertEntity(
    @PrimaryKey val reportId: String,
    val productId: String,
    val employeeId: String,
    val status: String,
    val timestamp: Long
)

@Dao
interface InventoryDao {
    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE productId = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("UPDATE products SET availableQuantity = availableQuantity - :qty WHERE productId = :id AND availableQuantity >= :qty")
    suspend fun deductStock(id: String, qty: Int): Int

    @Insert
    suspend fun insertSale(sale: SaleEntity)

    @Insert
    suspend fun insertActivityLog(log: ActivityLogEntity)

    @Insert
    suspend fun insertStockAlert(alert: StockAlertEntity)

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    suspend fun getAllSales(): List<SaleEntity>
}

@Database(
    entities = [UserEntity::class, ProductEntity::class, SaleEntity::class, ActivityLogEntity::class, StockAlertEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, passphrase: ByteArray): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val factory = SupportFactory(passphrase)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vlw_inventory_secure.db"
                ).openHelperFactory(factory).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
