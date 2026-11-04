package com.example.induztek.navegations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.induztek.repository.models.TipoUsuario
import com.example.induztek.view.ui.CameraScreen
import com.example.induztek.view.ui.CatalogoScreen
import com.example.induztek.view.ui.DireccionScreen
import com.example.induztek.view.ui.HistorialScreen
import com.example.induztek.view.ui.HomeScreen
import com.example.induztek.view.ui.LoginScreen
import com.example.induztek.view.ui.NuevaVisitaScreen
import com.example.induztek.view.ui.TipoUsuarioScreen
import com.example.induztek.view.ui.UserFormScreen
import com.example.induztek.view.ui.UserListScreen
import com.example.induztek.view.utils.navEnterTransition
import com.example.induztek.view.utils.navExitTransition
import com.example.induztek.view.utils.navPopEnterTransition
import com.example.induztek.view.utils.navPopExitTransition
import com.example.induztek.viewmodels.ClienteViewModel
import com.example.induztek.viewmodels.DireccionViewModel
import com.example.induztek.viewmodels.LoginViewModel
import com.example.induztek.viewmodels.TipoEquipoViewModel
import com.example.induztek.viewmodels.TipoPruebaViewModel
import com.example.induztek.viewmodels.TipoUsuarioViewModel
import com.example.induztek.viewmodels.UserViewModel
import com.example.induztek.viewmodels.VisitaViewModel
import kotlinx.coroutines.launch

/** Rutas de navegación */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val NUEVA_VISITA = "nueva_visita"
    const val HISTORIAL = "historial"
    const val CLIENTES = "clientes"
    const val DIRECCIONES = "direcciones"
    const val TIPOS_EQUIPO = "tipos_equipo"
    const val TIPOS_PRUEBA = "tipos_prueba"
    const val USERS = "users"
    const val USER_FORM = "user_form?id={id}"
    const val TIPOS_USUARIO = "tipos_usuario"
    const val CAMERA = "camera"

    fun userForm(id: Int = 0) = "user_form?id=$id"
}

data class MenuItem(val route: String, val label: String, val icon: ImageVector)

/** Opciones del menú lateral para el técnico. */
val menuTecnico = listOf(
    MenuItem(Routes.HOME, "Inicio", Icons.Default.Home),
    MenuItem(Routes.NUEVA_VISITA, "Ingresar servicio", Icons.Default.AddCircle),
    MenuItem(Routes.HISTORIAL, "Ver historial", Icons.Default.History),
    MenuItem(Routes.CAMERA, "Cámara", Icons.Default.PhotoCamera)
)

/** Opciones del menú lateral para el gerente (todo lo del técnico + administración). */
val menuGerente = listOf(
    MenuItem(Routes.HOME, "Inicio", Icons.Default.Home),
    MenuItem(Routes.NUEVA_VISITA, "Ingresar servicio", Icons.Default.AddCircle),
    MenuItem(Routes.HISTORIAL, "Historial general", Icons.Default.History),
    MenuItem(Routes.CLIENTES, "Clientes", Icons.Default.Business),
    MenuItem(Routes.DIRECCIONES, "Direcciones", Icons.Default.LocationOn),
    MenuItem(Routes.TIPOS_EQUIPO, "Tipos de equipo", Icons.Default.Build),
    MenuItem(Routes.TIPOS_PRUEBA, "Tipos de prueba", Icons.Default.Science),
    MenuItem(Routes.USERS, "Usuarios", Icons.Default.People),
    MenuItem(Routes.TIPOS_USUARIO, "Tipos de usuario", Icons.Default.Badge),
    MenuItem(Routes.CAMERA, "Cámara", Icons.Default.PhotoCamera)
)

