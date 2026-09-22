package com.example.tareas.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tareas.data.Tarea
import com.example.tareas.data.TareaRepository
import com.example.tareas.ui.theme.SyncOkColor
import com.example.tareas.ui.theme.SyncPendienteColor
import kotlinx.coroutines.launch

enum class FiltroEstado {
    TODAS,
    PENDIENTES,
    COMPLETADAS,
    OFFLINE_PENDIENTE_SYNC
}

/**
 * Pantalla principal de la lista de tareas.
 * Implementada por Persona D conectándose directamente al TareaRepository de Persona C.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaTareasScreen(
    repository: TareaRepository,
    modifier: Modifier = Modifier
) {
    // Observación reactiva de la fuente de datos Room mediante Flow
    val todasLasTareas by repository.todasLasTareas.collectAsStateWithLifecycle(initialValue = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var filtroEstado by remember { mutableStateOf(FiltroEstado.TODAS) }
    var categoriaSeleccionada by remember { mutableStateOf("Todas") }
    var busquedaQuery by remember { mutableStateOf("") }
    var busquedaVisible by remember { mutableStateOf(false) }
    var menuOpcionesAbierto by remember { mutableStateOf(false) }

    var tareaParaEditar by remember { mutableStateOf<Tarea?>(null) }
    var mostrarDialogoAgregarEditar by remember { mutableStateOf(false) }

    // Categorías dinámicas derivadas de los registros en Room
    val categoriasDisponibles = remember(todasLasTareas) {
        val base = listOf("Todas", "Universidad", "Trabajo", "Proyectos", "Personal", "Urgente")
        (base + todasLasTareas.map { it.categoria }).distinct()
    }

    // Filtrado en memoria para respuesta instantánea de la UI
    val tareasFiltradas = remember(todasLasTareas, filtroEstado, categoriaSeleccionada, busquedaQuery) {
        todasLasTareas.filter { tarea ->
            val coincideEstado = when (filtroEstado) {
                FiltroEstado.TODAS -> true
                FiltroEstado.PENDIENTES -> !tarea.estadoCompletado
                FiltroEstado.COMPLETADAS -> tarea.estadoCompletado
                FiltroEstado.OFFLINE_PENDIENTE_SYNC -> !tarea.sincronizado
            }

            val coincideCategoria = if (categoriaSeleccionada == "Todas") {
                true
            } else {
                tarea.categoria.equals(categoriaSeleccionada, ignoreCase = true)
            }

            val coincideBusqueda = if (busquedaQuery.isBlank()) {
                true
            } else {
                tarea.titulo.contains(busquedaQuery, ignoreCase = true) ||
                        tarea.descripcion.contains(busquedaQuery, ignoreCase = true)
            }

            coincideEstado && coincideCategoria && coincideBusqueda
        }
    }

    val totalTareas = todasLasTareas.size
    val tareasCompletadas = todasLasTareas.count { it.estadoCompletado }
    val tareasSinSincronizar = todasLasTareas.count { !it.sincronizado }
    val ratioProgreso = if (totalTareas == 0) 0f else tareasCompletadas.toFloat() / totalTareas

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tareas Offline-First",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Punto 2: Room SQLite",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            busquedaVisible = !busquedaVisible
                            if (!busquedaVisible) busquedaQuery = ""
                        },
                        modifier = Modifier.testTag("btn_toggle_busqueda")
                    ) {
                        Icon(
                            imageVector = if (busquedaVisible) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = "Buscar tareas"
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuOpcionesAbierto = true },
                            modifier = Modifier.testTag("btn_menu_opciones")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones"
                            )
                        }

                        DropdownMenu(
                            expanded = menuOpcionesAbierto,
                            onDismissRequest = { menuOpcionesAbierto = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = SyncOkColor
                                    )
                                },
                                text = { Text("Simular Sincronización (Punto 5)") },
                                onClick = {
                                    menuOpcionesAbierto = false
                                    scope.launch {
                                        val noSync = repository.getNoSincronizadas()
                                        noSync.forEach { repository.marcarSincronizada(it.id) }
                                        snackbarHostState.showSnackbar("${noSync.size} tareas sincronizadas con éxito")
                                    }
                                }
                            )

                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                text = { Text("Eliminar tareas completadas") },
                                onClick = {
                                    menuOpcionesAbierto = false
                                    scope.launch {
                                        repository.eliminarCompletadas()
                                        snackbarHostState.showSnackbar("Tareas completadas eliminadas de Room")
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    tareaParaEditar = null
                    mostrarDialogoAgregarEditar = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("fab_agregar_tarea")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nueva tarea",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Barra de búsqueda con animación suave
            AnimatedVisibility(
                visible = busquedaVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedTextField(
                    value = busquedaQuery,
                    onValueChange = { busquedaQuery = it },
                    placeholder = { Text("Buscar en título o descripción...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("input_busqueda_tiempo_real"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (busquedaQuery.isNotBlank()) {
                            IconButton(onClick = { busquedaQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    }
                )
            }

            // Tarjeta de Métricas y Estado Offline-First
            if (totalTareas > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Progreso del Taller",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "$tareasCompletadas de $totalTareas tareas finalizadas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Text(
                                text = "${(ratioProgreso * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { ratioProgreso },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Resumen de estado de sincronización local
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Almacenamiento: SQLite / Room",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )

                            if (tareasSinSincronizar > 0) {
                                Text(
                                    text = "$tareasSinSincronizar pendientes de sync",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SyncPendienteColor
                                )
                            } else {
                                Text(
                                    text = "Todas sincronizadas",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SyncOkColor
                                )
                            }
                        }
                    }
                }
            }

            // Filtros de Estado
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroEstado.entries.forEach { estado ->
                    val seleccionado = filtroEstado == estado
                    FilterChip(
                        selected = seleccionado,
                        onClick = { filtroEstado = estado },
                        label = {
                            Text(
                                text = when (estado) {
                                    FiltroEstado.TODAS -> "Todas ($totalTareas)"
                                    FiltroEstado.PENDIENTES -> "Pendientes (${totalTareas - tareasCompletadas})"
                                    FiltroEstado.COMPLETADAS -> "Completadas ($tareasCompletadas)"
                                    FiltroEstado.OFFLINE_PENDIENTE_SYNC -> "Offline ($tareasSinSincronizar)"
                                },
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("filtro_estado_${estado.name.lowercase()}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Filtros de Categorías
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoriasDisponibles.forEach { cat ->
                    val seleccionado = categoriaSeleccionada.equals(cat, ignoreCase = true)
                    FilterChip(
                        selected = seleccionado,
                        onClick = { categoriaSeleccionada = cat },
                        label = {
                            Text(
                                text = cat,
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("filtro_categoria_${cat.lowercase()}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            // Lista de Tareas o Estado Vacío
            if (tareasFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (busquedaQuery.isNotBlank()) "No hay resultados para la búsqueda"
                            else if (totalTareas == 0) "¡Aún no hay tareas registradas!"
                            else "No hay tareas en esta categoría/filtro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (busquedaQuery.isNotBlank()) "Verifica el término ingresado o limpia el buscador"
                            else "Pulsa el botón + para registrar una tarea persistida en Room",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = tareasFiltradas,
                        key = { it.id }
                    ) { tarea ->
                        TareaItem(
                            tarea = tarea,
                            onToggleCompletado = {
                                scope.launch {
                                    // Al modificar sin conexión, sincronizado pasa a false
                                    repository.update(
                                        tarea.copy(
                                            estadoCompletado = !tarea.estadoCompletado,
                                            sincronizado = false
                                        )
                                    )
                                }
                            },
                            onEditar = {
                                tareaParaEditar = tarea
                                mostrarDialogoAgregarEditar = true
                            },
                            onEliminar = {
                                scope.launch {
                                    repository.delete(tarea)
                                    snackbarHostState.showSnackbar("Tarea eliminada de Room")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo Modal para Agregar o Modificar Tarea
    if (mostrarDialogoAgregarEditar) {
        AgregarEditarTareaScreen(
            tareaAEditar = tareaParaEditar,
            onGuardar = { titulo, descripcion, categoria, prioridad, fechaLimite ->
                scope.launch {
                    if (tareaParaEditar == null) {
                        // Inserción de nueva tarea (Punto 2: Offline-First con sincronizado = false)
                        val nuevaTarea = Tarea(
                            titulo = titulo,
                            descripcion = descripcion,
                            categoria = categoria,
                            prioridad = prioridad,
                            fechaLimite = fechaLimite,
                            sincronizado = false,
                            fechaCreacion = System.currentTimeMillis()
                        )
                        repository.insert(nuevaTarea)
                        snackbarHostState.showSnackbar("Tarea guardada en Room exitosamente")
                    } else {
                        // Actualización de tarea existente
                        val tareaActualizada = tareaParaEditar!!.copy(
                            titulo = titulo,
                            descripcion = descripcion,
                            categoria = categoria,
                            prioridad = prioridad,
                            fechaLimite = fechaLimite,
                            sincronizado = false // Marcar para posterior sincronización (Offline-First)
                        )
                        repository.update(tareaActualizada)
                        snackbarHostState.showSnackbar("Tarea actualizada en Room")
                    }
                }
                mostrarDialogoAgregarEditar = false
                tareaParaEditar = null
            },
            onDescartar = {
                mostrarDialogoAgregarEditar = false
                tareaParaEditar = null
            }
        )
    }
}
