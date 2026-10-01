package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Entity(tableName = "sync_outbox")
data class SyncOutboxEntity(
    @PrimaryKey val mutationId: String = UUID.randomUUID().toString(),
    val entityType: String, // CLIENT, PET, APPOINTMENT, CONSULTATION, VACCINATION, DEWORMING, INVOICE, PAYMENT, INVENTORY, ENQUIRY
    val cloudId: String,
    val action: String, // CREATE, UPDATE, DELETE
    val payloadJson: String,
    val version: Long = 1L,
    val baseVersion: Long = 1L,
    val deviceId: String = "android_device",
    val isSeedData: Boolean = false, // Critical: Seed data is NEVER queued
    val status: String = "PENDING", // PENDING, IN_FLIGHT, FAILED, SYNCED
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long = 0L,
    val errorMessage: String? = null
)

@Dao
interface SyncOutboxDao {
    @Query("SELECT * FROM sync_outbox WHERE isSeedData = 0 AND status IN ('PENDING', 'FAILED') ORDER BY createdAt ASC")
    fun getPendingMutations(): Flow<List<SyncOutboxEntity>>

    @Query("SELECT * FROM sync_outbox WHERE isSeedData = 0 AND status = 'PENDING' ORDER BY createdAt ASC LIMIT :batchSize")
    suspend fun getPendingBatch(batchSize: Int = 20): List<SyncOutboxEntity>

    @Query("SELECT COUNT(*) FROM sync_outbox WHERE isSeedData = 0 AND status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMutation(mutation: SyncOutboxEntity)

    @Update
    suspend fun updateMutation(mutation: SyncOutboxEntity)

    @Query("DELETE FROM sync_outbox WHERE mutationId = :mutationId")
    suspend fun deleteMutation(mutationId: String)

    @Query("DELETE FROM sync_outbox WHERE status = 'SYNCED'")
    suspend fun purgeSynced()
}
