package com.example.tareas.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tareas")
data class Tarea(
    // Clave primaria autogenerada para Room
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "descripcion")
    val descripcion: String = "",

    @ColumnInfo(name = "estado_completado")
    val estadoCompletado: Boolean = false,

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Long = System.currentTimeMillis(),

    // Bandera para sincronizacion posterior en la nube
    @ColumnInfo(name = "sincronizado")
    val sincronizado: Boolean = false,

    @ColumnInfo(name = "prioridad")
    val prioridad: String = "MEDIA",

    @ColumnInfo(name = "categoria")
    val categoria: String = "Universidad",

    @ColumnInfo(name = "fecha_limite")
    val fechaLimite: Long? = null
)
