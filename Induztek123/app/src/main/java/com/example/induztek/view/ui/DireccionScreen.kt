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
import com.example.induztek.repository.models.Cliente
import com.example.induztek.repository.models.DireccionConCliente
import com.example.induztek.view.utils.SelectorDesplegable
import com.example.induztek.viewmodels.CatalogoViewModel
import com.example.induztek.viewmodels.DireccionViewModel

/** CRUD de direcciones (FK a Cliente) en una sola pantalla. */
@Composable
fun DireccionScreen(
    viewModel: DireccionViewModel,
    clienteViewModel: CatalogoViewModel<Cliente>
) {
    val direcciones by viewModel.direcciones.collectAsStateWithLifecycle()
    val clientes by clienteViewModel.items.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var editId by rememberSaveable { mutableIntStateOf(0) }
    var clienteId by rememberSaveable { mutableIntStateOf(0) }
    var texto by rememberSaveable { mutableStateOf("") }
    var toDelete by remember { mutableStateOf<DireccionConCliente?>(null) }

    DisposableEffect(Unit) { onDispose { viewModel.clearMessage() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            if (editId > 0) "Editar dirección" else "Nueva dirección",
            style = MaterialTheme.typography.titleLarge
        )

        if (clientes.isEmpty()) {
            Text("Primero agrega clientes")
        } else {
            SelectorDesplegable(
                etiqueta = "Cliente",
                opciones = clientes.map { it.id to it.nombre },
                seleccionId = clienteId,
                onSeleccion = { id, _ -> clienteId = id; viewModel.clearMessage() }
            )
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it; viewModel.clearMessage() },
                label = { Text("Dirección") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    viewModel.save(editId, clienteId, texto) {
                        editId = 0
                        clienteId = 0
                        texto = ""
                    }
                }) { Text(if (editId > 0) "Actualizar" else "Agregar") }
                if (editId > 0) {
                    OutlinedButton(onClick = { editId = 0; clienteId = 0; texto = "" }) { Text("Cancelar") }
                }
            }
        }

        if (clientes.isNotEmpty() && direcciones.isEmpty()) {
            Text("Aún no hay registros")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(direcciones, key = { it.id }) { d ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(d.texto)
                            Text(d.clienteNombre, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { editId = d.id; clienteId = d.clienteId; texto = d.texto }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { toDelete = d }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { d ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar dirección") },
            text = { Text("¿Eliminar \"${d.texto}\" de ${d.clienteNombre}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(d)
                    toDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}
