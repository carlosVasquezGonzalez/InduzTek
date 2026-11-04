package com.example.induztek.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.induztek.repository.models.User
import com.example.induztek.viewmodels.TipoUsuarioViewModel
import com.example.induztek.viewmodels.UserViewModel
import androidx.compose.foundation.lazy.items

@Composable
fun UserListScreen(
    viewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    currentUserId: Int?,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val tipos by tipoUsuarioViewModel.tipos.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val nombreTipo = tipos.associate { it.id to it.tipoUsuario }
    var toDelete by remember { mutableStateOf<User?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(users, key = { it.id }) { user ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (user.profileImageUri != null) {
                            AsyncImage(
                                model = user.profileImageUri,
                                contentDescription = user.nombre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(52.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(user.usuario, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                nombreTipo[user.tipoUsuarioId] ?: "Sin tipo",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { onEdit(user.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        // No se permite eliminar al usuario con sesión activa
                        IconButton(onClick = { toDelete = user }, enabled = user.id != currentUserId) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Agregar usuario")
        }
    }

    toDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar usuario") },
            text = { Text("¿Eliminar a ${user.nombre}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteUser(user)
                    toDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }

    // Error al eliminar (usuario con visitas registradas)
    message?.let { texto ->
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            text = { Text(texto) },
            confirmButton = { TextButton(onClick = { viewModel.clearMessage() }) { Text("Aceptar") } }
        )
    }
}
