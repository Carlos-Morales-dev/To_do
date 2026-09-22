package com.example.tareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tareas.data.AppDatabase
import com.example.tareas.data.TareaRepository
import com.example.tareas.ui.ListaTareasScreen
import com.example.tareas.ui.theme.TareasTheme

/**
 * Actividad principal de la App 2 (Punto 2 del Taller de Persistencia).
 * Conecta la inicialización de la base de datos Room (Persona C) con la interfaz de usuario (Persona D).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicialización de Room Database mediante patrón Singleton (Persona C)
        val database = AppDatabase.getDatabase(applicationContext)

        // Inicialización del Repositorio que provee las operaciones CRUD
        val repository = TareaRepository(database.tareaDao())

        // Montaje de la interfaz de usuario (Persona D)
        setContent {
            TareasTheme {
                ListaTareasScreen(repository = repository)
            }
        }
    }
}
