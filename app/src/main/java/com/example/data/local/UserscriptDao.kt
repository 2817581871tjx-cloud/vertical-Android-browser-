package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserscriptDao {
    @Query("SELECT * FROM userscripts ORDER BY isBuiltIn DESC, createdAt DESC")
    fun getAllScripts(): Flow<List<UserscriptEntity>>

    @Query("SELECT * FROM userscripts WHERE isEnabled = 1")
    fun getEnabledScripts(): Flow<List<UserscriptEntity>>

    @Query("SELECT * FROM userscripts WHERE isEnabled = 1")
    suspend fun getEnabledScriptsDirect(): List<UserscriptEntity>

    @Query("SELECT * FROM userscripts WHERE id = :id LIMIT 1")
    suspend fun getScriptById(id: Long): UserscriptEntity?

    @Query("SELECT COUNT(*) FROM userscripts")
    suspend fun getScriptCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: UserscriptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScripts(scripts: List<UserscriptEntity>)

    @Update
    suspend fun updateScript(script: UserscriptEntity)

    @Delete
    suspend fun deleteScript(script: UserscriptEntity)

    @Query("UPDATE userscripts SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateScriptEnabled(id: Long, isEnabled: Boolean)

    @Query("DELETE FROM userscripts WHERE id = :id")
    suspend fun deleteScriptById(id: Long)
}
