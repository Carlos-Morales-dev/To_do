package com.example.tareas.data

import kotlinx.coroutines.flow.Flow

// Patron de diseno repositorio para separar la fuente de datos de la interfaz
class TareaRepository(private val tareaDao: TareaDao) {

    val todasLasTareas: Flow<List<Tarea>> = tareaDao.getAll()

    suspend fun insert(tarea: Tarea): Long {
        return tareaDao.insert(tarea)
    }

    suspend fun update(tarea: Tarea) {
        tareaDao.update(tarea)
    }

    suspend fun delete(tarea: Tarea) {
        tareaDao.delete(tarea)
    }

    suspend fun deleteById(id: Int) {
        tareaDao.deleteById(id)
    }

    suspend fun getById(id: Int): Tarea? {
        return tareaDao.getById(id)
    }

    suspend fun getNoSincronizadas(): List<Tarea> {
        return tareaDao.getNoSincronizadas()
    }

    suspend fun marcarSincronizada(id: Int) {
        tareaDao.marcarSincronizada(id)
    }

    suspend fun eliminarCompletadas() {
        tareaDao.eliminarCompletadas()
    }

    suspend fun deleteByCategoria(categoria: String) {
        tareaDao.deleteByCategoria(categoria)
    }
}
