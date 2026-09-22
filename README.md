# Taller 3 — Persistencia en Android: Punto 2 (Room Offline-First)

**Universidad de Nariño — Ingeniería de Sistemas**  
**Desarrollo de Aplicaciones Móviles**  
**Docente:** MSc. Martha Nubia Carrillo Obando  

---

## 📌 Enfoque del Proyecto (App 2: Almacenamiento Local con Room)

Esta aplicación implementa una solución completa de gestión de tareas (**To-Do List**) con arquitectura **Offline-First**, utilizando **Android Room Persistence Library** sobre SQLite.

### 👥 Distribución del Trabajo: Foco en la Persona C

Siguiendo el contrato de responsabilidades definido para el equipo:

#### **Persona C — Capa de Datos (Backend Local):**
1. **Entidad `Tarea.kt` (`@Entity(tableName = "tareas")`)**:
   - `id: Int` (Clave primaria autoincremental `@PrimaryKey(autoGenerate = true)`).
   - `titulo: String`: Título obligatorio de la tarea.
   - `descripcion: String`: Detalle opcional de la tarea.
   - `estadoCompletado: Boolean`: Estado booleano de realización.
   - `fechaCreacion: Long`: Marca de tiempo epoch en milisegundos.
   - `sincronizado: Boolean`: Bandera Offline-First para identificar registros creados/editados sin conexión a la red (deja la base lista para sincronización remota en el **Punto 5**).
   - *Adiciones diferenciadoras:* `prioridad` (ALTA, MEDIA, BAJA), `categoria` (Universidad, Trabajo, Proyectos, etc.) y `fechaLimite: Long?`.

2. **DAO `TareaDao.kt` (`@Dao`)**:
   - `getAll(): Flow<List<Tarea>>`: Consulta reactiva que notifica automáticamente a la interfaz gráfica ante cambios en SQLite.
   - `getById(id: Int): Tarea?`: Búsqueda puntual por identificador.
   - `getNoSincronizadas(): List<Tarea>`: Extrae tareas pendientes de subir al servidor remoto.
   - `insert(tarea: Tarea): Long`, `update(tarea: Tarea)`, `delete(tarea: Tarea)`, `deleteById(id: Int)`.
   - `marcarSincronizada(id: Int)` y `eliminarCompletadas()`.

3. **Base de Datos `AppDatabase.kt` (`@Database`)**:
   - Implementa el patrón **Singleton con sincronización segura (`@Volatile` + `synchronized`)**, evitando aperturas concurrentes de la base SQLite.

4. **Repositorio `TareaRepository.kt`**:
   - Actúa como la única fuente de verdad (Single Source of Truth), encapsulando el DAO y ofreciendo a la Persona D los métodos acordados: `getAll()`, `insert()`, `update()`, `delete()`.

---

## 📂 Estructura del Código

```text
app/src/main/java/com/example/tareas/
├── MainActivity.kt                <- ComponentActivity, inyección de Room e inicio de UI
├── data/                          <- Responsabilidad de PERSONA C
│   ├── Tarea.kt                   <- Entidad Room con campos requeridos y sincronizado: Boolean
│   ├── TareaDao.kt                <- Métodos CRUD completos y consultas especializadas
│   ├── AppDatabase.kt             <- Singleton RoomDatabase sobre SQLite
│   └── TareaRepository.kt         <- Repositorio que desacopla datos de la UI
└── ui/                            <- Responsabilidad de PERSONA D
    ├── ListaTareasScreen.kt       <- Pantalla principal reactiva con métricas y simulación sync
    ├── TareaItem.kt               <- Tarjeta con estado de sincronización visual (Offline / Sync)
    ├── AgregarEditarTareaScreen.kt<- Formulario modal de creación/edición
    └── theme/                     <- Estilos y paleta Material Design 3
```

---

## 🎯 Cumplimiento de la Rúbrica y Requerimientos

| Requerimiento del Taller | Implementación |
|---|---|
| **1. Entidad Tarea completa** | Modelada en `Tarea.kt` con `id`, `titulo`, `descripcion`, `estado_completado`, `fecha_creacion` y `sincronizado`. |
| **2. Persistencia local con Room** | Implementada en `AppDatabase.kt` con SQLite subyacente. |
| **3. Métodos CRUD completos** | Inserción, consulta reactiva (`Flow`), actualización y eliminación granular y en lote en `TareaDao` y `TareaRepository`. |
| **4. Disponibilidad Offline sin pérdida** | Los datos se escriben físicamente en disco flash del dispositivo; persisten al forzar cierre, rotación o reinicio. |
| **5. Arquitectura Offline-First con sincronización** | Campo `sincronizado = false` en cada modificación local. Botón de simulación de sincronización que actualiza el flag a `true` preparando el Punto 5. |

---

## 💡 Respuestas Conceptuales para la Sustentación (Saber 40%)

- **¿Por qué Room en lugar de SQLite directo?**  
  Room provee verificación de consultas SQL en tiempo de compilación, elimina el código repetitivo de `Cursor` y `ContentValues`, y se integra nativamente con `Flow` y Coroutines de Kotlin para interfaces reactivas.
- **¿Por qué el campo `sincronizado: Boolean` es clave en Offline-First?**  
  Permite que el usuario continúe interactuando con la app sin conectividad (cero bloqueos). Cuando el dispositivo recupera acceso a internet, un proceso en segundo plano (WorkManager o servicio HTTP del Punto 5) consulta `getNoSincronizadas()` para enviar únicamente las modificaciones pendientes al servidor.

