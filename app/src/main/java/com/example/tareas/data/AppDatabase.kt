package com.example.tareas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Clase principal de base de datos Room para la aplicación de tareas.
 * Implementada por Persona C (Capa de datos).
 * 
 * Gestiona la conexión física SQLite, transacciones y ciclo de vida de la BD.
 */
@Database(
    entities = [Tarea::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tareaDao(): TareaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retorna la instancia Singleton de AppDatabase.
         * Garantiza una única conexión SQLite abierta en toda la aplicación,
         * previniendo bloqueos y fugas de memoria.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tareas_offline_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