private fun titleFor(route: String?): String = when (route) {
    Routes.HOME -> "Inicio"
    Routes.NUEVA_VISITA -> "Ingresar servicio"
    Routes.HISTORIAL -> "Historial"
    Routes.CLIENTES -> "Clientes"
    Routes.DIRECCIONES -> "Direcciones"
    Routes.TIPOS_EQUIPO -> "Tipos de equipo"
    Routes.TIPOS_PRUEBA -> "Tipos de prueba"
    Routes.USERS -> "Usuarios"
    Routes.USER_FORM -> "Usuario"
    Routes.TIPOS_USUARIO -> "Tipos de usuario"
    Routes.CAMERA -> "Cámara"
    else -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    userViewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    clienteViewModel: ClienteViewModel,
    direccionViewModel: DireccionViewModel,
    tipoEquipoViewModel: TipoEquipoViewModel,
    tipoPruebaViewModel: TipoPruebaViewModel,
    visitaViewModel: VisitaViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()

    val esGerente = loginState.loggedUser?.tipoUsuarioId == TipoUsuario.ID_GERENTE
    val menuItems = if (esGerente) menuGerente else menuTecnico

    val isLogin = currentRoute == null || currentRoute == Routes.LOGIN
    val isTopLevel = menuItems.any { it.route == currentRoute }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = false }
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isLogin,
        drawerContent = {
            ModalDrawerSheet {
                // El menú puede ser largo (gerente): se desplaza dentro del panel
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        loginState.loggedUser?.nombre ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Text(
                        if (esGerente) "Gerente" else "Técnico",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    menuItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item.label) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            selected = currentRoute == item.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navigateTopLevel(item.route)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(
                        label = { Text("Cerrar sesión") },
                        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            loginViewModel.logout()
                            visitaViewModel.limpiarFormulario()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!isLogin) {
                    TopAppBar(
                        title = { Text(titleFor(currentRoute)) },
                        navigationIcon = {
                            if (isTopLevel) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                                }
                            } else {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                loginViewModel = loginViewModel,
                userViewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                clienteViewModel = clienteViewModel,
                direccionViewModel = direccionViewModel,
                tipoEquipoViewModel = tipoEquipoViewModel,
                tipoPruebaViewModel = tipoPruebaViewModel,
                visitaViewModel = visitaViewModel,
                currentUserId = loginState.loggedUser?.id,
                currentUserName = loginState.loggedUser?.nombre ?: "",
                esGerente = esGerente,
                menuItems = menuItems,
                onNavigateTopLevel = ::navigateTopLevel
            )
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier,
    loginViewModel: LoginViewModel,
    userViewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    clienteViewModel: ClienteViewModel,
    direccionViewModel: DireccionViewModel,
    tipoEquipoViewModel: TipoEquipoViewModel,
    tipoPruebaViewModel: TipoPruebaViewModel,
    visitaViewModel: VisitaViewModel,
    currentUserId: Int?,
    currentUserName: String,
    esGerente: Boolean,
    menuItems: List<MenuItem>,
    onNavigateTopLevel: (String) -> Unit
) {
    val idArg = listOf(navArgument("id") { type = NavType.IntType; defaultValue = 0 })

    // ANIMACIÓN 3 (view/utils/Animations.kt): deslizamiento horizontal entre pantallas
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier,
        enterTransition = navEnterTransition,
        exitTransition = navExitTransition,
        popEnterTransition = navPopEnterTransition,
        popExitTransition = navPopExitTransition
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(loginViewModel) {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        }
        composable(Routes.HOME) {
            HomeScreen(userName = currentUserName, items = menuItems, onNavigate = onNavigateTopLevel)
        }
        composable(Routes.NUEVA_VISITA) {
            NuevaVisitaScreen(
                viewModel = visitaViewModel,
                clienteViewModel = clienteViewModel,
                tipoEquipoViewModel = tipoEquipoViewModel,
                tipoPruebaViewModel = tipoPruebaViewModel,
                tecnicoId = currentUserId ?: 0,
                onGuardada = { onNavigateTopLevel(Routes.HISTORIAL) }
            )
        }
        composable(Routes.HISTORIAL) {
            HistorialScreen(
                viewModel = visitaViewModel,
                tecnicoId = if (esGerente) null else currentUserId,
                puedeEliminar = esGerente
            )
        }
        composable(Routes.CLIENTES) {
            CatalogoScreen(etiqueta = "cliente", viewModel = clienteViewModel)
        }
        composable(Routes.DIRECCIONES) {
            DireccionScreen(viewModel = direccionViewModel, clienteViewModel = clienteViewModel)
        }
        composable(Routes.TIPOS_EQUIPO) {
            CatalogoScreen(etiqueta = "tipo de equipo", viewModel = tipoEquipoViewModel)
        }
        composable(Routes.TIPOS_PRUEBA) {
            CatalogoScreen(etiqueta = "tipo de prueba", viewModel = tipoPruebaViewModel)
        }
        composable(Routes.USERS) {
            UserListScreen(
                viewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                currentUserId = currentUserId,
                onAdd = { navController.navigate(Routes.userForm()) },
                onEdit = { id -> navController.navigate(Routes.userForm(id)) }
            )
        }
        composable(Routes.USER_FORM, arguments = idArg) { entry ->
            UserFormScreen(
                viewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                userId = entry.arguments?.getInt("id") ?: 0,
                onDone = { navController.popBackStack() }
            )
        }
        composable(Routes.TIPOS_USUARIO) {
            TipoUsuarioScreen(tipoUsuarioViewModel)
        }
        composable(Routes.CAMERA) {
            CameraScreen()
        }
    }
}
