package com.example.induztek.view.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable

/**
 * Lista desplegable reutilizable (para llaves foráneas: cliente, dirección, tipos...).
 * opciones: pares (id, texto). seleccionId = 0 significa "sin selección".
 */
@Composable
fun SelectorDesplegable(
    etiqueta: String,
    opciones: List<Pair<Int, String>>,
    seleccionId: Int,
    onSeleccion: (Int, String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    val textoSeleccionado = opciones.firstOrNull { it.first == seleccionId }?.second ?: ""

    Column(modifier = modifier.fillMaxWidth()) {
        Box {
            OutlinedTextField(
                value = textoSeleccionado,
                onValueChange = {},
                readOnly = true,
                label = { Text(etiqueta) },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                isError = error != null,
                supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )
            // Capa transparente que recibe el toque (un TextField readOnly no abre el menú por sí solo)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expandido = true }
            )
            DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                if (opciones.isEmpty()) {
                    DropdownMenuItem(text = { Text("Sin opciones disponibles") }, onClick = { expandido = false })
                }
                opciones.forEach { (id, texto) ->
                    DropdownMenuItem(
                        text = { Text(texto) },
                        onClick = {
                            onSeleccion(id, texto)
                            expandido = false
                        }
                    )
                }
            }
        }
    }
}
