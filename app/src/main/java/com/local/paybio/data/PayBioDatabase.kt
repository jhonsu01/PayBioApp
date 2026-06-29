package com.local.paybio.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.local.paybio.util.DbKey
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [PaymentMethod::class], version = 2, exportSchema = false)
abstract class PayBioDatabase : RoomDatabase() {

    abstract fun paymentDao(): PaymentMethodDao

    companion object {
        const val DB_NAME = "paybio_data.db"

        // v1 -> v2: añade columnas para logo y nombre personalizado (sin borrar datos).
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE payment_methods ADD COLUMN logoImagePath TEXT")
                db.execSQL("ALTER TABLE payment_methods ADD COLUMN label TEXT")
            }
        }

        @Volatile
        private var INSTANCE: PayBioDatabase? = null

        fun get(context: Context): PayBioDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context.applicationContext).also { INSTANCE = it }
            }

        /** Closes the open DB and clears the singleton (used before restoring a backup). */
        fun closeAndReset() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }

        private fun build(context: Context): PayBioDatabase {
            // SQLCipher native libs are loaded once in PayBioApp.onCreate().
            val passphrase = SQLiteDatabase.getBytes(DbKey.getOrCreate(context).toCharArray())
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(context, PayBioDatabase::class.java, DB_NAME)
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
