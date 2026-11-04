package com.example.induztek.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.induztek.view.utils.AnimacionEntradaFormulario
import com.example.induztek.view.utils.CameraCapture
import com.example.induztek.view.utils.PhotoBox
import com.example.induztek.view.utils.SelectorDesplegable

@Composable
fun UserFormScreen(
    viewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    userId: Int,
    onDone: () -> Unit
) {
    val tipos by tipoUsuarioViewModel.tipos.collectAsStateWithLifecycle()
    var tipoUsuarioId by rememberSaveable { mutableIntStateOf(0) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var usuario by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var showCamera by rememberSaveable { mutableStateOf(false) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }
    val dbMessage by viewModel.message.collectAsStateWithLifecycle()

    DisposableEffect(Unit) { onDispose { viewModel.clearMessage() } }

    LaunchedEffect(userId) {
        if (userId > 0 && !loaded) {
            viewModel.getUser(userId)?.let {
                nombre = it.nombre
                usuario = it.usuario
                password = it.password
                imageUri = it.profileImageUri
                tipoUsuarioId = it.tipoUsuarioId
            }
        }
        loaded = true
        visible = true
    }

    if (showCamera) {
        CameraCapture(
            carpeta = "usuarios",
            onImageCaptured = { uri ->
                imageUri = uri.toString()
                showCamera = false
            },
            onClose = { showCamera = false }
        )
        return
    }

    // ANIMACIÓN 1 (view/utils/Animations.kt): fade + deslizamiento vertical
    AnimacionEntradaFormulario(visible = visible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (userId > 0) "Editar Usuario" else "Nuevo Usuario",
                style = MaterialTheme.typography.headlineMedium
            )

            PhotoBox(imageUri = imageUri, circle = true, onTakePhoto = { showCamera = true })

            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it },
                label = { Text("Nombre") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = usuario, onValueChange = { usuario = it; viewModel.clearMessage() },
                label = { Text("Usuario") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Contraseña") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            // Tipo de usuario (FK): lista desplegable con los registros de tipo_usuarios
            SelectorDesplegable(
                etiqueta = "Tipo de usuario",
                opciones = tipos.map { it.id to it.tipoUsuario },
                seleccionId = tipoUsuarioId,
                onSeleccion = { id, _ -> tipoUsuarioId = id }
            )

            (error ?: dbMessage)?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    when {
                        nombre.isBlank() -> error = "El nombre es obligatorio"
                        usuario.trim().length < 3 -> error = "El usuario debe tener al menos 3 caracteres"
                        password.length < 4 -> error = "La contraseña debe tener al menos 4 caracteres"
                        tipos.none { it.id == tipoUsuarioId } -> error = "Seleccione un tipo de usuario"
                        else -> {
                            error = null
                            viewModel.saveUser(userId, nombre.trim(), usuario.trim(), password, imageUri, tipoUsuarioId, onDone)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar Usuario") }

            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}
