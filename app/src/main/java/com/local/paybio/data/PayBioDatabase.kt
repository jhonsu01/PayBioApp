package com.local.paybio.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.local.paybio.util.DbKey
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [PaymentMethod::class], version = 1, exportSchema = false)
abstract class PayBioDatabase : RoomDatabase() {

    abstract fun paymentDao(): PaymentMethodDao

    companion object {
        const val DB_NAME = "paybio_data.db"

        @Volatile
        private var INSTANCE: PayBioDatabase? = null

        fun get(context: Context): PayBioDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context.applicationContext).also { INSTANCE = it }
            }

        private fun build(context: Context): PayBioDatabase {
            // SQLCipher native libs are loaded once in PayBioApp.onCreate().
            val passphrase = SQLiteDatabase.getBytes(DbKey.getOrCreate(context).toCharArray())
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(context, PayBioDatabase::class.java, DB_NAME)
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
