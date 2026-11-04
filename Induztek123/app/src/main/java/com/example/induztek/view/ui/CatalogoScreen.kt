package com.example.induztek.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.induztek.repository.models.ItemCatalogo
import com.example.induztek.viewmodels.CatalogoViewModel

/**
 * CRUD de un catálogo simple (Clientes, Tipos de equipo, Tipos de prueba)
 * en una sola pantalla: formulario arriba, lista abajo.
 */
@Composable
fun <T : ItemCatalogo> CatalogoScreen(
    etiqueta: String,
    viewModel: CatalogoViewModel<T>
) {
    val lista by viewModel.items.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var editId by rememberSaveable { mutableIntStateOf(0) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var toDelete by remember { mutableStateOf<T?>(null) }

    DisposableEffect(Unit) { onDispose { viewModel.clearMessage() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (editId > 0) "Editar $etiqueta" else "Nuevo $etiqueta",
            style = MaterialTheme.typography.titleLarge
        )
        OutlinedTextField(
            value = nombre,
            onValueChange = {
                if (it.length <= CatalogoViewModel.MAX_LARGO) nombre = it
                viewModel.clearMessage()
            },
            label = { Text(etiqueta.replaceFirstChar { c -> c.uppercase() }) },
            supportingText = { Text("${nombre.length}/${CatalogoViewModel.MAX_LARGO}") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                viewModel.save(editId, nombre) {
                    editId = 0
                    nombre = ""
                }
            }) { Text(if (editId > 0) "Actualizar" else "Agregar") }
            if (editId > 0) {
                OutlinedButton(onClick = { editId = 0; nombre = "" }) { Text("Cancelar") }
            }
        }

        if (lista.isEmpty()) {
            Text("Aún no hay registros")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(lista, key = { it.id }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.nombre, modifier = Modifier.weight(1f))
                        IconButton(onClick = { editId = item.id; nombre = item.nombre }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { toDelete = item }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar") },
            text = { Text("¿Eliminar \"${item.nombre}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(item)
                    toDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}
