package com.local.paybio.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {

    @Query("SELECT * FROM payment_methods ORDER BY isFavorite DESC, createdAt DESC")
    fun observeAll(): Flow<List<PaymentMethod>>

    @Query("SELECT * FROM payment_methods WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): PaymentMethod?

    @Query("SELECT * FROM payment_methods WHERE isFavorite = 1 ORDER BY createdAt DESC")
    suspend fun getFavorites(): List<PaymentMethod>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(method: PaymentMethod): Long

    @Update
    suspend fun update(method: PaymentMethod)

    @Delete
    suspend fun delete(method: PaymentMethod)

    @Query("SELECT COUNT(*) FROM payment_methods")
    suspend fun count(): Int
}
