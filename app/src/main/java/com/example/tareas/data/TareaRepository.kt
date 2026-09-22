package com.example.tareas.data

import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que implementa el patrón Repository para abstraer el origen de datos.
 * Construido por Persona C (Capa de datos) conforme al contrato de nombres del Kickoff.
 * 
 * Este es el punto de integración que Persona D utiliza para realizar el CRUD
 * sin conocer los detalles internos de Room ni las consultas SQL.
 */
class TareaRepository(private val tareaDao: TareaDao) {

    /**
     * Flujo reactivo de todas las tareas almacenadas en Room.
     */
    val todasLasTareas: Flow<List<Tarea>> = tareaDao.getAll()

    /**
     * Inserta una nueva tarea en la base de datos local.
     * Retorna el id asignado por SQLite.
     */
    suspend fun insert(tarea: Tarea): Long {
        return tareaDao.insert(tarea)
    }

    /**
     * Actualiza una tarea existente.
     */
    suspend fun update(tarea: Tarea) {
        tareaDao.update(tarea)
    }

    /**
     * Elimina una tarea de la base de datos.
     */
    suspend fun delete(tarea: Tarea) {
        tareaDao.delete(tarea)
    }

    /**
     * Elimina una tarea directamente por su ID numérico.
     */
    suspend fun deleteById(id: Int) {
        tareaDao.deleteById(id)
    }

    /**
     * Busca una tarea específica por su ID.
     */
    suspend fun getById(id: Int): Tarea? {
        return tareaDao.getById(id)
    }

    /**
     * Obtiene tareas pendientes de sincronización (para Punto 5 / Offline-First).
     */
    suspend fun getNoSincronizadas(): List<Tarea> {
        return tareaDao.getNoSincronizadas()
    }

    /**
     * Marca una tarea como sincronizada con éxito.
     */
    suspend fun marcarSincronizada(id: Int) {
        tareaDao.marcarSincronizada(id)
    }

    /**
     * Remueve en lote todas las tareas completadas.
     */
    suspend fun eliminarCompletadas() {
        tareaDao.eliminarCompletadas()
    }
}
