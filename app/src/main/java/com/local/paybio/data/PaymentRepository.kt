package com.local.paybio.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/** Single entry point for persistence. Delegates to the encrypted Room DB. */
class PaymentRepository(context: Context) {

    private val dao = PayBioDatabase.get(context).paymentDao()

    fun observeAll(): Flow<List<PaymentMethod>> = dao.observeAll()
    suspend fun getById(id: Int): PaymentMethod? = dao.getById(id)
    suspend fun getFavorites(): List<PaymentMethod> = dao.getFavorites()
    suspend fun save(method: PaymentMethod): Long = dao.upsert(method)
    suspend fun delete(method: PaymentMethod) = dao.delete(method)
    suspend fun count(): Int = dao.count()
}
