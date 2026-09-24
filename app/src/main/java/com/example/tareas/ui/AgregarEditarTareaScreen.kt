package com.example.tareas.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tareas.data.Tarea
import com.example.tareas.ui.theme.PrioridadAltaContainer
import com.example.tareas.ui.theme.PrioridadBajaContainer
import com.example.tareas.ui.theme.PrioridadMediaContainer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgregarEditarTareaScreen(
    tareaAEditar: Tarea? = null,
    onGuardar: (titulo: String, descripcion: String, categoria: String, prioridad: String, fechaLimite: Long?) -> Unit,
    onDescartar: () -> Unit
) {
    val context = LocalContext.current
    var titulo by remember { mutableStateOf(tareaAEditar?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(tareaAEditar?.descripcion ?: "") }
    var categoria by remember { mutableStateOf(tareaAEditar?.categoria ?: "Universidad") }
    var categoriaPersonalizada by remember { mutableStateOf("") }
    var modoPersonalizado by remember { mutableStateOf(false) }
    var prioridad by remember { mutableStateOf(tareaAEditar?.prioridad ?: "MEDIA") }
    var fechaLimite by remember { mutableStateOf(tareaAEditar?.fechaLimite) }
    var errorTitulo by remember { mutableStateOf(false) }

    val categoriasPredefinidas = listOf("Universidad", "Trabajo", "Proyectos", "Personal", "Urgente")

    val calendar = Calendar.getInstance()
    fechaLimite?.let { calendar.timeInMillis = it }

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val seleccionado = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            fechaLimite = seleccionado.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDescartar,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .testTag("dialogo_agregar_editar_tarea"),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (tareaAEditar == null) "Nueva Tarea" else "Modificar Tarea",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it
                        if (errorTitulo && it.isNotBlank()) errorTitulo = false
                    },
                    label = { Text("Título de la tarea *") },
                    placeholder = { Text("ej. Taller 3 Persistencia") },
                    isError = errorTitulo,
                    supportingText = {
                        if (errorTitulo) {
                            Text("El título no puede estar vacío", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_titulo_tarea"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción o notas") },
                    placeholder = { Text("Detalles sobre la entrega o requisitos...") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_descripcion_tarea"),
                    shape = RoundedCornerShape(12.dp)
                )

                Column {
                    Text(
                        text = "Prioridad",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ALTA", "MEDIA", "BAJA").forEach { nivel ->
                            val seleccionado = prioridad == nivel
                            FilterChip(
                                selected = seleccionado,
                                onClick = { prioridad = nivel },
                                label = {
                                    Text(
                                        text = nivel.lowercase().replaceFirstChar { it.uppercase() },
                                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_prioridad_${nivel.lowercase()}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (nivel) {
                                        "ALTA" -> PrioridadAltaContainer
                                        "BAJA" -> PrioridadBajaContainer
                                        else -> PrioridadMediaContainer
                                    }
                                )
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoriasPredefinidas.forEach { cat ->
                            FilterChip(
                                selected = !modoPersonalizado && categoria == cat,
                                onClick = {
                                    modoPersonalizado = false
                                    categoria = cat
                                },
                                label = { Text(cat) },
                                modifier = Modifier.testTag("chip_categoria_$cat")
                            )
                        }

                        FilterChip(
                            selected = modoPersonalizado,
                            onClick = { modoPersonalizado = true },
                            label = { Text("+ Otra") },
                            modifier = Modifier.testTag("chip_categoria_otra")
                        )
                    }

                    if (modoPersonalizado) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = categoriaPersonalizada,
                            onValueChange = { categoriaPersonalizada = it },
                            label = { Text("Nombre de nueva categoría") },
                            placeholder = { Text("ej. Investigación") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_categoria_personalizada"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Fecha de Entrega / Límite",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePicker.show() }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Seleccionar fecha",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = fechaLimite?.let {
                                        SimpleDateFormat("EEE, d MMM, yyyy", Locale.getDefault()).format(Date(it))
                                    } ?: "Sin fecha asignada",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (fechaLimite != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (fechaLimite != null) {
                                IconButton(
                                    onClick = { fechaLimite = null },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar fecha",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isBlank()) {
                        errorTitulo = true
                        return@Button
                    }
                    val catFinal = if (modoPersonalizado && categoriaPersonalizada.isNotBlank()) {
                        categoriaPersonalizada.trim()
                    } else {
                        categoria
                    }
                    onGuardar(titulo.trim(), descripcion.trim(), catFinal, prioridad, fechaLimite)
                },
                modifier = Modifier.testTag("btn_confirmar_guardar"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (tareaAEditar == null) "Agregar Tarea" else "Guardar Cambios")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDescartar,
                modifier = Modifier.testTag("btn_descartar"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar")
            }
        }
    )
}
