package com.example.tareas.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la entidad Tarea.
 * Desarrollado por Persona C (Capa de datos).
 * 
 * Expone las operaciones CRUD completas y consultas de soporte para Offline-First.
 */
@Dao
interface TareaDao {

    /**
     * Consulta reactiva de todas las tareas.
     * Al usar Flow, cualquier inserción, actualización o eliminación en la tabla
     * notificará automáticamente a la capa de UI.
     */
    @Query("SELECT * FROM tareas ORDER BY estado_completado ASC, fecha_creacion DESC")
    fun getAll(): Flow<List<Tarea>>

    /**
     * Obtiene una tarea específica por su ID.
     */
    @Query("SELECT * FROM tareas WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Tarea?

    /**
     * Retorna la lista de tareas pendientes de sincronización con la nube (Punto 5).
     * Fundamental para la arquitectura Offline-First.
     */
    @Query("SELECT * FROM tareas WHERE sincronizado = 0")
    suspend fun getNoSincronizadas(): List<Tarea>

    /**
     * Inserta una nueva tarea o reemplaza si ya existe el mismo ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tarea: Tarea): Long

    /**
     * Actualiza los datos de una tarea existente.
     */
    @Update
    suspend fun update(tarea: Tarea)

    /**
     * Elimina una tarea directamente pasando la entidad.
     */
    @Delete
    suspend fun delete(tarea: Tarea)

    /**
     * Elimina una tarea por su clave primaria.
     */
    @Query("DELETE FROM tareas WHERE id = :id")
    suspend fun deleteById(id: Int)

    /**
     * Actualiza el estado de sincronización de una tarea tras un push exitoso al servidor.
     */
    @Query("UPDATE tareas SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizada(id: Int)

    /**
     * Elimina todas las tareas marcadas como completadas.
     */
    @Query("DELETE FROM tareas WHERE estado_completado = 1")
    suspend fun eliminarCompletadas()
}
