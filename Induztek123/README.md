# Induztek 2.2

App Android (Kotlin + Jetpack Compose) para registrar **visitas técnicas con pruebas a equipos**.
Reorganizada con la misma arquitectura y los mismos módulos que la Sumativa 2:
**MVVM**, **Room (SQLite)**, **corrutinas**, **CameraX**, **Navigation** con menú lateral y **3 animaciones**.

## Abrir y ejecutar
1. Android Studio → *Open* → carpeta `Induztek2_2` y esperar el *Gradle Sync*.
2. Ejecutar en un dispositivo/emulador con cámara.
3. Usuarios de prueba (se crean al instalar vía `RoomDatabase.Callback`):
   - **gerente / 1234** → ve todo el menú (administración + visitas).
   - **tecnico / 1234** → solo ingresa servicios, ve su historial y usa la cámara.

## Estructura (MVVM, igual a la Sumativa 2)
```
app/src/main/java/com/example/induztek/
├── MainActivity.kt
├── repository/                    (Model)
│   ├── data/    AppDatabase.kt, UserDao, TipoUsuarioDao, ClienteDao, DireccionDao,
│   │            TipoEquipoDao, TipoPruebaDao, VisitaDao                       (Room)
│   └── models/  User, TipoUsuario, Cliente, Direccion, TipoEquipo, TipoPrueba,
│                Visita (+ EquipoPrueba), ItemCatalogo                         (Entidades)
├── viewmodels/  LoginViewModel, UserViewModel, TipoUsuarioViewModel, CatalogoViewModel
│                (Cliente/TipoEquipo/TipoPrueba), DireccionViewModel, VisitaViewModel, Validaciones
├── navegations/ AppNavigation.kt (Routes, menú lateral por rol, NavHost)
└── view/
    ├── theme/   Theme.kt, Color.kt, Type.kt
    ├── utils/   CameraCapture, PhotoBox, SelectorDesplegable, Animations (las 3 animaciones)
    └── ui/      LoginScreen, HomeScreen, NuevaVisitaScreen, HistorialScreen,
                 CatalogoScreen (clientes / tipos de equipo / tipos de prueba),
                 DireccionScreen, UserListScreen, UserFormScreen, TipoUsuarioScreen, CameraScreen
```

## Equivalencias con la Sumativa 2
| Sumativa 2 | Induztek 2.2 |
|---|---|
| `Product` (CRUD + foto) | `Visita` + `EquipoPrueba` (se crea con varios equipos, cada uno con foto) |
| `User` con email | `User` con `usuario` único (login como en Induztek) |
| `TipoUsuario` (FK en User) | `TipoUsuario` Gerente / Técnico (FK en User) |
| — | `Cliente`, `Direccion` (FK a cliente), `TipoEquipo`, `TipoPrueba` (catálogos que ya tenía Induztek, ahora en Room) |
| Menú: Productos, Usuarios, Tipos, Cámara | Menú según rol (gerente / técnico) |

## Requisitos cubiertos
| Requisito | Dónde |
|---|---|
| CRUD catálogos (crear, listar, editar, eliminar, nombre único) | `CatalogoScreen` + `CatalogoViewModel` + DAOs |
| CRUD Direcciones (FK a cliente) | `DireccionScreen`, `DireccionViewModel`, `DireccionDao` |
| CRUD Usuarios + usuario único + foto de perfil | `UserDao`, `UserViewModel`, `UserListScreen`, `UserFormScreen` |
| Tipos de usuario (FK en User) | `TipoUsuarioScreen`, `TipoUsuarioViewModel` |
| Visitas: crear (transacción visita + equipos), listar con JOIN, eliminar | `VisitaDao`, `VisitaViewModel`, `NuevaVisitaScreen`, `HistorialScreen` |
| Login contra Room | `UserDao.login`, `LoginViewModel`, `LoginScreen` |
| Corrutinas | DAOs `suspend` + `Flow`; `viewModelScope.launch(Dispatchers.IO)`, `withContext`, `produceState`, `LaunchedEffect` |
| CameraX | `CameraCapture.kt`; usado en la foto de cada equipo, foto de perfil y pantalla Cámara |
| Menú y navegación | `ModalNavigationDrawer` + `TopAppBar` + `NavHost` con argumentos (`user_form?id=`) |
| Animación 1 | `AnimacionEntradaFormulario` (fade + slide) en `NuevaVisitaScreen` y `UserFormScreen` |
| Animación 2 | `Modifier.pulsoYRotacion()` en el logo del Login |
| Animación 3 | `navEnterTransition`… en el `NavHost` |
| Permisos | `AndroidManifest.xml`: `CAMERA` + permiso en tiempo de ejecución en `CameraCapture` |

## Notas
- Las fotos se guardan en almacenamiento interno privado: `files/fotos/{equipos|usuarios|camara}/img_<timestamp>.jpg`
  y se excluyen del respaldo (`res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml`).
- BD versión 2 con `fallbackToDestructiveMigration()` (la v1 de Induztek solo tenía la tabla `usuarios`).
- Los catálogos (clientes, direcciones, tipos) parten vacíos: el gerente los registra antes de que el técnico ingrese servicios.
- Contraseñas en texto plano: solo con fines académicos.
