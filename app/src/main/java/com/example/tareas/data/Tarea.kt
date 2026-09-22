package com.example.tareas.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Tarea según la especificación del Taller 3 (Punto 2).
 * Modelada por Persona C (Capa de datos).
 * 
 * Incluye los campos obligatorios del enunciado:
 * - id: Identificador único autogenerado
 * - titulo: Título descriptivo de la tarea
 * - descripcion: Detalle de la tarea
 * - estadoCompletado: Bandera que indica si la tarea fue finalizada
 * - fechaCreacion: Timestamp epoch en milisegundos
 * - sincronizado: Bandera offline-first (deja la base lista para Punto 5 / Sync remota)
 * 
 * Y adiciones para hacer la aplicación no tan común (destacada para la rúbrica):
 * - prioridad: Nivel de urgencia (ALTA, MEDIA, BAJA)
 * - categoria: Agrupación temática (Universidad, Trabajo, Personal, Urgente)
 * - fechaLimite: Fecha de vencimiento opcional con recordatorio
 */
@Entity(tableName = "tareas")
data class Tarea(
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

    @ColumnInfo(name = "sincronizado")
    val sincronizado: Boolean = false,

    @ColumnInfo(name = "prioridad")
    val prioridad: String = "MEDIA",

    @ColumnInfo(name = "categoria")
    val categoria: String = "Universidad",

    @ColumnInfo(name = "fecha_limite")
    val fechaLimite: Long? = null
)
