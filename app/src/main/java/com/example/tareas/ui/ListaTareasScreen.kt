package com.example.tareas.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tareas.data.Tarea
import com.example.tareas.data.TareaRepository
import com.example.tareas.ui.theme.SyncOkColor
import com.example.tareas.ui.theme.SyncOkContainer
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
 * Implementada con soporte responsivo para rotación de pantalla (Portrait y Landscape),
 * filtros desplegables horizontales, y acciones de sincronización y limpieza en la esquina inferior izquierda
 * con diálogo de confirmación para eliminar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaTareasScreen(
    repository: TareaRepository,
    modifier: Modifier = Modifier
) {
    val todasLasTareas by repository.todasLasTareas.collectAsStateWithLifecycle(initialValue = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var filtroEstado by remember { mutableStateOf(FiltroEstado.TODAS) }
    var categoriaSeleccionada by remember { mutableStateOf("Todas") }
    var busquedaQuery by remember { mutableStateOf("") }
    var busquedaVisible by remember { mutableStateOf(false) }

    var tareaParaEditar by remember { mutableStateOf<Tarea?>(null) }
    var mostrarDialogoAgregarEditar by remember { mutableStateOf(false) }
    var mostrarDialogoConfirmarEliminar by remember { mutableStateOf(false) }

    // Categorías dinámicas derivadas de los registros en Room
    val categoriasDisponibles = remember(todasLasTareas) {
        val base = listOf("Todas", "Universidad", "Trabajo", "Proyectos", "Personal", "Urgente")
        (base + todasLasTareas.map { it.categoria }).distinct()
    }

    // Filtrado en memoria
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

    // Lambda para sincronizar
    val ejecutarSincronizacion: () -> Unit = {
        scope.launch {
            val noSync = repository.getNoSincronizadas()
            noSync.forEach { repository.marcarSincronizada(it.id) }
            snackbarHostState.showSnackbar(
                if (noSync.isNotEmpty()) "${noSync.size} tareas sincronizadas con éxito"
                else "Todas las tareas ya están al día"
            )
        }
    }

    // Lambda para solicitar eliminación con confirmación previa
    val solicitarEliminarCompletadas: () -> Unit = {
        if (tareasCompletadas > 0) {
            mostrarDialogoConfirmarEliminar = true
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("No hay tareas completadas para eliminar")
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Lista de Tareas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Barra inferior en modo vertical (Portrait):
            // Acciones de Sincronizar y Eliminar Hechas en la ESQUINA INFERIOR IZQUIERDA
            // y Botón FAB en la ESQUINA INFERIOR DERECHA
            if (!isLandscape) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Esquina Inferior Izquierda: Sincronizar y Eliminar Hechas
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("contenedor_acciones_inferior_izq")
                        ) {
                            BotonSincronizar(
                                tareasSinSincronizar = tareasSinSincronizar,
                                onClick = ejecutarSincronizacion
                            )

                            BotonEliminarHechas(
                                tareasCompletadas = tareasCompletadas,
                                onClick = solicitarEliminarCompletadas
                            )
                        }

                        // Esquina Inferior Derecha: FAB Agregar Tarea
                        FloatingActionButton(
                            onClick = {
                                tareaParaEditar = null
                                mostrarDialogoAgregarEditar = true
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(52.dp)
                                .testTag("fab_agregar_tarea")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Nueva tarea",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        // Adaptación ergonómica según la orientación (Landscape vs Portrait)
        if (isLandscape) {
            // DISPOSICIÓN HORIZONTAL (Landscape): 2 Paneles
            // Panel Izquierdo: Buscador, Métricas compactas, Filtros desplegables y Acciones en esquina inferior izquierda
            // Panel Derecho: Lista de Tareas y FAB de Agregar
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Panel Izquierdo (Controlador y Métricas)
                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedVisibility(
                        visible = busquedaVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        OutlinedTextField(
                            value = busquedaQuery,
                            onValueChange = { busquedaQuery = it },
                            placeholder = { Text("Buscar tareas...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_busqueda_tiempo_real"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            trailingIcon = {
                                if (busquedaQuery.isNotBlank()) {
                                    IconButton(onClick = { busquedaQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                    }
                                }
                            }
                        )
                    }

                    CardMetricas(
                        totalTareas = totalTareas,
                        tareasCompletadas = tareasCompletadas,
                        tareasSinSincronizar = tareasSinSincronizar,
                        ratioProgreso = ratioProgreso,
                        compacto = true
                    )

                    // Filtros desplegables organizados horizontalmente
                    FilaFiltrosDesplegables(
                        filtroEstado = filtroEstado,
                        onFiltroEstadoChange = { filtroEstado = it },
                        totalTareas = totalTareas,
                        tareasCompletadas = tareasCompletadas,
                        tareasSinSincronizar = tareasSinSincronizar,
                        categoriaSeleccionada = categoriaSeleccionada,
                        onCategoriaChange = { categoriaSeleccionada = it },
                        categoriasDisponibles = categoriasDisponibles
                    )

                    Spacer(modifier = Modifier.weight(1f, fill = false))

                    // Acciones en la esquina inferior izquierda para Landscape
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .testTag("contenedor_acciones_landscape_izq"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Acciones Rápidas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BotonSincronizar(
                                tareasSinSincronizar = tareasSinSincronizar,
                                onClick = ejecutarSincronizacion,
                                modifier = Modifier.weight(1f)
                            )
                            BotonEliminarHechas(
                                tareasCompletadas = tareasCompletadas,
                                onClick = solicitarEliminarCompletadas,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Panel Derecho (Lista de Tareas con FAB flotante)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 8.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)
                ) {
                    ListaTareasContent(
                        tareasFiltradas = tareasFiltradas,
                        busquedaQuery = busquedaQuery,
                        totalTareas = totalTareas,
                        onToggleCompletado = { tarea ->
                            scope.launch {
                                // Al modificar sin conexión, la tarea pasa a requerir sync
                                repository.update(
                                    tarea.copy(
                                        estadoCompletado = !tarea.estadoCompletado,
                                        sincronizado = false
                                    )
                                )
                            }
                        },
                        onEditar = { tarea ->
                            tareaParaEditar = tarea
                            mostrarDialogoAgregarEditar = true
                        },
                        onEliminar = { tarea ->
                            scope.launch {
                                repository.delete(tarea)
                                snackbarHostState.showSnackbar("Tarea eliminada de Room")
                            }
                        },
                        contentPadding = PaddingValues(bottom = 76.dp)
                    )

                    // FAB en la esquina inferior derecha en Landscape
                    FloatingActionButton(
                        onClick = {
                            tareaParaEditar = null
                            mostrarDialogoAgregarEditar = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 8.dp)
                            .size(52.dp)
                            .testTag("fab_agregar_tarea")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nueva tarea",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        } else {
            // DISPOSICIÓN VERTICAL (Portrait estándar)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Barra de búsqueda desplegable
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
                    CardMetricas(
                        totalTareas = totalTareas,
                        tareasCompletadas = tareasCompletadas,
                        tareasSinSincronizar = tareasSinSincronizar,
                        ratioProgreso = ratioProgreso,
                        compacto = false
                    )
                }

                // Filtros desplegables organizados de forma horizontal
                FilaFiltrosDesplegables(
                    filtroEstado = filtroEstado,
                    onFiltroEstadoChange = { filtroEstado = it },
                    totalTareas = totalTareas,
                    tareasCompletadas = tareasCompletadas,
                    tareasSinSincronizar = tareasSinSincronizar,
                    categoriaSeleccionada = categoriaSeleccionada,
                    onCategoriaChange = { categoriaSeleccionada = it },
                    categoriasDisponibles = categoriasDisponibles
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Lista de Tareas
                ListaTareasContent(
                    tareasFiltradas = tareasFiltradas,
                    busquedaQuery = busquedaQuery,
                    totalTareas = totalTareas,
                    onToggleCompletado = { tarea ->
                        scope.launch {
                            repository.update(
                                tarea.copy(
                                    estadoCompletado = !tarea.estadoCompletado,
                                    sincronizado = false
                                )
                            )
                        }
                    },
                    onEditar = { tarea ->
                        tareaParaEditar = tarea
                        mostrarDialogoAgregarEditar = true
                    },
                    onEliminar = { tarea ->
                        scope.launch {
                            repository.delete(tarea)
                            snackbarHostState.showSnackbar("Tarea eliminada de Room")
                        }
                    },
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 16.dp
                    )
                )
            }
        }
    }

    // Diálogo de Confirmación para Eliminar Tareas Completadas
    if (mostrarDialogoConfirmarEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmarEliminar = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "¿Eliminar tareas hechas?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Se eliminarán definitivamente las $tareasCompletadas tareas marcadas como completadas. Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val cantidad = tareasCompletadas
                            repository.eliminarCompletadas()
                            snackbarHostState.showSnackbar("Se eliminaron $cantidad tareas completadas")
                        }
                        mostrarDialogoConfirmarEliminar = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_confirmar_eliminar_hechas")
                ) {
                    Text("Sí, eliminar ($tareasCompletadas)")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarDialogoConfirmarEliminar = false },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_cancelar_eliminar_hechas")
                ) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("dialogo_confirmar_eliminar")
        )
    }

    // Diálogo Modal para Agregar o Modificar Tarea
    if (mostrarDialogoAgregarEditar) {
        val tareaAEditarLocal = tareaParaEditar
        AgregarEditarTareaScreen(
            tareaAEditar = tareaAEditarLocal,
            onGuardar = { titulo, descripcion, categoria, prioridad, fechaLimite ->
                scope.launch {
                    if (tareaAEditarLocal == null) {
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
                        val tareaActualizada = tareaAEditarLocal.copy(
                            titulo = titulo,
                            descripcion = descripcion,
                            categoria = categoria,
                            prioridad = prioridad,
                            fechaLimite = fechaLimite,
                            sincronizado = false
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

/**
 * Fila horizontal que contiene los 2 desplegables de filtro:
 * - Filtro de Estado (Todas, Pendientes, Hechas, Pendientes Sync)
 * - Filtro de Categoría (Todas, Universidad, Trabajo, etc.)
 */
@Composable
fun FilaFiltrosDesplegables(
    filtroEstado: FiltroEstado,
    onFiltroEstadoChange: (FiltroEstado) -> Unit,
    totalTareas: Int,
    tareasCompletadas: Int,
    tareasSinSincronizar: Int,
    categoriaSeleccionada: String,
    onCategoriaChange: (String) -> Unit,
    categoriasDisponibles: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Desplegable de Estado
        val textoEstado = when (filtroEstado) {
            FiltroEstado.TODAS -> "Todas ($totalTareas)"
            FiltroEstado.PENDIENTES -> "Pendientes (${totalTareas - tareasCompletadas})"
            FiltroEstado.COMPLETADAS -> "Hechas ($tareasCompletadas)"
            FiltroEstado.OFFLINE_PENDIENTE_SYNC -> "Sync ($tareasSinSincronizar)"
        }

        MenuDesplegableFiltro(
            label = "Estado",
            valorSeleccionado = textoEstado,
            icono = Icons.Default.FilterList,
            opciones = listOf(
                FiltroEstado.TODAS to "Todas ($totalTareas)",
                FiltroEstado.PENDIENTES to "Pendientes (${totalTareas - tareasCompletadas})",
                FiltroEstado.COMPLETADAS to "Hechas ($tareasCompletadas)",
                FiltroEstado.OFFLINE_PENDIENTE_SYNC to "Offline / Sync ($tareasSinSincronizar)"
            ),
            onSeleccionar = onFiltroEstadoChange,
            modifier = Modifier.weight(1f),
            testTag = "desplegable_filtro_estado"
        )

        // Desplegable de Categoría
        MenuDesplegableFiltro(
            label = "Categoría",
            valorSeleccionado = categoriaSeleccionada,
            icono = Icons.Default.Category,
            opciones = categoriasDisponibles.map { it to it },
            onSeleccionar = onCategoriaChange,
            modifier = Modifier.weight(1f),
            testTag = "desplegable_filtro_categoria"
        )
    }
}

/**
 * Componente reutilizable para menús desplegables de selección rápida.
 */
@Composable
fun <T> MenuDesplegableFiltro(
    label: String,
    valorSeleccionado: String,
    icono: ImageVector,
    opciones: List<Pair<T, String>>,
    onSeleccionar: (T) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    var expandido by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
                .clickable { expandido = true },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            border = BorderStroke(
                1.dp,
                if (expandido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = valorSeleccionado,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = if (expandido) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Expandir menú",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false },
            modifier = Modifier.widthIn(min = 190.dp)
        ) {
            opciones.forEach { (item, texto) ->
                val esSeleccionado = texto == valorSeleccionado || texto.startsWith(valorSeleccionado)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = texto,
                            fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Normal,
                            color = if (esSeleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = if (esSeleccionado) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    onClick = {
                        onSeleccionar(item)
                        expandido = false
                    }
                )
            }
        }
    }
}

/**
 * Botón para Sincronizar en la esquina inferior izquierda.
 */
@Composable
fun BotonSincronizar(
    tareasSinSincronizar: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = SyncOkContainer,
            contentColor = SyncOkColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        modifier = modifier.testTag("btn_sincronizar_bottom_left")
    ) {
        Icon(
            imageVector = Icons.Default.CloudSync,
            contentDescription = "Sincronizar",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (tareasSinSincronizar > 0) "Sync ($tareasSinSincronizar)" else "Sync",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Botón para Eliminar tareas hechas en la esquina inferior izquierda.
 */
@Composable
fun BotonEliminarHechas(
    tareasCompletadas: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
            contentColor = MaterialTheme.colorScheme.error
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        modifier = modifier.testTag("btn_eliminar_hechas_bottom_left")
    ) {
        Icon(
            imageVector = Icons.Default.DeleteSweep,
            contentDescription = "Eliminar tareas hechas",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Limpiar ($tareasCompletadas)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Tarjeta de métricas con barra de progreso y estado Offline-First.
 */
@Composable
fun CardMetricas(
    totalTareas: Int,
    tareasCompletadas: Int,
    tareasSinSincronizar: Int,
    ratioProgreso: Float,
    compacto: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (compacto) 0.dp else 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Progreso del Taller",
                        style = if (compacto) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$tareasCompletadas de $totalTareas finalizadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Text(
                    text = "${(ratioProgreso * 100).toInt()}%",
                    style = if (compacto) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { ratioProgreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SQLite Local (Room)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                if (tareasSinSincronizar > 0) {
                    Text(
                        text = "$tareasSinSincronizar por sincronizar",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SyncPendienteColor
                    )
                } else {
                    Text(
                        text = "Todo sincronizado",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SyncOkColor
                    )
                }
            }
        }
    }
}

/**
 * Contenido de la lista de tareas o estado vacío si no hay coincidencias.
 */
@Composable
fun ListaTareasContent(
    tareasFiltradas: List<Tarea>,
    busquedaQuery: String,
    totalTareas: Int,
    onToggleCompletado: (Tarea) -> Unit,
    onEditar: (Tarea) -> Unit,
    onEliminar: (Tarea) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    if (tareasFiltradas.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (busquedaQuery.isNotBlank()) "No hay resultados para la búsqueda"
                    else if (totalTareas == 0) "¡Aún no hay tareas registradas!"
                    else "No hay tareas con estos filtros",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (busquedaQuery.isNotBlank()) "Verifica el término ingresado o limpia la búsqueda"
                    else "Pulsa el botón + para registrar una tarea persistida en Room",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = tareasFiltradas,
                key = { it.id }
            ) { tarea ->
                TareaItem(
                    tarea = tarea,
                    onToggleCompletado = { onToggleCompletado(tarea) },
                    onEditar = { onEditar(tarea) },
                    onEliminar = { onEliminar(tarea) }
                )
            }
        }
    }
}

