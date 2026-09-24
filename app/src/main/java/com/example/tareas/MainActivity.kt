package com.example.tareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tareas.data.AppDatabase
import com.example.tareas.data.TareaRepository
import com.example.tareas.ui.ListaTareasScreen
import com.example.tareas.ui.theme.TareasTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TareaRepository(database.tareaDao())

        setContent {
            TareasTheme {
                ListaTareasScreen(repository = repository)
            }
        }
    }
}
