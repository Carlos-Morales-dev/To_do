package com.example.tareas.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tareas.data.Tarea
import com.example.tareas.ui.theme.PrioridadAltaColor
import com.example.tareas.ui.theme.PrioridadAltaContainer
import com.example.tareas.ui.theme.PrioridadBajaColor
import com.example.tareas.ui.theme.PrioridadBajaContainer
import com.example.tareas.ui.theme.PrioridadMediaColor
import com.example.tareas.ui.theme.PrioridadMediaContainer
import com.example.tareas.ui.theme.SyncOkColor
import com.example.tareas.ui.theme.SyncOkContainer
import com.example.tareas.ui.theme.SyncPendienteColor
import com.example.tareas.ui.theme.SyncPendienteContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TareaItem(
    tarea: Tarea,
    onToggleCompletado: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (tarea.estadoCompletado) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300),
        label = "animacionFondoTarea"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tarea_item_${tarea.id}")
            .clickable { onToggleCompletado() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(
            1.dp,
            if (tarea.estadoCompletado) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (tarea.estadoCompletado) 0.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = tarea.estadoCompletado,
                    onCheckedChange = { onToggleCompletado() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("checkbox_tarea_${tarea.id}"),
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = tarea.titulo,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (tarea.estadoCompletado) FontWeight.Normal else FontWeight.SemiBold,
                            textDecoration = if (tarea.estadoCompletado) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (tarea.estadoCompletado) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (tarea.descripcion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tarea.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (tarea.estadoCompletado) 0.5f else 0.85f
                            ),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditar,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_editar_tarea_${tarea.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar tarea",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onEliminar,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_eliminar_tarea_${tarea.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar tarea",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BadgePrioridad(prioridad = tarea.prioridad)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = tarea.categoria,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                if (tarea.sincronizado) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SyncOkContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = SyncOkColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sincronizado",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = SyncOkColor
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SyncPendienteContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = SyncPendienteColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Local (Offline)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = SyncPendienteColor
                            )
                        }
                    }
                }

                tarea.fechaLimite?.let { limite ->
                    val fechaFormateada = SimpleDateFormat("d MMM, yyyy", Locale.getDefault()).format(Date(limite))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Vence: $fechaFormateada",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgePrioridad(prioridad: String) {
    val (fondo, textoColor, etiqueta) = when (prioridad.uppercase()) {
        "ALTA" -> Triple(PrioridadAltaContainer, PrioridadAltaColor, "Alta")
        "BAJA" -> Triple(PrioridadBajaContainer, PrioridadBajaColor, "Baja")
        else -> Triple(PrioridadMediaContainer, PrioridadMediaColor, "Media")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = fondo
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textoColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = textoColor
            )
        }
    }
}
